package org.jlibvips.operations;

import com.sun.jna.Pointer;
import org.jlibvips.VipsImage;
import org.jlibvips.VipsOperationRelational;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.util.VipsUtils;

public class RelationalOperation {

    private final VipsImage image;
    private VipsOperationRelational relational;
    private double[] c;

    public RelationalOperation(VipsImage image) {
        this.image = image;
    }

    public VipsImage relational_const(VipsOperationRelational relational, double[] c) {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance().vips_relational_const(image.getPtr(), out, VipsUtils.toOrdinal(relational), c, c.length);
        image.unref();
        if (ret != 0) {
            throw new VipsException("vips_relational_const", ret);
        }
        return new VipsImage(out[0]);
    }

    public VipsImage relational_const(VipsOperationRelational relational, double c) {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance().vips_relational_const1(image.getPtr(), out, VipsUtils.toOrdinal(relational), c);
        image.unref();
        if (ret != 0) {
            throw new VipsException("vips_relational_const", ret);
        }
        return new VipsImage(out[0]);
    }

}
