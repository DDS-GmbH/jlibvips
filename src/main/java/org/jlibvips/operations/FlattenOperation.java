package org.jlibvips.operations;

import com.sun.jna.Pointer;
import org.jlibvips.VipsImage;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.util.Varargs;
import org.jlibvips.util.VipsUtils;

import java.util.List;

/**
 * Take the last band of in as an alpha and use it to blend the remaining channels with background .
 *
 * The alpha channel is 0 - max_alpha , where 1 means 100% image and 0 means 100% background.
 * Non-complex images only. background defaults to zero (black).
 *
 * max_alpha has the default value 255, or 65535 for images tagged as VIPS_INTERPRETATION_RGB16 or
 * VIPS_INTERPRETATION_GREY16.
 *
 * Useful for flattening PNG images to RGB.
 *
 * @author codecitizen
 */
public class FlattenOperation {

  private final VipsImage in;

  private Pointer background;
  private Double maxAlpha;

  public FlattenOperation(VipsImage in) {
    this.in = in;
  }

  public VipsImage flatten() {
    Pointer[] out = new Pointer[1];
    int ret = VipsBindingsSingleton.instance().vips_flatten(in.getPtr(), out,
            new Varargs().add("background", background).add("max_alpha", maxAlpha).toArray());
    if(ret != 0) {
      throw new VipsException("vips_flatten", ret);
    }
    return new VipsImage(out[0]);
  }

  public FlattenOperation background(List<Float> background) {
    return background(background != null? background.stream().mapToDouble(Float::doubleValue).toArray() : null);
  }

  public FlattenOperation background(double[] background) {
    this.background = background != null?
            VipsUtils.toPointer(background) : null;
    return this;
  }

  public FlattenOperation maxAlpha(double maxAlpha) {
    this.maxAlpha = maxAlpha;
    return this;
  }
}
