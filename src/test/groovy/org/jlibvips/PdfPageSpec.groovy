package org.jlibvips

import com.sun.jna.Pointer
import org.jlibvips.exceptions.CouldNotLoadPdfVipsException
import org.jlibvips.jna.VipsBindingsSingleton
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

import static org.jlibvips.TestUtils.copyResourceToFS
import static org.jlibvips.TestUtils.pdf

/**
 * PdfPage must settle on the same scale and size as the load-at-every-step search it replaces, in at most two loads,
 * without leaving pages in the libvips operation cache.
 */
class PdfPageSpec extends Specification {

    static final int LIMIT = VipsImage.POPPLER_CAIRO_LIMIT

    List<Path> files = []
    List<VipsImage> images = []
    List<LogRecord> records = []
    Logger logger = Logger.getLogger(PdfPage.name)
    Handler handler = new Handler() {
        void publish(LogRecord record) { records << record }
        void flush() {}
        void close() {}
    }

    def setup() {
        logger.level = Level.ALL
        logger.useParentHandlers = false
        logger.addHandler(handler)
    }

    def cleanup() {
        logger.removeHandler(handler)
        logger.useParentHandlers = true
        logger.level = null
        images.each { it.unref() }
        files.each { Files.deleteIfExists(it) }
    }

    def "renders a page that fits at the scale asked for, in one load"() {
        given: "an A4 page, 595.28 x 841.89 pt"
        def file = keep(copyResourceToFS("1.pdf"))

        when:
        def page = keep(PdfPage.load(file, 0))

        then: "it is rendered at the maximum scale, 6"
        page.scale() == PdfPage.MAX_SCALE
        page.image().width == 3572
        page.image().height == 5051

        and: "with a single load"
        loads() == 1
    }

    def "renders a #width x #height pt page at #expected, the scale the load-at-every-step search settles on"() {
        given:
        def file = keep(pdf(width: width, height: height))
        def searched = searchedAtEveryStep(file, 0, PdfPage.MAX_SCALE)

        when:
        def page = keep(PdfPage.load(file, 0))

        then: "the same scale and size as the search that loaded the page at every step"
        page.scale() == searched.scale
        page.image().width == searched.width
        page.image().height == searched.height
        page.image().width <= LIMIT
        page.image().height <= LIMIT

        and: "which is the expected scale"
        Math.abs(page.scale() - expected) < 1e-4

        and: "in no more loads than the probe, the estimated scale and, if the estimate was a pixel short, one more"
        loads() == expectedLoads
        searched.loads == searchedLoads

        where:
        width   | height  | expected | expectedLoads | searchedLoads
        4149.92 | 2383.94 | 6.0f     | 1             | 1  // 24900 px: fits right away
        5461.1  | 100     | 6.0f     | 1             | 1  // 32766.6 -> 32767 px: just fits
        5461.25 | 100     | 5.9f     | 2             | 2  // 32767.5 -> 32768 px: rounds to even, one too many
        5461.3  | 100     | 5.9f     | 2             | 2  // 32767.8 -> 32768 px
        100     | 5461.6  | 5.9f     | 2             | 2  // the height counts too
        9666.14 | 6122.83 | 3.3f     | 2             | 28 // a wide CAD plan: 3.4 renders 32865 px, 3.3 renders 31898 px
        5553.9  | 100     | 5.8f     | 3             | 3  // the estimate for 5.9 says 32767 px, the real header 32768
        20000   | 20000   | 1.6f     | 2             | 45 // 44 steps down
    }

    def "does not leave the loaded page in the libvips operation cache, unlike a plain pdfload"() {
        given: "budgets that the images other specs leave behind cannot exhaust, or libvips would evict every load"
        def vips = VipsBindingsSingleton.instance()
        int max = vips.vips_cache_get_max()
        long maxMem = vips.vips_cache_get_max_mem()
        int maxFiles = vips.vips_cache_get_max_files()
        vips.vips_cache_set_max(max + 1000)
        vips.vips_cache_set_max_mem(Long.MAX_VALUE)
        vips.vips_cache_set_max_files(Integer.MAX_VALUE)

        and: "the small helper operations every PDF load shares, cached on first use"
        def file = keep(pdf(width: 9666.14, height: 6122.83))
        PdfPage.load(file, 0).image().unref()
        int before = vips.vips_cache_get_size()

        when: "loading and releasing the page"
        PdfPage.load(file, 0).image().unref()

        then: "the cache is as it was"
        vips.vips_cache_get_size() == before

        when: "loading and releasing it with the file loader jlibvips used before"
        Pointer[] out = new Pointer[1]
        vips.vips_pdfload(file.toString(), out, 'scale', 1.0f, 'page', 0, null)
        vips.g_object_unref(out[0])

        then: "the cache keeps that load"
        vips.vips_cache_get_size() == before + 1

        cleanup:
        vips.vips_cache_set_max(max)
        vips.vips_cache_set_max_mem(maxMem)
        vips.vips_cache_set_max_files(maxFiles)
    }

    def "loads from a buffer with the same result as from the file"() {
        given:
        def file = keep(pdf(width: 9666.14, height: 6122.83))
        def fromFile = keep(PdfPage.load(file, 0))

        when:
        def fromBuffer = keep(PdfPage.load(Files.readAllBytes(file), 0))

        then:
        fromBuffer.scale() == fromFile.scale()
        fromBuffer.image().width == fromFile.image().width
        fromBuffer.image().height == fromFile.image().height
    }

    def "renders pixels from a buffer after the array has been passed"() {
        given: "a blue page loaded from a buffer"
        def file = keep(pdf(width: 200, height: 100, content: '0 0 1 rg 0 0 200 100 re f'))
        def page = keep(PdfPage.load(Files.readAllBytes(file), 0, 1.0f))

        when: "rendering it"
        def thumbnail = keep(page.image().thumbnail(100).create())
        def png = keep(thumbnail.png().save())

        then:
        thumbnail.width == 100
        thumbnail.height == 50
        Files.size(png) > 0
    }

    def "sizes the page the way the renderers do: the crop box, rotated"() {
        given: "a rotated page whose crop box is smaller than its media box"
        def file = keep(pdf(width: 1000, height: 800, cropBox: [100, 100, 700, 500], rotate: 90))

        when:
        def page = keep(PdfPage.load(file, 0, 1.0f))

        then:
        page.image().width == 400
        page.image().height == 600
    }

    def "the page index selects the page"() {
        given: "a small first page and a wide second page"
        def file = keep(pdf([width: 500, height: 500], [width: 9666.14, height: 6122.83]))

        expect:
        keep(PdfPage.load(file, 0)).scale() == PdfPage.MAX_SCALE
        Math.abs(keep(PdfPage.load(file, 1)).scale() - 3.3f) < 1e-4
    }

    def "VipsImage's PDF loaders render at the fitting scale"() {
        given:
        def file = keep(pdf(width: 9666.14, height: 6122.83))
        def bytes = Files.readAllBytes(file)
        def searched = searchedAtEveryStep(file, 0, PdfPage.MAX_SCALE)

        when:
        def loaded = [VipsImage.fromPdf(file, 0), VipsImage.fromPdf(file, 0, 4.0f),
                      VipsImage.fromPdfBuffer(bytes, 0), VipsImage.fromPdfBufferFast(bytes, 0)]
        loaded.each { keep(it) }

        then:
        loaded.every { it.width == searched.width && it.height == searched.height }
    }

    def "reports pages out of range"() {
        given:
        def file = keep(pdf(width: 100, height: 100))

        when:
        PdfPage.load(file, 3)

        then:
        def e = thrown(CouldNotLoadPdfVipsException)
        e.message.contains('pages out of range')
    }

    def "rejects files that are no PDF"() {
        given:
        def file = keep(Files.createTempFile('jlibvips', '.pdf'))
        Files.writeString(file, 'not a pdf')

        when:
        PdfPage.load(file, 0)

        then:
        thrown(CouldNotLoadPdfVipsException)
    }

    def "rejects files that do not exist"() {
        when:
        PdfPage.load(Path.of('/nowhere/jlibvips.pdf'), 0)

        then:
        thrown(CouldNotLoadPdfVipsException)
    }

    def "rejects a scale of #scale"() {
        when:
        PdfPage.load(keep(pdf(width: 100, height: 100)), 0, scale)

        then:
        thrown(IllegalArgumentException)

        where:
        scale << [0f, -1f, Float.NaN]
    }

    def "gives up on a page that does not fit at any scale"() {
        given: "a page 700000 pt wide"
        def file = keep(pdf(width: 700000, height: 100))

        when:
        PdfPage.load(file, 0)

        then:
        def e = thrown(CouldNotLoadPdfVipsException)
        e.message.contains('does not fit')
    }

    private Path keep(Path file) {
        files << file
        return file
    }

    private PdfPage keep(PdfPage page) {
        images << page.image()
        return page
    }

    private VipsImage keep(VipsImage image) {
        images << image
        return image
    }

    private int loads() {
        return records.count { it.message.startsWith('pdfload_source') }
    }

    /**
     * The search jlibvips used to run: load the page at every step down from {@code maxScale} until it fits.
     */
    private static Map searchedAtEveryStep(Path file, int page, float maxScale) {
        def vips = VipsBindingsSingleton.instance()
        float scale = maxScale
        int loads = 0
        while (true) {
            Pointer[] out = new Pointer[1]
            assert vips.vips_pdfload(file.toString(), out, 'scale', scale, 'page', page, null) == 0
            loads++
            def image = new VipsImage(out[0])
            def result = [scale: scale, width: image.width, height: image.height, loads: loads]
            image.unref()
            if (result.width <= LIMIT && result.height <= LIMIT) {
                return result
            }
            scale = (float) (scale - 0.1f)
        }
    }
}
