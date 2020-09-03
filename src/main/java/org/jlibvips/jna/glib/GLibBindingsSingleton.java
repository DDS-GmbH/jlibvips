package org.jlibvips.jna.glib;

import com.sun.jna.Native;

public class GLibBindingsSingleton {

  private static String libraryPath = "/usr/local/opt/glib/lib/libglib-2.0.dylib";

  public static void configure(String lp) {
    libraryPath = lp;
  }

  private static GLibBindings INSTANCE;

  public static GLibBindings instance() {
    if(INSTANCE == null) {
      INSTANCE = Native.load(libraryPath, GLibBindings.class);
    }
    return INSTANCE;
  }

  private GLibBindingsSingleton() {
  }

}
