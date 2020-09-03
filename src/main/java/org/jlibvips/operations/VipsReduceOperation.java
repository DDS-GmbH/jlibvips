package org.jlibvips.operations;

import com.sun.jna.Pointer;
import org.jlibvips.VipsImage;
import org.jlibvips.VipsKernel;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.util.Varargs;

import static org.jlibvips.util.VipsUtils.booleanToInteger;
import static org.jlibvips.util.VipsUtils.toOrdinal;

public class VipsReduceOperation {

    private final Pointer in;
    private final double hshrink;
    private final double vshrink;
    private VipsKernel kernel;
    private boolean centre;

    public VipsReduceOperation(Pointer in, double hshrink, double vshrink) {
        this.in = in;
        this.hshrink = hshrink;
        this.vshrink = vshrink;
    }

    public VipsImage create() {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance().vips_reduce(this.in, out, hshrink, vshrink,
                new Varargs()
                        .add("centre", booleanToInteger(centre))
                        .add("kernel", toOrdinal(kernel))
                        .toArray());
        if(ret != 0) {
            throw new VipsException("vips_reduce", ret);
        }
        return new VipsImage(out[0]);
    }

    public VipsReduceOperation kernel(VipsKernel kernel) {
        this.kernel = kernel;
        return this;
    }

    public VipsReduceOperation centre() {
        this.centre = true;
        return this;
    }
}
