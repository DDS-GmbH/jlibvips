package org.jlibvips.jna;

import com.sun.jna.Native;

public class VipsBindingsSingleton {


    private static String libraryPath = "vips";

    public static void configure(String lp) {
        libraryPath = lp;
    }

    private static VipsBindings INSTANCE;

    public static VipsBindings instance() {
        if(INSTANCE == null) {
            if(libraryPath == null || libraryPath.isEmpty()) {
                throw new IllegalStateException("Please call VipsBindingsSingleton.configure(...) before getting the instance.");
            }
            VipsBindings bindings = Native.load(libraryPath, VipsBindings.class);
            // libvips before 8.17 creates its global lock in vips_init(); calls that skip the lazy
            // initialisation of operations, such as vips_error_clear(), crash without it.
            if (bindings.vips_init("jlibvips") != 0) {
                throw new IllegalStateException("Could not initialise libvips: " + bindings.vips_error_buffer());
            }
            INSTANCE = bindings;
        }
        return INSTANCE;
    }

    private VipsBindingsSingleton() {
    }

}
