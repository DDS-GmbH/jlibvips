package org.jlibvips.operations;

import com.sun.jna.Pointer;
import org.jlibvips.VipsImage;
import org.jlibvips.VipsInteresting;
import org.jlibvips.VipsSize;
import org.jlibvips.exceptions.FailedOnThumbnailException;
import org.jlibvips.jna.VipsBindingsSingleton;
import org.jlibvips.util.Varargs;

import static org.jlibvips.util.VipsUtils.booleanToInteger;
import static org.jlibvips.util.VipsUtils.toOrdinal;

/**
 * Make a thumbnail from a file. Shrinking is done in three stages: using any shrink-on-load features available in the
 * file import library, using a block shrink, and using a {@link VipsSize#LANCZOS3} shrink. At least the final 200% is
 * done with {@link VipsSize#LANCZOS3}. The output should be high quality, and the operation should be quick.
 *
 * @author amp
 * @see <a href="http://libvips.github.io/libvips/API/current/libvips-resample.html#vips-thumbnail">vips_thumbnail()</a>
 * @see <a href="http://libvips.github.io/libvips/API/current/libvips-resample.html#vips-thumbnail-image">vips_thumbnail_image()</a>
 */
public class ThumbnailOperation {

    private final VipsImage image;
    private final int width;

    private Integer height;
    private Boolean autoRotate;
    private VipsSize size;
    private VipsInteresting crop;
    private Boolean linear;


    public ThumbnailOperation(VipsImage image, int width) {
        this.image = image;
        this.width = width;
    }

    /**
     * Creates the thumbnail {@link VipsImage} based on the operation configuration.
     *
     * @return new {@link VipsImage}
     */
    public VipsImage create() {
        Pointer[] pointers = new Pointer[1];
        int ret = VipsBindingsSingleton.instance().vips_thumbnail_image(image.getPtr(), pointers, width,
                new Varargs().add("auto_rotate", booleanToInteger(autoRotate))
                        .add("height", height)
                        .add("size", toOrdinal(size))
                        .add("crop", toOrdinal(crop))
                        .add("linear", booleanToInteger(linear))
                        .toArray());
        if(ret != 0) {
            throw new FailedOnThumbnailException(ret);
        }
        return new VipsImage(pointers[0]);
    }

    /**
     * target height in pixels
     *
     * @param height target height in pixels
     * @return this
     */
    public ThumbnailOperation height(int height) {
        this.height = height;
        return this;
    }

    /**
     * Normally any orientation tags on the input image (such as EXIF tags) are interpreted to rotate the image upright.
     * If you set auto_rotate to FALSE, these tags will not be interpreted.
     *
     * @return this
     */
    public ThumbnailOperation autoRotate() {
        this.autoRotate = true;
        return this;
    }

    public ThumbnailOperation size(VipsSize size) {
        this.size = size;
        return this;
    }

    /**
     * If you set crop , then the output image will fill the whole of the width x height rectangle, with any excess
     * cropped away.
     *
     * @param crop {@link }
     * @return this
     */
    public ThumbnailOperation crop(VipsInteresting crop) {
        this.crop = crop;
        return this;
    }

    /**
     * Shrinking is normally done in sRGB colourspace. Set linear to shrink in linear light colourspace instead. This
     * can give better results, but can also be far slower, since tricks like JPEG shrink-on-load cannot be used in
     * linear space.
     *
     * @return this
     */
    public ThumbnailOperation linear() {
        this.linear = true;
        return this;
    }
}
