package org.jlibvips.operations;

import com.sun.jna.Pointer;
import org.jlibvips.VipsImage;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;

public class BandJoinOperation {

    private VipsImage in;

    public BandJoinOperation(VipsImage in) {
        this.in = in;
    }

    public VipsImage bandjoin_const(double[] c) {
        Pointer[] out = new Pointer[1];

        int ret = VipsBindingsSingleton.instance().vips_bandjoin_const(in.getPtr(), out, c, c.length);
        if (ret == -1) {
            throw new VipsException("vips_bandjoin_const", ret);
        }

        in.unref();
        return new VipsImage(out[0]);
    }

}
