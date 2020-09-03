package org.jlibvips.operations;

import com.sun.jna.Pointer;
import org.jlibvips.VipsImage;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;

public class LinearOperation {
    private final VipsImage image;


    public LinearOperation(VipsImage image) {
        this.image = image;
    }

    public VipsImage linear(double[] a, double[] b, Integer n) {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance().vips_linear(image.getPtr(), out, a, b, n);
        image.unref();
        if (ret == -1) {
            throw new VipsException("vips_linear", ret);
        }
        return new VipsImage(out[0]);
    }

    public VipsImage linear(Double a, Double b) {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance().vips_linear1(image.getPtr(), out, a, b);
        image.unref();
        if (ret == -1) {
            throw new VipsException("vips_linear", ret);
        }
        return new VipsImage(out[0]);
    }

}
