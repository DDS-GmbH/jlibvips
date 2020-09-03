package org.jlibvips

import org.jlibvips.jna.VipsBindingsSingleton
import spock.lang.Specification

import java.nio.file.Files

import static TestUtils.*

class VipsImageSpec extends Specification {

    def setupSpec() {
        VipsBindingsSingleton.configure("/usr/local/lib/libvips.42.dylib")
    }

    def "get resolution of image"() {
        given: "a arbitrary image file"
        def file = copyResourceToFS(resource)
        when: "loading it as VipsImage"
        def image = VipsImage.fromFile(file)
        then: "the width and height should be as original"
        image.width == width
        image.height == height
        cleanup:
        image.unref()
        Files.deleteIfExists(file)
        where:
        resource      | width | height
        "500x500.jpg" | 500   | 500
    }

    def "loading vectorised PDFs"() {
        given: "a large vectorised PDF"
        def pdfFile = copyResourceToFS(pdfResource)
        when: "loading it as a vips image"
        def image = VipsImage.fromPdf(pdfFile, pageNumber)
        then: "the height should not exceed the limits imposed by libpoppler (32767)"
        image.width <= VipsImage.POPPLER_CAIRO_LIMIT
        image.height <= VipsImage.POPPLER_CAIRO_LIMIT
        cleanup:
        image.unref()
        Files.deleteIfExists(pdfFile)
        where:
        pdfResource | pageNumber
        "1.pdf"     | 0
        "2page.pdf" | 1
    }

    def "get bands of image"() {
        given: "a arbitrary image file"
        def file = copyResourceToFS(resource)
        when: "loading it as VipsImage"
        def image = VipsImage.fromFile(file)
        then: "the number of bands is accessible"
        image.bands == bands
        cleanup:
        image.unref()
        Files.deleteIfExists(file)
        where:
        resource      | bands
        "500x500.jpg" | 1
    }

    def "creating image pyramids"() {
        given: "a VipsImage"
        def file = copyResourceToFS(resource)
        def image = VipsImage.fromFile(file)
        def outDir = newTempDir()
        when: "calling dzsave"
        image.deepZoom(outDir)
                .layout(layout)
                .background(background)
                .rotate(angle)
                .container(container)
                .tileSize(tileSize)
                .centre()
                .strip()
                .suffix(".jpg[Q=85]")
                .save()
        then: "generates an image pyramid on the file system"
        Files.exists outDir.resolve("0/0/0.jpg")
        cleanup:
        image.unref()
        Files.deleteIfExists(file)
        outDir.toFile().deleteDir()
        where:
        resource      | layout                 | background         | angle         | container                    | tileSize
        "500x500.jpg" | DeepZoomLayouts.Google | [0.0f, 0.0f, 0.0f] | VipsAngle.D90 | DeepZoomContainer.FileSystem | 256
    }

    def "thumbnail creation"() {
        given: "a VipsImage"
        def file = copyResourceToFS(resource)
        def image = VipsImage.fromFile(file)
        when: "calling .thumbnail()"
        def thumbnail = image.thumbnail(thumbnailWidth)
                .height(100)
                .autoRotate()
                .size(VipsSize.Nearest)
                .crop(VipsInteresting.Attention)
                .linear()
                .create()
        then: "the resulting image is resized to the specifications"
        thumbnail.width == thumbnailWidth
        thumbnail.height == thumbnailHeight
        cleanup:
        image.unref()
        thumbnail.unref()
        Files.deleteIfExists file
        where:
        resource      | thumbnailWidth | thumbnailHeight
        "500x500.jpg" | 100            | 100
    }

    def "to .jpg conversion"() {
        given: "a image"
        def file = copyResourceToFS(resource)
        def image = VipsImage.fromFile(file)
        when: "calling .jpeg()"
        def jpegFile = image.jpeg()
                .strip()
                .interlalce()
                .noSubsample()
                .optimizeCoding()
                .overshootDeringing()
                .quality(100)
                .trellisQuant()
                .save()
        then: "the resulting image is stored as JPEG to a temporary file location"
        Files.exists jpegFile
        cleanup:
        image.unref()
        Files.deleteIfExists file
        Files.deleteIfExists jpegFile
        where:
        resource << ["900x700.png"]
    }

    def ".jpg to .webp conversion"() {
        given: "a JPEG image"
        def file = copyResourceToFS(resource)
        def image = VipsImage.fromFile(file)
        when: "calling .webp()"
        def webpFile = image.webp()
                .quality(100)
                .lossless()
                .smartSubsample()
                .alphaQuality(100)
                .strip()
                .save()
        then: "the image is stored as WEBP to a temporary file location"
        Files.exists webpFile
        cleanup:
        image.unref()
        Files.deleteIfExists file
        Files.deleteIfExists webpFile
        where:
        resource << ["500x500.jpg"]
    }

    def ".jpg to .v conversion"() {
        given: "a JPEG image"
        def file = copyResourceToFS(resource)
        def image = VipsImage.fromFile(file)
        when: "calling .v()"
        def vFile = image.v()
                .save()
        then: "the image is stored in Vips Image Format to a temporary file location"
        Files.exists vFile
        cleanup:
        image.unref()
        Files.deleteIfExists file
        Files.deleteIfExists vFile
        where:
        resource << ["500x500.jpg"]
    }

    def "extract centre square from jpeg"() {
        given: "a JPEG image"
        def file = copyResourceToFS(resource)
        def image = VipsImage.fromFile(file)
        when: "calling .extractArea(...)"
        def area = image.extractArea(125, 125, 250, 250)
        then: "expect a new image with size 250x250"
        area.width == 250
        area.height == 250
        cleanup:
        image.unref()
        area.unref()
        where:
        resource << ["500x500.jpg"]
    }

    def "resize a jpeg to 100x50 pixel"() {
        given: "a JPEG image"
        def file = copyResourceToFS(resource)
        def image = VipsImage.fromFile(file)
        when: "calling .resize(...)"
        def resizedImage = image.resize(0.2)
                .verticalScale(0.1)
                .kernel(VipsKernel.Nearest)
                .create()
        then: "expect a image with size 100x50"
        resizedImage.width == 100
        resizedImage.height == 50
        cleanup:
        image.unref()
        resizedImage.unref()
        where:
        resource << ["500x500.jpg"]
    }

    def "embed a jpeg into a larger image"() {
        given: "a jpeg image"
        def file = copyResourceToFS(resource)
        def image = VipsImage.fromFile(file)
        when: "calling .embed(...)"
        def extendedImage = image.embed(0, 0, 1000, 1000)
                .background([0.0f,0.0f,0.0f])
                .extend(VipsExtend.Background)
                .create()
        then: "expect an image with size 1000x1000"
        extendedImage.width == 1000
        extendedImage.height == 1000
        cleanup:
        image.unref()
        extendedImage.unref()
        where:
        resource << ["500x500.jpg"]
    }

}
