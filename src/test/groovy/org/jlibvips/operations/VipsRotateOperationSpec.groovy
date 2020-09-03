package org.jlibvips.operations

import org.jlibvips.VipsAngle
import org.jlibvips.VipsImage
import org.jlibvips.jna.VipsBindingsSingleton
import spock.lang.Specification

import java.nio.file.Files

import static org.jlibvips.TestUtils.copyResourceToFS

class VipsRotateOperationSpec extends Specification {

    def "Rotate a PNG by various angles."() {
        given:
        def file = copyResourceToFS "1920x1080.png"
        def image = VipsImage.fromFile(file)
        when:
        def rotateImage = image.rotate(angle).create()
        then:
        rotateImage.width == expectedWith
        rotateImage.height == expectedHeight
        cleanup:
        image.unref()
        rotateImage.unref()
        Files.deleteIfExists file
        where:
        angle << [VipsAngle.D0, VipsAngle.D90, VipsAngle.D180, VipsAngle.D270]
        expectedWith << [1920, 1080, 1920, 1080]
        expectedHeight << [1080, 1920, 1080, 1920]
    }

}
