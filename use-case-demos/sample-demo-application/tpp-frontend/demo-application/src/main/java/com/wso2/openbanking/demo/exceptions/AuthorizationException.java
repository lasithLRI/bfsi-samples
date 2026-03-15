package com.wso2.openbanking.demo.exceptions;

/**
 * Exception thrown when an authorization failure occurs during open banking operations.
 * This exception signals issues such as invalid tokens, insufficient permissions,
 * or unauthorized access attempts.
 */
public class AuthorizationException extends Exception {

    /**
     * Constructs a new AuthorizationException with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public AuthorizationException(String message) {
        super(message);
    }

    /**
     * Constructs a new AuthorizationException with the specified detail message and cause.
     *
     * @param message the detail message describing the cause of the exception
     * @param cause   the underlying cause of this exception
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
