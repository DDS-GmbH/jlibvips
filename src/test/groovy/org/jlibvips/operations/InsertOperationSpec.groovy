package org.jlibvips.operations

import org.jlibvips.jna.VipsBindingsSingleton
import spock.lang.Specification
import org.jlibvips.VipsImage

import java.nio.file.Files

import static org.jlibvips.TestUtils.*

class InsertOperationSpec extends Specification {

    def "Should insert a 500x500 JPEG into a full HD png on it's lower right corner."() {
        given:
        def baseImagePath = copyResourceToFS("1920x1080.png")
        def smallImagePath = copyResourceToFS("500x500.jpg")
        when:
        def smallImage = VipsImage.fromFile smallImagePath
        def baseImage = VipsImage.fromFile baseImagePath

        int width = (baseImage.width - smallImage.height/2).toInteger()
        int height = (baseImage.height - smallImage.height/2).toInteger()
        def image = baseImage.insert(smallImage)
            .x(width)
            .y(height)
            .expand(true)
            .background([0.0f, 0.0f, 0.0f])
            .create()
        then:
        image != null
        image.width == 2170
        image.height == 1330
        cleanup:
        smallImage.unref()
        baseImage.unref()
        Files.deleteIfExists baseImagePath
        Files.deleteIfExists smallImagePath
    }

}
