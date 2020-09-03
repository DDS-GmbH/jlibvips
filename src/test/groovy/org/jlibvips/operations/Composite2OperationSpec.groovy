package org.jlibvips.operations

import org.jlibvips.TestUtils
import org.jlibvips.VipsBlendMode
import org.jlibvips.VipsImage
import org.jlibvips.jna.VipsBindingsSingleton
import spock.lang.Specification

import java.nio.file.Files

class Composite2OperationSpec extends Specification {

    def "Draw PNG overlay onto JPEG."() {
        given: "A JPEG image and a PNG overlay"
        def imagePath = TestUtils.copyResourceToFS "snippet.jpg"
        def image = VipsImage.fromFile imagePath
        def overlayPath = TestUtils.copyResourceToFS "pin.png"
        def overlay = VipsImage.fromFile overlayPath
        when: "compositioning the base ad overlay image"
        def overlayedImage = image.composite(overlay, VipsBlendMode.Atop)
            .x(143)
            .y(216)
            .create()
        then:
        println overlayedImage.jpeg().save()
        cleanup:
        image.unref()
        overlayedImage.unref()
        Files.deleteIfExists imagePath
        Files.deleteIfExists overlayPath
    }

}
