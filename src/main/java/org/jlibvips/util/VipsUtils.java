package org.jlibvips.util;

import com.sun.jna.Pointer;
import org.jlibvips.jna.VipsBindingsSingleton;

public class VipsUtils {

    public static Integer booleanToInteger(Boolean b) {
        return b == null? null : (b? 1 : 0);
    }

    public static Integer toOrdinal(Enum v) {
        return v != null? v.ordinal() : null;
    }

    public static Pointer toPointer(double[] array) {
        return VipsBindingsSingleton.instance().vips_array_double_new(array, array.length);
    }

}
