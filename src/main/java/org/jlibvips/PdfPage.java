package org.jlibvips;

import com.sun.jna.Pointer;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import org.jlibvips.exceptions.CouldNotLoadPdfVipsException;
import org.jlibvips.jna.VipsBindings;
import org.jlibvips.jna.VipsBindingsSingleton;

/**
 * A PDF page loaded by libvips, and the scale it was rendered at: the scale asked for, or the largest smaller one in
 * steps of {@link #SCALE_STEP} at which the page fits {@link VipsImage#POPPLER_CAIRO_LIMIT} pixels per side.
 * <p>
 * The fitting scale is derived from one probe load, so a page needs at most two loads. The loads bypass the libvips
 * operation cache, so a page is released as soon as it is closed.
 *
 * @param image the page; {@link #close()} releases it
 * @param scale the scale the page was rendered at
 */
public record PdfPage(VipsImage image, float scale) implements AutoCloseable {

  /**
   * The scale used unless the caller names one.
   */
  public static final float MAX_SCALE = 6.0f;

  /**
   * The step by which the scale is reduce until the page fits.
   */
  public static final float SCALE_STEP = 0.1f;

  /**
   * Reductions stop here; a page still too large at this scale would be over 200 m wide.
   */
  private static final float MIN_SCALE = 0.05f;

  /**
   * Pixels the estimated fitting scale may fall short of the real header by.
   */
  private static final int TOLERANCE_PX = 1;

  private static final Logger LOG = System.getLogger(PdfPage.class.getName());

  /**
   * Loads a page of a PDF file at {@link #MAX_SCALE}, or the largest smaller scale at which it fits.
   *
   * @param pdf  the PDF file
   * @param page the page to load, starting at 0
   * @return an {@link PdfPage} object containing the page and the scale it was rendered at
   */
  public static PdfPage load(Path pdf, int page) {
    return load(pdf, page, MAX_SCALE);
  }

  /**
   * Loads a page of a PDF file at {@code maxScale}, or the largest smaller scale at which it fits.
   *
   * @param pdf      the path to the PDF file
   * @param page     the page to load, starting at 0
   * @param maxScale the scale to render at, unless the page would be too large
   * @return an {@link PdfPage} object containing the page and the scale it was rendered at
   * @throws CouldNotLoadPdfVipsException when libvips cannot load the page, or it fits at no scale
   */
  public static PdfPage load(Path pdf, int page, float maxScale) {
    VipsBindings vips = VipsBindingsSingleton.instance();
    vips.vips_error_clear();
    Pointer source = vips.vips_source_new_from_file(pdf.toString());
    if (source == null) {
      throw new CouldNotLoadPdfVipsException(pdf + ": " + lastError(vips));
    }
    try {
      return fit(source, pdf.toString(), page, maxScale);
    } finally {
      vips.g_object_unref(source);
    }
  }

  /**
   * Loads a page of a PDF held in memory at {@link #MAX_SCALE}, or the largest smaller scale at which it fits.
   *
   * @param pdf  the PDF document
   * @param page the page to load, starting at 0
   * @return an {@link PdfPage} object containing the page and the scale it was rendered at
   */
  public static PdfPage load(byte[] pdf, int page) {
    return load(pdf, page, MAX_SCALE);
  }

  /**
   * Loads a page of a PDF held in memory at {@code maxScale}, or the largest smaller scale at which it fits.
   * libvips takes a copy of the document, so the image outlives the array.
   *
   * @param pdf      the PDF document
   * @param page     the page to load, starting at 0
   * @param maxScale the scale to render at, unless the page would be too large
   * @return an {@link PdfPage} object containing the page and the scale it was rendered at
   * @throws CouldNotLoadPdfVipsException when libvips cannot load the page, or it fits at no scale
   */
  public static PdfPage load(byte[] pdf, int page, float maxScale) {
    VipsBindings vips = VipsBindingsSingleton.instance();
    String origin = pdf.length + " byte buffer";
    Pointer source = memorySource(vips, pdf, origin);
    try {
      return fit(source, origin, page, maxScale);
    } finally {
      vips.g_object_unref(source);
    }
  }

  /**
   * Releases the page by closing its image, which is what {@link VipsImage#unref()} does. Release a
   * page only once, by either of the two: the first release frees the image.
   */
  @Override
  public void close() {
    image.close();
  }

  private static Pointer memorySource(VipsBindings vips, byte[] pdf, String origin) {
    // We create two Pointers here since the one created by vips_blob_copy points to a VipsBlob,
    // while the one created by vips_source_new_from_blob points to a VipsSource from it, which we
    // can then return
    vips.vips_error_clear();
    Pointer blob = vips.vips_blob_copy(pdf, pdf.length);
    if (blob == null) {
      throw new CouldNotLoadPdfVipsException(origin + ": " + lastError(vips));
    }
    try {
      Pointer source = vips.vips_source_new_from_blob(blob);
      if (source == null) {
        throw new CouldNotLoadPdfVipsException(origin + ": " + lastError(vips));
      }
      return source;
    } finally {
      vips.vips_area_unref(blob); // the blob is not needed anymore
    }
  }

  private static PdfPage fit(Pointer source, String origin, int page, float maxScale) {
    if (Float.isNaN(maxScale) || maxScale <= 0) {
      throw new IllegalArgumentException("maxScale must be positive, got " + maxScale);
    }
    float scale = maxScale;
    int loads = 1;
    VipsImage image = loadPage(source, origin, page, scale);
    if (!fits(image)) {
      // libvips renders rint(size * scale) pixels per side, so the image dimension headers bound the page
      // size from below.
      double minWidth = (image.getWidth() - 0.5) / (double) maxScale;
      double minHeight = (image.getHeight() - 0.5) / (double) maxScale;
      image.unref();
      do {
        scale = reduce(scale, origin, page);
      } while (!fitsEstimate(minWidth, minHeight, scale));
      image = loadPage(source, origin, page, scale);
      loads++;
      // The real header decides. This steps down once more only when the estimate was a pixel short.
      while (!fits(image)) {
        image.unref();
        scale = reduce(scale, origin, page);
        image = loadPage(source, origin, page, scale);
        loads++;
      }
    }
    int width = image.getWidth();
    int height = image.getHeight();
    float renderedAt = scale;
    int loadCount = loads;
    if (loadCount == 1) {
      LOG.log(Level.DEBUG, () -> String.format("Loaded page %d of %s at scale %s: %dx%d px",
          page, origin, renderedAt, width, height));
    } else {
      LOG.log(Level.INFO, () -> String.format(
          "Page %d of %s exceeds %d px per side at scale %s, loaded at scale %s: %dx%d px in %d loads",
          page, origin, VipsImage.POPPLER_CAIRO_LIMIT, maxScale, renderedAt, width, height,
          loadCount));
    }
    return new PdfPage(image, renderedAt);
  }

  private static boolean fits(VipsImage image) {
    return image.getWidth() <= VipsImage.POPPLER_CAIRO_LIMIT
        && image.getHeight() <= VipsImage.POPPLER_CAIRO_LIMIT;
  }

  private static boolean fitsEstimate(double minWidth, double minHeight, float scale) {
    // libvips receives the float promoted to double, so the estimate uses the same value
    return Math.rint(minWidth * (double) scale) <= VipsImage.POPPLER_CAIRO_LIMIT + TOLERANCE_PX
        && Math.rint(minHeight * (double) scale) <= VipsImage.POPPLER_CAIRO_LIMIT + TOLERANCE_PX;
  }

  private static float reduce(float scale, String origin, int page) {
    float next = scale - SCALE_STEP;
    if (next < MIN_SCALE) {
      throw new CouldNotLoadPdfVipsException(String.format(
          "page %d of %s does not fit %d px per side at any scale down to %s",
          page, origin, VipsImage.POPPLER_CAIRO_LIMIT, scale));
    }
    return next;
  }

  private static VipsImage loadPage(Pointer source, String origin, int page, float scale) {
    VipsBindings vips = VipsBindingsSingleton.instance();
    Pointer[] out = new Pointer[1];
    vips.vips_error_clear();
    int ret = vips.vips_pdfload_source(source, out, "page", page, "scale", scale, null);
    if (ret != 0) {
      throw new CouldNotLoadPdfVipsException(
          String.format("page %d of %s: %s", page, origin, lastError(vips)));
    }
    VipsImage image = new VipsImage(out[0]);
    LOG.log(Level.DEBUG,
        () -> String.format("pdfload_source: page %d of %s at scale %s is %dx%d px",
            page, origin, scale, image.getWidth(), image.getHeight()));
    return image;
  }

  private static String lastError(VipsBindings vips) {
    String error = vips.vips_error_buffer();
    vips.vips_error_clear();
    return error == null ? "" : error.strip();
  }
}
