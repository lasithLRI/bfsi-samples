package com.wso2.openbanking.models;

import com.wso2.openbanking.models.Payment;

public class PaymentSuccessResponse {
    private int status;
    private boolean success;
    private Payment payment;
    private String authorizationUrl;

    public PaymentSuccessResponse(int status, boolean success, Payment payment) {
        this.status = status;
        this.success = success;
        this.payment = payment;
    }

    public PaymentSuccessResponse(int status, boolean success, Payment payment, String authorizationUrl) {
        this.status = status;
        this.success = success;
        this.payment = payment;
        this.authorizationUrl = authorizationUrl;
    }

    // IMPORTANT: Add all getters for JSON serialization
    public int getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return success;
    }

    public Payment getPayment() {
        return payment;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }
}
