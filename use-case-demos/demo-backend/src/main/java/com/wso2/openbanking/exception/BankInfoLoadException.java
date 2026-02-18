package com.wso2.openbanking.exception;

public class BankInfoLoadException extends Exception {

  public BankInfoLoadException(String message) {
    super(message);
  }

  public BankInfoLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}
