package org.jlibvips.exceptions;

public class CouldNotLoadPdfVipsException extends VipsException {

    public CouldNotLoadPdfVipsException(int returnValue) {
        super("pdfload", returnValue);
    }

    public CouldNotLoadPdfVipsException(String message) {
        super("pdfload", message);
    }

}
