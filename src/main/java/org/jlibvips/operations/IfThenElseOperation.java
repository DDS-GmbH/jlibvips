package org.jlibvips.operations;

import com.sun.jna.Pointer;
import org.jlibvips.VipsImage;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.util.Varargs;
import org.jlibvips.util.VipsUtils;

public class IfThenElseOperation {

    private final VipsImage cond;

    public IfThenElseOperation(VipsImage cond) {
        this.cond = cond;
    }

    public VipsImage ifthenelse(VipsImage inThen, VipsImage inElse, Boolean blend) {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance()
                .vips_ifthenelse(
                        cond.getPtr(),
                        inThen.getPtr(),
                        inElse.getPtr(),
                        out,
                        new Varargs().add("blend", VipsUtils.booleanToInteger(blend)).toArray());
        if(ret != 0) {
            throw new VipsException("vips_ifthenelse", ret);
        }
        return new VipsImage(out[0]);
    }

    public VipsImage ifthenelse(VipsImage inThen, VipsImage inElse) {
        return ifthenelse(inThen, inThen, null);
    }

}
