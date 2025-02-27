package org.jlibvips;

/**
 * Java representation of <code>VipsAngle</code> enum from libvips
 *
 * @see <a href="http://libvips.github.io/libvips/API/current/libvips-conversion.html#VipsAngle">VipsAngle</a>
 * @author amp
 */
public enum VipsAngle {
    D0,
    D90,
    D180,
    D270,
    Last;

    /**
     * Creates a {@link VipsAngle} based on degrees as integer.
     *
     * @param value degrees
     * @return the {@link VipsAngle}
     */
    public static VipsAngle fromInteger(int value) {
        return switch (value) {
            case 0 -> D0;
            case 90 -> D90;
            case 180 -> D180;
            case 270 -> D270;
            default -> throw new IllegalArgumentException("Allowed VipsAngle's are [0°, 90°, 180°, 270°], not %s.".formatted(value));
        };
    }

    /**
     * Creates a {@link VipsAngle} based on the <a href="https://www.libvips.org/API/current/libvips-header.html#VIPS-META-ORIENTATION:CAPS">vips meta orientation</a>.
     *
     * @param value degrees
     * @return the {@link VipsAngle}
     */
    public static VipsAngle fromMetaOrientation(int value) {
        return switch (value) {
          case 1, 2 -> D0;
          case 3, 4 -> D180;
          case 5, 6 -> D270;
          case 7, 8 -> D90;
          default -> throw new IllegalArgumentException("Allowed VipsMetagAngle's are [1-8], not %s.".formatted(value));
        };
    }


    public double toDouble() {
        return switch (this) {
            case D90 -> 90.0;
            case D180 -> 180.0;
            case D270 -> 270.0;
            default -> 0.0;
        };
    }

    public VipsAngle add(VipsAngle other) {
        return fromInteger((int) (toDouble() + other.toDouble() % 360));
    }
}
