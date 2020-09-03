package org.jlibvips.operations;

import org.jlibvips.VipsForeignPngFilter;
import org.jlibvips.VipsImage;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.util.Varargs;
import org.jlibvips.util.VipsUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PngSaveOperation implements SaveOperation {

    private final VipsImage image;

    private Integer compression;
    private Boolean interlace;
    private char[] profile;
    private VipsForeignPngFilter filter;
    private Boolean palette;
    private Integer colors;
    private Integer quality;
    private Double dither;

    public PngSaveOperation(VipsImage image) {
        this.image = image;
    }


    @Override
    public Path save() throws IOException, VipsException {
        Path path = Files.createTempFile("jlibvips", ".png");
        int ret = VipsBindingsSingleton.instance().vips_pngsave(image.getPtr(), path.toString(),
                new Varargs()
                        .add("compression", compression)
                        .add("interlace", VipsUtils.booleanToInteger(interlace))
                        .add("profile", profile)
                        .add("filter", VipsUtils.toOrdinal(filter))
                        .add("palette", VipsUtils.booleanToInteger(palette))
                        .add("colours", colors)
                        .add("Q", quality)
                        .add("dither", dither)
                        .toArray());
        if(ret != 0) {
            throw new VipsException("vips_pngsave", ret);
        }
        return path;
    }

    /**
     * Set the quality factor.
     *
     * @param q quality factor
     * @return this
     */
    public PngSaveOperation quality(Integer q) {
        this.quality = q;
        return this;
    }

    public PngSaveOperation compression(Integer compression) {
        this.compression = compression;
        return this;
    }

    public PngSaveOperation interlace(Boolean interlace) {
        this.interlace = interlace;
        return this;
    }

    public PngSaveOperation profile(char[] profile) {
        this.profile = profile;
        return this;
    }

    public PngSaveOperation filter(VipsForeignPngFilter filter) {
        this.filter = filter;
        return this;
    }

    public PngSaveOperation palette(Boolean palette) {
        this.palette = palette;
        return this;
    }

    public PngSaveOperation colors(Integer colors) {
        this.colors = colors;
        return this;
    }

    public PngSaveOperation dither(Double dither) {
        this.dither = dither;
        return this;
    }

}
