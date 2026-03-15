package com.wso2.openbanking.demo.exceptions;

/**
 * Exception thrown when an error occurs while creating the SSL context
 * during open banking operations.
 */
public class SSLContextCreationException extends Exception {

    /**
     * Constructs a new SSLContextCreationException with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public SSLContextCreationException(String message) {
        super(message);
    }

    /**
     * Constructs a new SSLContextCreationException with the specified detail message and cause.
     *
     * @param message the detail message describing the cause of the exception
     * @param cause   the underlying cause of this exception
     */
    public SSLContextCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
