package org.jlibvips

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class TestUtils {

    static Path copyResourceToFS(String resourceName) {
        def tempFile = Files.createTempFile("jlibvips", "." + resourceName.replaceFirst(~/\.[^\.]+$/, ''))
        tempFile.toFile().withDataOutputStream { DataOutputStream os ->
            TestUtils.class.getResource("/$resourceName").withInputStream { is ->
                os << is
            }
        }
        return tempFile
    }

    static Path newTempDir() {
        return Files.createTempDirectory("jlibvips")
    }

    static Path copyStringToFS(String val, String extension) {
        def tempFile = Files.createTempFile "jlivips", ".$extension"
        Files.writeString tempFile, val
        return tempFile
    }

    /**
     * Writes a minimal PDF, one page per map: width and height in points, optionally cropBox [x0, y0, x1, y1],
     * rotate in degrees and content, a content stream.
     */
    static Path pdf(Map... pages) {
        List<String> objects = []
        objects << "<< /Type /Catalog /Pages 2 0 R >>"
        def kids = (0..<pages.size()).collect { "${3 + 2 * it} 0 R" }.join(' ')
        objects << "<< /Type /Pages /Kids [$kids] /Count ${pages.size()} >>"
        pages.each { page ->
            int contents = objects.size() + 2
            def dict = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ${page.width} ${page.height}]"
            if (page.cropBox) {
                dict += " /CropBox [${page.cropBox.join(' ')}]"
            }
            if (page.rotate) {
                dict += " /Rotate ${page.rotate}"
            }
            objects << "$dict /Contents $contents 0 R >>"
            String content = page.content ?: ''
            objects << "<< /Length ${content.length()} >>\nstream\n${content}\nendstream"
        }
        def out = new StringBuilder("%PDF-1.4\n")
        List<Integer> offsets = []
        objects.eachWithIndex { body, i ->
            offsets << out.length()
            out << "${i + 1} 0 obj\n${body}\nendobj\n"
        }
        int xref = out.length()
        out << "xref\n0 ${objects.size() + 1}\n0000000000 65535 f \n"
        offsets.each { out << String.format("%010d 00000 n \n", it) }
        out << "trailer\n<< /Size ${objects.size() + 1} /Root 1 0 R >>\nstartxref\n${xref}\n%%EOF\n"
        def file = Files.createTempFile("jlibvips", ".pdf")
        Files.write(file, out.toString().getBytes(StandardCharsets.US_ASCII))
        return file
    }

    private TestUtils() {
    }

}
