// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import ballerina/http;
import ballerina/log;
import bfsi_payment_initiation_api.'client;
import bfsi_payment_initiation_api.interceptor;
import bfsi_payment_initiation_api.model;
import bfsi_payment_initiation_api.util;

// Request interceptors handle HTTP requests globally 
final interceptor:RequestInterceptor requestInterceptor = new;
final interceptor:RequestErrorInterceptor requestErrorInterceptor = new;
final interceptor:ResponseErrorInterceptor responseErrorInterceptor = new;

http:ListenerConfiguration config = {
    interceptors: [requestInterceptor, requestErrorInterceptor, responseErrorInterceptor]
};
listener http:Listener interceptorListener = new (9090, config);

service / on interceptorListener {

    private final 'client:PaymentClient paymentClient = new ();

    # Create a domestic payment
    #
    # + return - DomesticPaymentResponse object if successful else returns error
    isolated resource function post domestic\-payments(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string x\-idempotency\-key, @http:Header string x\-jws\-signature,
            @http:Header string? x\-customer\-user\-agent,
                @http:Payload {
                    mediaType: ["application/json", "application/jose+jwe", "application/json; charset=utf-8"]
                } model:DomesticPaymentRequest payload
        )
        returns model:DomesticPaymentResponse|error {

        log:printDebug("Initiating a domestic payment for idempotent request", 
                                        idempotencyKey = x\-idempotency\-key);
        check self.validatePayload(payload, util:DOMESTIC_PAYMENT);
        return self.paymentClient->/domestic\-payments.post(payload);
    }

    # Get Domestic Payment by payment ID
    #
    # + domesticPaymentId - the input payment ID 
    # + return - Domestic Payment
    isolated resource function get domestic\-payments/[string domesticPaymentId](@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent)
        returns model:DomesticPaymentResponse|error {

        log:printDebug("Retriveing Domestic Payment ", paymentId = domesticPaymentId);
        return self.paymentClient->/domestic\-payments/[domesticPaymentId];
    }

    # Get Domestic Payment Details by payment ID
    #
    # + domesticPaymentId - the input payment ID 
    # + return - Domestic Payment
    isolated resource function get domestic\-payments/[string domesticPaymentId]/payment\-details(
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printDebug("Retriveing Domestic Payment Details", paymentId = domesticPaymentId);
        return self.paymentClient->/payments\-details/domestic\-payments/[domesticPaymentId];
    }

    # Create a domestic scheduled payment
    #
    # + return - DomesticScheduledPaymentResponse object if successful else returns error
    isolated resource function post domestic\-scheduled\-payments(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string x\-idempotency\-key, @http:Header string x\-jws\-signature,
            @http:Header string? x\-customer\-user\-agent,
                @http:Payload {
                    mediaType: ["application/json", "application/jose+jwe", "application/json; charset=utf-8"]
                } model:DomesticScheduledPaymentRequest payload
        )
        returns model:DomesticScheduledPaymentResponse|error {

        log:printDebug("Initiating a domestic scheduled payment for idempotent request", 
                                        idempotencyKey = x\-idempotency\-key);
        check self.validatePayload(payload, util:DOMESTIC_SCHEDULED_PAYMENT);
        return self.paymentClient->/domestic\-scheduled\-payments.post(payload);
    }

    # Get Domestic Scheduled Payment by payment ID
    #
    # + domesticScheduledPaymentId - the input payment ID
    # + return - Domestic Scheduled Payment
    isolated resource function get domestic\-scheduled\-payments/[string domesticScheduledPaymentId](
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
        returns model:DomesticScheduledPaymentResponse|error {

        log:printDebug("Retriveing Domestic Scheduled Payment", paymentId = domesticScheduledPaymentId);
        return self.paymentClient->/domestic\-scheduled\-payments/[domesticScheduledPaymentId];
    }

    # Get Domestic Scheduled Payment Details by payment ID
    #
    # + domesticScheduledPaymentId - the input payment ID
    # + return - Domestic Scheduled Payment
    isolated resource function get domestic\-scheduled\-payments/[string domesticScheduledPaymentId]/payment\-details(
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printDebug("Retriveing Domestic Scheduled Payment Details", paymentId = domesticScheduledPaymentId);
        return self.paymentClient->/payments\-details/domestic\-scheduled\-payments/[domesticScheduledPaymentId];
    }

    # Create a domestic standing order
    #
    # + return - DomesticStandingOrderResponse object if successful else returns error
    isolated resource function post domestic\-standing\-orders(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string x\-idempotency\-key, @http:Header string x\-jws\-signature,
            @http:Header string? x\-customer\-user\-agent,
            @http:Payload {
                mediaType: ["application/json; charset=utf-8", "application/json", "application/jose+jwe"]
            } model:DomesticStandingOrderRequest payload)
        returns model:DomesticStandingOrderResponse|error {

        log:printDebug("Initiating a domestic standing order payment for idempotent request", 
                                        idempotencyKey = x\-idempotency\-key);
        check self.validatePayload(payload, util:DOMESTIC_STANDING_ORDER_PAYMENT);
        return self.paymentClient->/domestic\-standing\-orders.post(payload);
    }

    # Get Domestic Standing Order by payment ID
    #
    # + domesticStandingOrderId - the input payment ID
    # + return - Domestic Standing Order
    isolated resource function get domestic\-standing\-orders/[string domesticStandingOrderId](
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
        returns model:DomesticStandingOrderResponse|error {

        log:printDebug("Retriveing Domestic Standing Order", paymentId = domesticStandingOrderId);
        return self.paymentClient->/domestic\-standing\-orders/[domesticStandingOrderId];
    }

    # Get Domestic Standing Order Details by payment ID
    #
    # + domesticStandingOrderId - the input payment ID
    # + return - Domestic Standing Order
    isolated resource function get domestic\-standing\-orders/[string domesticStandingOrderId]/payment\-details(
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printDebug("Retriveing Domestic Standing Order Details", paymentId = domesticStandingOrderId);
        return self.paymentClient->/payments\-details/domestic\-standing\-orders/[domesticStandingOrderId];
    }

    # Create a file payment
    #
    # + return - FilePaymentResponse object if successful else returns error
    isolated resource function post file\-payments(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string x\-idempotency\-key, @http:Header string x\-jws\-signature,
            @http:Header string? x\-customer\-user\-agent,
            @http:Payload {
                mediaType: ["application/json; charset=utf-8", "application/json", "application/jose+jwe"]
            } model:FilePaymentRequest payload)
        returns model:FilePaymentResponse|error {

        log:printDebug("Initiating a file payment for idempotent request", 
                                        idempotencyKey = x\-idempotency\-key);
        check self.validatePayload(payload, util:FILE_PAYMENT);
        return self.paymentClient->/file\-payments.post(payload);
    }

    # Get File Payment by payment ID
    #
    # + filePaymentId - the input payment ID
    # + return - File Payment
    isolated resource function get file\-payments/[string filePaymentId](@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent)
        returns model:FilePaymentResponse|error {

        log:printDebug("Retriveing File Payment", paymentId = filePaymentId);
        return self.paymentClient->/file\-payments/[filePaymentId];
    }

    # Get File Payment Details by payment ID
    #
    # + filePaymentId - the input payment ID
    # + return - File Payment
    isolated resource function get file\-payments/[string filePaymentId]/payment\-details(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printDebug("Retriveing File Payment Details", paymentId = filePaymentId);
        return self.paymentClient->/payments\-details/file\-payments/[filePaymentId];
    }

    # Create an international payment
    #
    # + return - InternationalPaymentResponse object if successful else returns error
    isolated resource function post international\-payments(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string x\-idempotency\-key, @http:Header string x\-jws\-signature,
            @http:Header string? x\-customer\-user\-agent,
            @http:Payload {
                mediaType: ["application/json; charset=utf-8", "application/json", "application/jose+jwe"]
            } model:InternationalPaymentRequest payload)
        returns model:InternationalPaymentResponse|error {

        log:printDebug("Initiating a international payment for idempotent request", 
                                        idempotencyKey = x\-idempotency\-key);
        check self.validatePayload(payload, util:INTERNATIONAL_PAYMENT);
        return self.paymentClient->/international\-payments.post(payload);
    }

    # Get International Payment by payment ID
    #
    # + internationalPaymentId - the input payment ID
    # + return - International Payment
    isolated resource function get international\-payments/[string internationalPaymentId](@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string? x\-customer\-user\-agent)
        returns model:InternationalPaymentResponse|error {

        log:printDebug("Retriveing International Payment", paymentId = internationalPaymentId);
        return self.paymentClient->/international\-payments/[internationalPaymentId];
    }

    # Get International Payment Details by payment ID
    #
    # + internationalPaymentId - the input payment ID
    # + return - International Payment
    isolated resource function get international\-payments/[string internationalPaymentId]/payment\-details(
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printDebug("Retriveing International Payment Details", paymentId = internationalPaymentId);
        return self.paymentClient->/payments\-details/international\-payments/[internationalPaymentId];
    }

    # Create an international scheduled payment
    #
    # + return - InternationalScheduledPaymentResponse object if successful else returns error
    isolated resource function post international\-scheduled\-payments(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string x\-idempotency\-key, @http:Header string x\-jws\-signature,
            @http:Header string? x\-customer\-user\-agent,
            @http:Payload {
                mediaType: ["application/json", "application/jose+jwe", "application/json; charset=utf-8"]
            } model:InternationalScheduledPaymentRequest payload)
        returns model:InternationalScheduledPaymentResponse|error {

        log:printDebug("Initiating a International scheduled payment for idempotent request", 
                                        idempotencyKey = x\-idempotency\-key);
        check self.validatePayload(payload, util:INTERNATIONAL_SCHEDULED_PAYMENT);
        return self.paymentClient->/international\-scheduled\-payments.post(payload);
    }

    # Get International Scheduled Payment by payment ID
    #
    # + internationalScheduledPaymentId - the input payment ID
    # + return - International Scheduled Payment
    isolated resource function get international\-scheduled\-payments/[string internationalScheduledPaymentId](
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
        returns model:InternationalScheduledPaymentResponse|error {

        log:printDebug("Retriveing International Scheduled Payment", paymentId = internationalScheduledPaymentId);
        return self.paymentClient->/international\-scheduled\-payments/[internationalScheduledPaymentId];
    }

    # Get International Scheduled Payment Details by payment ID
    #
    # + internationalScheduledPaymentId - the input payment ID
    # + return - International Scheduled Payment
    isolated resource function get international\-scheduled\-payments/[string internationalScheduledPaymentId]/payment\-details(
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printDebug("Retriveing International Scheduled Payment Details", paymentId = internationalScheduledPaymentId);
        return self.paymentClient->/payments\-details/international\-scheduled\-payments/[internationalScheduledPaymentId];
    }

    # Create an international standing order
    #
    # + return - InternationalStandingOrderResponse object if successful else returns error
    # + payload - the request payload
    isolated resource function post international\-standing\-orders(@http:Header string? x\-fapi\-auth\-date,
            @http:Header string? x\-fapi\-customer\-ip\-address, @http:Header string? x\-fapi\-interaction\-id,
            @http:Header string x\-idempotency\-key, @http:Header string x\-jws\-signature,
            @http:Header string? x\-customer\-user\-agent,
            @http:Payload {
                mediaType: ["application/json", "application/jose+jwe", "application/json; charset=utf-8"]
            } model:InternationalStandingOrderRequest payload)
        returns model:InternationalStandingOrderResponse|error {

        log:printDebug("Initiating a  International Standing Order payment for idempotent request", 
                                        idempotencyKey = x\-idempotency\-key);
        check self.validatePayload(payload, util:INTERNATIONAL_STANDING_ORDER_PAYMENT);
        return self.paymentClient->/international\-standing\-orders.post(payload);
    }

    # Get International Standing Order by payment ID
    #
    # + internationalStandingOrderPaymentId - the input payment ID
    # + return - International Standing Order
    isolated resource function get international\-standing\-orders/[string internationalStandingOrderPaymentId](
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
        returns model:InternationalStandingOrderResponse|error {

        log:printDebug("Retriveing International Standing Order", paymentId = internationalStandingOrderPaymentId);
        return self.paymentClient->/international\-standing\-orders/[internationalStandingOrderPaymentId];
    }

    # Get International Standing Order Details by payment ID
    #
    # + internationalStandingOrderPaymentId - the input payment ID
    # + return - International Standing Order
    isolated resource function get international\-standing\-orders/[string internationalStandingOrderPaymentId]/payment\-details(
            @http:Header string? x\-fapi\-auth\-date, @http:Header string? x\-fapi\-customer\-ip\-address,
            @http:Header string? x\-fapi\-interaction\-id, @http:Header string? x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printDebug("Retriveing International Standing Order Details", paymentId = internationalStandingOrderPaymentId);
        return self.paymentClient->/payments\-details/international\-standing\-orders/[internationalStandingOrderPaymentId];
    }

    # Validate the payload
    #
    # + payload - the payload object
    # + path - the path
    # + return - boolean
    private isolated function validatePayload(anydata payload, string path) returns model:InvalidPayloadError? {

        model:CreditorAccount|error creditorAccount = util:extractCreditorAccount(payload.toJson(), path);
        if !path.includes(util:FILE_PAYMENT) {
            if creditorAccount is error {
                return error("Creditor Account is missing", ErrorCode = util:CODE_FIELD_MISSING);
            }
            check util:validateCreditorAccount(creditorAccount);
        }

        model:DebtorAccount|error? debtorAccount = util:extractDebtorAccount(payload.toJson(), path);
        if debtorAccount is error {
            return error("Debtor Account is missing", ErrorCode = util:CODE_FIELD_MISSING);
        }
        if debtorAccount is () {
            return;
        }
        check util:validateDebtorAccount(debtorAccount);
    }
}
