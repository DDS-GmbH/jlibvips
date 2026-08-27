package org.jlibvips.exceptions;

public class VipsException extends RuntimeException {

    private final String operation;
    private final int returnValue;

    public VipsException(String operation, String message) {
        super(String.format("VipsBindings operations %s failed: %s".formatted(operation, message)));
        this.operation = operation;
        returnValue = -1;
    }

    public VipsException(String operation, int returnValue) {
        super(String.format("VipsBindings operation %s returned error code %d.", operation, returnValue));
        this.operation = operation;
        this.returnValue = returnValue;
    }

    public VipsException(String operation, Throwable cause) {
        super(String.format("VipsBindings operation %s failed with Exception.", operation), cause);
        this.operation = operation;
        this.returnValue = -1;
    }

    public String getOperation() {
        return operation;
    }

    public int getReturnValue() {
        return returnValue;
    }
}
