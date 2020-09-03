package org.jlibvips.operations;

import com.sun.jna.Pointer;
import org.jlibvips.VipsImage;
import org.jlibvips.VipsInterpretation;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindings;
import org.jlibvips.jna.VipsBindingsSingleton;

public class ColorspaceOperation {

    private VipsImage in;

    public ColorspaceOperation(VipsImage in) {
        this.in = in;
    }

    public VipsImage space(VipsInterpretation space) {
        VipsBindings vips = VipsBindingsSingleton.instance();

        if (!vips.vips_colourspace_issupported(in.getPtr())) {
            throw new VipsException("vips_colourspace_issupported", 0);
        }

        Pointer[] out = new Pointer[1];
        int ret = vips.vips_colourspace(in.getPtr(), out, space.value());
        if (ret == -1) {
            throw new VipsException("vips_colourspace", ret);
        }

        in.unref();

        return new VipsImage(out[0]);
    }

}
