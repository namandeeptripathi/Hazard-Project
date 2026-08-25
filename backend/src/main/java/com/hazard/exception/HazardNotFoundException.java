package com.hazard.exception;

/**
 * Exception thrown when a requested hazard event or record cannot be found.
 */
public class HazardNotFoundException extends RuntimeException {

    public HazardNotFoundException(String message) {
        super(message);
    }
}
