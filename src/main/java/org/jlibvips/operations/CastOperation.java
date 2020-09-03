package org.jlibvips.operations;

import com.sun.jna.Pointer;
import org.jlibvips.VipsBandFormat;
import org.jlibvips.VipsImage;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.util.Varargs;
import org.jlibvips.util.VipsUtils;

public class CastOperation {

    private final VipsImage image;

    public CastOperation(VipsImage image) {
        this.image = image;
    }

    public VipsImage cast(VipsBandFormat format, Boolean shift) {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance()
                .vips_cast(image.getPtr(), out, VipsUtils.toOrdinal(format), new Varargs().add("shift", shift).toArray());
        image.unref();
        if (ret == -1) {
            throw new VipsException("vips_cast", ret);
        }
        return new VipsImage(out[0]);
    }

    public VipsImage cast(VipsBandFormat format) {
        return cast(format, null);
    }
}
