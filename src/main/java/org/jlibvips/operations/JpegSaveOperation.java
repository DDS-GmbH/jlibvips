package org.jlibvips.operations;

import com.sun.jna.Pointer;
import java.util.List;
import org.jlibvips.VipsImage;
import org.jlibvips.exceptions.VipsException;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.util.Varargs;
import org.jlibvips.util.VipsUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JpegSaveOperation implements SaveOperation {

    private final VipsImage image;

    private Integer quality;
    private Boolean optimizeCoding;
    private Boolean interlace;
    private Boolean strip;
    private Boolean noSubsample;
    private Boolean trellisQuant;
    private Boolean overshootDeringing;
    private Boolean optimizeScans;
    private Pointer background;

    public JpegSaveOperation(VipsImage image) {
        this.image = image;
    }

    @Override
    public Path save() throws IOException, VipsException {
        Path path = Files.createTempFile("jlibvips", ".jpg");
        int ret = VipsBindingsSingleton.instance().vips_jpegsave(image.getPtr(), path.toString(),
                new Varargs().add("Q", quality)
                        .add("optimize_coding", VipsUtils.booleanToInteger(optimizeCoding))
                        .add("interlace", VipsUtils.booleanToInteger(interlace))
                        .add("strip", VipsUtils.booleanToInteger(strip))
                        .add("no_subsample", VipsUtils.booleanToInteger(noSubsample))
                        .add("trellis_quant", VipsUtils.booleanToInteger(trellisQuant))
                        .add("overshoot_deringing", VipsUtils.booleanToInteger(overshootDeringing))
                        .add("optimize_scans", VipsUtils.booleanToInteger(optimizeScans))
                        .add("background", background).toArray());
        if(ret != 0) {
            throw new VipsException("vips_jpegsave", ret);
        }
        return path;
    }

    /**
     * Set the quality factor.
     *
     * @param q quality factor
     * @return this
     */
    public JpegSaveOperation quality(Integer q) {
        this.quality = q;
        return this;
    }

    /**
     * Compute optimal Huffman coding tables.
     *
     * @return this
     */
    public JpegSaveOperation optimizeCoding() {
        this.optimizeCoding = true;
        return this;
    }

    /**
     * Write an interlaced (progressive) jpeg
     *
     * @return this
     */
    public JpegSaveOperation interlalce() {
        this.interlace = true;
        return this;
    }

    /**
     * Remove all metadata from image.
     *
     * @return this
     */
    public JpegSaveOperation strip() {
        this.strip = true;
        return this;
    }

    /**
     * Disable chroma subsampling.
     *
     * @return this
     */
    public JpegSaveOperation noSubsample() {
        this.noSubsample = true;
        return this;
    }

    /**
     * Apply trellis quantisation to each 8x8 block.
     *
     * @return this
     */
    public JpegSaveOperation trellisQuant() {
        this.trellisQuant = true;
        return this;
    }

    /**
     * Overshoot samples with extreme values.
     *
     * @return this
     */
    public JpegSaveOperation overshootDeringing() {
        this.overshootDeringing = true;
        return this;
    }

    /**
     * Split DCT coefficients into separate scans
     *
     * @return this
     */
    public JpegSaveOperation optimizeScans() {
        this.optimizeScans = true;
        return this;
    }

    public JpegSaveOperation background(List<Float> background) {
        return background(background != null? background.stream().mapToDouble(Float::doubleValue).toArray() : null);
    }

    public JpegSaveOperation background(double[] background) {
        this.background = background != null?
            VipsUtils.toPointer(background) : null;
        return this;
    }
}
