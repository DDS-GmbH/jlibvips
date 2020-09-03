package org.jlibvips.operations;

import com.sun.jna.Pointer;
import org.jlibvips.VipsImage;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.util.Varargs;

public class MaplutOperation {

    private final VipsImage image;

    public MaplutOperation(VipsImage image) {
        this.image = image;
    }

    public VipsImage maplut(VipsImage lut, Integer band) {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance()
                .vips_maplut(image.getPtr(), out, lut.getPtr(), new Varargs().add("band", band).toArray());
        this.image.unref();
        if (ret != 0) {
            throw new VipsException("vips_copy", ret);
        }
        return new VipsImage(out[0]);
    }
}
