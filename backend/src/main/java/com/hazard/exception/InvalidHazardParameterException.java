package com.hazard.exception;

/**
 * Exception thrown when invalid parameters (e.g. coordinates, date ranges, hazard types)
 * are supplied to hazard retrieval or query operations.
 */
public class InvalidHazardParameterException extends RuntimeException {

    public InvalidHazardParameterException(String message) {
        super(message);
    }
}
