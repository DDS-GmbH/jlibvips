package org.jlibvips.operations;

import com.sun.jna.Pointer;
import org.jlibvips.*;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.util.Varargs;
import org.jlibvips.util.VipsUtils;

public class CopyOperation {

    private final VipsImage image;
    private Integer width;
    private Integer height;
    private Integer bands;
    private VipsBandFormat format;
    private VipsCoding coding;
    private VipsInterpretation interpretation;
    private Double xres;
    private Double yres;
    private Integer xoffset;
    private Integer yoffset;

    public CopyOperation(VipsImage image) {
        this.image = image;
    }

    public VipsImage copy() {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance()
                .vips_copy(this.image.getPtr(), out, new Varargs()
                        .add("width", width)
                        .add("height", height)
                        .add("bands", bands)
                        .add("format", VipsUtils.toOrdinal(format))
                        .add("coding", VipsUtils.toOrdinal(coding))
                        .add("interpretation", interpretation.value())
                        .add("xres", xres)
                        .add("yres", yres)
                        .add("xoffset", xoffset)
                        .add("yoffset", yoffset)
                        .toArray());

        this.image.unref();

        if (ret != 0) {
            throw new VipsException("vips_copy", ret);
        }
        return new VipsImage(out[0]);
    }

    public CopyOperation width(Integer width) {
        this.width = width;
        return this;
    }

    public CopyOperation height(Integer height) {
        this.height = height;
        return this;
    }

    public CopyOperation bands(Integer bands) {
        this.bands = bands;
        return this;
    }

    public CopyOperation format(VipsBandFormat format) {
        this.format = format;
        return this;
    }

    public CopyOperation coding(VipsCoding coding) {
        this.coding = coding;
        return this;
    }

    public CopyOperation interpretation(VipsInterpretation interpretation) {
        this.interpretation = interpretation;
        return this;
    }

    public CopyOperation xres(Double xres) {
        this.xres = xres;
        return this;
    }

    public CopyOperation yres(Double yres) {
        this.yres = yres;
        return this;
    }

    public CopyOperation xoffset(Integer xoffset) {
        this.xoffset = xoffset;
        return this;
    }

    public CopyOperation yoffset(Integer yoffset) {
        this.yoffset = yoffset;
        return this;
    }
}
