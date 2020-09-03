package org.jlibvips;

import java.util.Arrays;
import java.util.Optional;

public enum VipsInterpretation {

    ERROR(1),
    MULTIBAND(0),
    B_W(1),
    HISTOGRAM(10),
    XYZ(12),
    LAB(13),
    CMYK(15),
    LABQ(16),
    RGB(17),
    CMC(18),
    LCH(19),
    LABS(21),
    sRGB(22),
    YXY(23),
    FOURIER(24),
    RGB16(25),
    GREY16(26),
    MATRIX(27),
    scRGB(28),
    HSV(29),
    LAST(30);

    private int value;

    VipsInterpretation(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    static Optional<VipsInterpretation> fromValue(int value) {
        return Arrays.stream(values())
                .filter(i -> i.value == value)
                .findAny();
    }

}
