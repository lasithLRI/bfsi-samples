package com.wso2.openbanking.demo.exceptions;

/**
 * Exception thrown when an error occurs while loading bank information
 * during open banking operations.
 */
public class BankInfoLoadException extends Exception {

    /**
     * Constructs a new BankInfoLoadException with the specified detail message.
     *
     * @param message the detail message describing the cause of the exception
     */
    public BankInfoLoadException(String message) {
        super(message);
    }

    /**
     * Constructs a new BankInfoLoadException with the specified detail message and cause.
     *
     * @param message the detail message describing the cause of the exception
     * @param cause   the underlying cause of this exception
     */
    public BankInfoLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}