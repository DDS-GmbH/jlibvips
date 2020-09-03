package org.jlibvips.operations;

import com.sun.jna.Pointer;
import org.jlibvips.VipsImage;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.util.Varargs;

public class VipsIdentityOperation {

    private Integer bands;
    private Boolean ushort;
    private Integer size = 256;

    public VipsImage create() {
        Pointer[] out = new Pointer[1];
        int ret = VipsBindingsSingleton.instance().vips_identity(out, new Varargs()
                .add("bands", bands)
                .add("ushort", ushort)
                .add("size", size)
                .toArray());
        if (ret != 0) {
            throw new VipsException("vips_identity", ret);
        }
        return new VipsImage(out[0]);
    }

    public VipsIdentityOperation bands(Integer bands) {
        this.bands = bands;
        return this;
    }

    public VipsIdentityOperation ushort(Boolean ushort) {
        this.ushort = ushort;
        return this;
    }

    public VipsIdentityOperation size(Integer size) {
        this.size = size;
        return this;
    }
}
