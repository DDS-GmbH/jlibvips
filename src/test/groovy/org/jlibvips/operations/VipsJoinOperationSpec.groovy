package org.jlibvips.operations

import org.jlibvips.TestUtils
import org.jlibvips.VipsDirection
import org.jlibvips.VipsImage
import org.jlibvips.jna.VipsBindingsSingleton
import spock.lang.Specification

import java.nio.file.Files

class VipsJoinOperationSpec extends Specification {

    def "Join images together."() {
        given:
        def if1 = TestUtils.copyResourceToFS(in1)
        def image1 = VipsImage.fromFile if1
        def if2 = TestUtils.copyResourceToFS(in2)
        def image2 = VipsImage.fromFile if2
        when:
        def image = image1.join(image2)
            .direction(dir)
            .create()
        then:
        image != null
        cleanup:
        image1.unref()
        image2.unref()
        image.unref()
        Files.deleteIfExists if1
        Files.deleteIfExists if2
        where:
        in1           | in2           | dir
        "500x500.jpg" | "500x500.jpg" | VipsDirection.Horizontal
        "500x500.jpg" | "500x500.jpg" | VipsDirection.Vertical
        "500x500.jpg" | "900x700.png" | VipsDirection.Vertical
    }

}
