package org.jlibvips.operations

import org.jlibvips.VipsImage
import org.jlibvips.jna.VipsBindingsSingleton
import spock.lang.Specification

import java.nio.file.Files

import static org.jlibvips.TestUtils.copyResourceToFS

class ReduceOperationSpec extends Specification {

    def "Should reduce a 500x500 JPEG into a 300x200 jpeg."() {
        given:
        def smallImagePath = copyResourceToFS("500x500.jpg")
        when:
        def smallImage = VipsImage.fromFile smallImagePath

        int width = 300
        int height = 200
        double hshrink = (double) smallImage.getWidth() / width
        double vshrink = (double)  smallImage.getHeight() / height
        def image = smallImage.reduce(hshrink, vshrink).create()
        then:
        image != null
        image.height == height
        image.width == width
        cleanup:
        smallImage.unref()
        Files.deleteIfExists smallImagePath
    }

}
