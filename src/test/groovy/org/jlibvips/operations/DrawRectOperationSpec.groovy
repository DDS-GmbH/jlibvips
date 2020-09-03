package org.jlibvips.operations

import org.jlibvips.VipsImage
import org.jlibvips.jna.VipsBindingsSingleton
import spock.lang.Specification

import java.nio.file.Files

import static org.jlibvips.TestUtils.copyResourceToFS

class DrawRectOperationSpec extends Specification {

    def "Draw a rectangular black border with a thickness of 10 pixel to a 500 by 500 Jpeg."() {
        given:
        def imagePath = copyResourceToFS "500x500.jpg"
        def image = VipsImage.fromFile imagePath
        when:
        image.rect()
            .width(480)
            .height(480)
            .top(10)
            .left(10)
            .ink([255.0f, 0.0f, 0.0f])
            //.fill(true)
            .draw()
        then:
        image.bands == 1
        cleanup:
        image.unref()
        Files.deleteIfExists imagePath
    }

}
