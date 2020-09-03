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
            INSTANCE = Native.load(libraryPath, VipsBindings.class);
        }
        return INSTANCE;
    }

    private VipsBindingsSingleton() {
    }

}
