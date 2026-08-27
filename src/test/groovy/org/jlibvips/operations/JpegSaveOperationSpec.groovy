package org.jlibvips.operations

import org.jlibvips.VipsImage
import org.jlibvips.jna.VipsBindingsSingleton
import spock.lang.Specification

import javax.imageio.ImageIO
import java.nio.file.Files

import static org.jlibvips.TestUtils.copyResourceToFS

class JpegSaveOperationSpec extends Specification {

    def "Should save a PNG image as JPEG with white background for transparent areas."() {
        given:
        def baseImagePath = copyResourceToFS("100x100_transparent.png")
        def image = VipsImage.fromFile(baseImagePath)
        when:
        def path = image.jpeg()
            .background([255f, 255f, 255f])
            .save()
        then:
        // check weather first pixel is white
        def bufferedImage = ImageIO.read(path.toFile())
        def rgb = bufferedImage.getRGB(0, 0)
        isWhiteish(rgb)
        cleanup:
        bufferedImage = null
        if(image != null) image.unref()
        Files.deleteIfExists(baseImagePath)
    }

    /** Whether the pixel is white-ish (all channels ≥ 200), i.e. clearly not a black background. */
    static boolean isWhiteish(int argb) {
        def r = (argb >> 16) & 0xFF
        def g = (argb >> 8) & 0xFF
        def b = argb & 0xFF
        return r >= 200 && g >= 200 && b >= 200
    }
}
