package org.mayukh.rivet.core;

/**
 * Created by mayukh42 on 6/19/2017.
 */
public class RivetException extends RuntimeException {

    public RivetException(String message) {
        super("[RivetError] " + message);
    }
}
