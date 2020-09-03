package org.jlibvips.operations

import org.jlibvips.VipsImage
import spock.lang.Specification

import java.nio.file.Files

import static org.jlibvips.TestUtils.copyResourceToFS

class FlattenOperationSpec extends Specification {

    def "Should flatten a PNG image into a white background RGB Jpeg."() {
        given:
        def baseImagePath = copyResourceToFS("1920x1080.png")
        def image = VipsImage.fromFile(baseImagePath)
        when:
        def flattenedImage = image.flatten()
                .background([0f, 0f, 0f])
                .maxAlpha(1.0)
                .flatten()
        then:
        flattenedImage.width == 1920
        flattenedImage.height == 1080
        cleanup:
        if(image != null) image.unref()
        if(flattenedImage != null) flattenedImage.unref()
        Files.deleteIfExists(baseImagePath)
    }

}
