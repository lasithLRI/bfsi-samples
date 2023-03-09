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
import bfsi_payment_initiation_api.interceptor;
import bfsi_payment_initiation_api.payload.validator;
import bfsi_payment_initiation_api.'client;
import bfsi_payment_initiation_api.model;

// Request interceptors handle HTTP requests globally 
interceptor:RequestInterceptor requestInterceptor = new;
interceptor:RequestErrorInterceptor requestErrorInterceptor = new;
interceptor:ResponseErrorInterceptor responseErrorInterceptor = new;

http:ListenerConfiguration config = {
    host: "localhost",
    interceptors: [requestInterceptor, requestErrorInterceptor, responseErrorInterceptor]
};
listener http:Listener interceptorListener = new (9090, config);

service / on interceptorListener {

    private final 'client:PaymentClient paymentClient = new();

    # Create a domestic payment
    #
    # + return - DomesticPaymentResponse object if successful else returns error
    isolated resource function post domestic\-payments(@http:Header string? 'x\-fapi\-auth\-date,
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id,
            @http:Header string 'x\-idempotency\-key, @http:Header string 'x\-jws\-signature,
            @http:Header string? 'x\-customer\-user\-agent,
                @http:Payload {
                    mediaType: ["application/json", "application/jose+jwe", "application/json; charset=utf-8"]
                } model:DomesticPaymentRequest payload
        )
        returns model:DomesticPaymentResponse|error {

        log:printInfo("Initiating a domestic payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "domestic-payments");
        if result is error {
            return result;
        }
        model:DomesticPaymentResponse|error response = self.paymentClient->/domestic\-payments.post(payload);
        return response;
    }

    # Get Domestic Payment by payment ID
    #
    # + domesticPaymentId - the input payment ID 
    # + return - Domestic Payment
    isolated resource function get 'domestic\-payments/[string domesticPaymentId](@http:Header string? 'x\-fapi\-auth\-date,
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id,
            @http:Header string? 'x\-customer\-user\-agent)
        returns model:DomesticPaymentResponse|error {

        log:printInfo("Retriveing Domestic Payment for payment ID: " + domesticPaymentId);
        model:DomesticPaymentResponse|error response = self.paymentClient->/domestic\-payments/[domesticPaymentId];

        return response;
    }

    # Get Domestic Payment Details by payment ID
    #
    # + domesticPaymentId - the input payment ID 
    # + return - Domestic Payment
    isolated resource function get domestic\-payments/[string domesticPaymentId]/'payment\-details(
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address,
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printInfo("Retriveing Domestic Payment Details for payment ID: " + domesticPaymentId);
        model:PaymentDetailsResponse|error response = 
                            self.paymentClient->/payments\-details/domestic\-payments/[domesticPaymentId];

        return response;
    }

    # Create a domestic scheduled payment
    #
    # + return - DomesticScheduledPaymentResponse object if successful else returns error
    isolated resource function post 'domestic\-scheduled\-payments(@http:Header string? 'x\-fapi\-auth\-date,
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id,
            @http:Header string 'x\-idempotency\-key, @http:Header string 'x\-jws\-signature,
            @http:Header string? 'x\-customer\-user\-agent,
                @http:Payload {
                    mediaType: ["application/json", "application/jose+jwe", "application/json; charset=utf-8"]
                } model:DomesticScheduledPaymentRequest payload
        )
        returns model:DomesticScheduledPaymentResponse|error {

        log:printInfo("Initiating a domestic scheduled payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "domestic-scheduled-payments");
        if result is error {
            return result;
        }

        model:DomesticScheduledPaymentResponse|error response =
                        self.paymentClient->/domestic\-scheduled\-payments.post(payload);
        return response;
    }

    # Get Domestic Scheduled Payment by payment ID
    #
    # + domesticScheduledPaymentId - the input payment ID
    # + return - Domestic Scheduled Payment
    isolated resource function get domestic\-scheduled\-payments/[string domesticScheduledPaymentId](
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address,
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent)
        returns model:DomesticScheduledPaymentResponse|error {

        log:printInfo("Retriveing Domestic Scheduled Payment for payment ID: " + domesticScheduledPaymentId);
        model:DomesticScheduledPaymentResponse|error response = 
                            self.paymentClient->/domestic\-scheduled\-payments/[domesticScheduledPaymentId];
        
        return response;
    }

    # Get Domestic Scheduled Payment Details by payment ID
    #
    # + domesticScheduledPaymentId - the input payment ID
    # + return - Domestic Scheduled Payment
    isolated resource function get domestic\-scheduled\-payments/[string domesticScheduledPaymentId]/'payment\-details(
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address,
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printInfo("Retriveing Domestic Scheduled Payment Details for payment ID: " + domesticScheduledPaymentId);
        model:PaymentDetailsResponse|error response =
                        self.paymentClient->/payments\-details/domestic\-scheduled\-payments/[domesticScheduledPaymentId];
        return response;
    }

    # Create a domestic standing order
    #
    # + return - DomesticStandingOrderResponse object if successful else returns error
    isolated resource function post domestic\-standing\-orders(@http:Header string? 'x\-fapi\-auth\-date,
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id,
            @http:Header string 'x\-idempotency\-key, @http:Header string 'x\-jws\-signature,
            @http:Header string? 'x\-customer\-user\-agent,
            @http:Payload {
                mediaType: ["application/json; charset=utf-8", "application/json", "application/jose+jwe"]
            } model:DomesticStandingOrderRequest payload)
        returns model:DomesticStandingOrderResponse|error {

        log:printInfo("Initiating a domestic scheduled payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "domestic-standing-orders");
        if result is error {
            return result;
        }

        model:DomesticStandingOrderResponse|error response =
                        self.paymentClient->/domestic\-standing\-orders.post(payload);
        return response;
    }

    # Get Domestic Standing Order by payment ID
    #
    # + domesticStandingOrderId - the input payment ID
    # + return - Domestic Standing Order
    isolated resource function get domestic\-standing\-orders/[string domesticStandingOrderId](
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address,
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent)
        returns model:DomesticStandingOrderResponse|error {

        log:printInfo("Retriveing Domestic Standing Order for payment ID: " + domesticStandingOrderId);
        model:DomesticStandingOrderResponse|error|model:PayloadParseError response =
                        self.paymentClient->/domestic\-standing\-orders/[domesticStandingOrderId];

        return response;
    }

    # Get Domestic Standing Order Details by payment ID
    #
    # + domesticStandingOrderId - the input payment ID
    # + return - Domestic Standing Order
    isolated resource function get domestic\-standing\-orders/[string domesticStandingOrderId]/'payment\-details(
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address,
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printInfo("Retriveing Domestic Standing Order Details for payment ID: " + domesticStandingOrderId);
        model:PaymentDetailsResponse|error response =
                        self.paymentClient->/payments\-details/domestic\-standing\-orders/[domesticStandingOrderId];
        return response;
    }

    # Create a file payment
    #
    # + return - FilePaymentResponse object if successful else returns error
    isolated resource function post file\-payments(@http:Header string? 'x\-fapi\-auth\-date,
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id,
            @http:Header string 'x\-idempotency\-key, @http:Header string 'x\-jws\-signature,
            @http:Header string? 'x\-customer\-user\-agent,
            @http:Payload {
                mediaType: ["application/json; charset=utf-8", "application/json", "application/jose+jwe"]
            } model:FilePaymentRequest payload)
        returns model:FilePaymentResponse|error {

        log:printInfo("Initiating a domestic scheduled payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "file-payments");
        if result is model:InvalidPayloadError {
            return result;
        }

        model:FilePaymentResponse|error response = self.paymentClient->/file\-payments.post(payload);

        return response;
    }

    # Get File Payment by payment ID
    #
    # + filePaymentId - the input payment ID
    # + return - File Payment
    isolated resource function get file\-payments/[string filePaymentId](@http:Header string? 'x\-fapi\-auth\-date,
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id,
            @http:Header string? 'x\-customer\-user\-agent)
        returns model:FilePaymentResponse|error {

        log:printInfo("Retriveing File Payment for payment ID: " + filePaymentId);
        model:FilePaymentResponse|error|model:PayloadParseError response =
                        self.paymentClient->/file\-payments/[filePaymentId];

        return response;
    }

    # Get File Payment Details by payment ID
    #
    # + filePaymentId - the input payment ID
    # + return - File Payment
    isolated resource function get file\-payments/[string filePaymentId]/'payment\-details(@http:Header string? 'x\-fapi\-auth\-date,
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id,
            @http:Header string? 'x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printInfo("Retriveing File Payment Details for payment ID: " + filePaymentId);
        model:PaymentDetailsResponse|error response =
                        self.paymentClient->/payments\-details/file\-payments/[filePaymentId];

        return response;
    }

    # Create an international payment
    #
    # + return - InternationalPaymentResponse object if successful else returns error
    isolated resource function post international\-payments(@http:Header string? 'x\-fapi\-auth\-date,
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id,
            @http:Header string 'x\-idempotency\-key, @http:Header string 'x\-jws\-signature,
            @http:Header string? 'x\-customer\-user\-agent,
            @http:Payload {
                mediaType: ["application/json; charset=utf-8", "application/json", "application/jose+jwe"]
            } model:InternationalPaymentRequest payload)
        returns model:InternationalPaymentResponse|error {

        log:printInfo("Initiating a domestic scheduled payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "international-payments");
        if result is error {
            return result;
        }

        model:InternationalPaymentResponse|error response =
                        self.paymentClient->/international\-payments.post(payload);

        return response;
    }

    # Get International Payment by payment ID
    #
    # + internationalPaymentId - the input payment ID
    # + return - International Payment
    isolated resource function get international\-payments/[string internationalPaymentId](@http:Header string? 'x\-fapi\-auth\-date,
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id,
            @http:Header string? 'x\-customer\-user\-agent)
        returns model:InternationalPaymentResponse|error {

        log:printInfo("Retriveing International Payment for payment ID: " + internationalPaymentId);
        model:InternationalPaymentResponse|error|model:PayloadParseError response =
                        self.paymentClient->/international\-payments/[internationalPaymentId];

        return response;
    }

    # Get International Payment Details by payment ID
    #
    # + internationalPaymentId - the input payment ID
    # + return - International Payment
    isolated resource function get international\-payments/[string internationalPaymentId]/'payment\-details(
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address,
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printInfo("Retriveing International Payment Details for payment ID: " + internationalPaymentId);
        model:PaymentDetailsResponse|error response =
                        self.paymentClient->/payments\-details/international\-payments/[internationalPaymentId];
        return response;
    }

    # Create an international scheduled payment
    #
    # + return - InternationalScheduledPaymentResponse object if successful else returns error
    isolated resource function post international\-scheduled\-payments(@http:Header string? 'x\-fapi\-auth\-date,
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id,
            @http:Header string 'x\-idempotency\-key, @http:Header string 'x\-jws\-signature,
            @http:Header string? 'x\-customer\-user\-agent,
            @http:Payload {
                mediaType: ["application/json", "application/jose+jwe", "application/json; charset=utf-8"]
            } model:InternationalScheduledPaymentRequest payload)
        returns model:InternationalScheduledPaymentResponse|error {

        log:printInfo("Initiating a domestic scheduled payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "international-scheduled-payments");
        if result is error {
            return result;
        }

        model:InternationalScheduledPaymentResponse|error response =
                        self.paymentClient->/international\-scheduled\-payments.post(payload);

    
        return response;
    }

    # Get International Scheduled Payment by payment ID
    #
    # + internationalScheduledPaymentId - the input payment ID
    # + return - International Scheduled Payment
    isolated resource function get international\-scheduled\-payments/[string internationalScheduledPaymentId](
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address,
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent)
        returns model:InternationalScheduledPaymentResponse|error {

        log:printInfo("Retriveing International Scheduled Payment for payment ID: " + internationalScheduledPaymentId);
        model:InternationalScheduledPaymentResponse|error|model:PayloadParseError response =
                        self.paymentClient->/international\-scheduled\-payments/[internationalScheduledPaymentId];

        return response;
    }

    # Get International Scheduled Payment Details by payment ID
    #
    # + internationalScheduledPaymentId - the input payment ID
    # + return - International Scheduled Payment
    isolated resource function get international\-scheduled\-payments/[string internationalScheduledPaymentId]/'payment\-details(
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address,
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printInfo("Retriveing International Scheduled Payment Details for payment ID: " + internationalScheduledPaymentId);
        model:PaymentDetailsResponse|error response =
                        self.paymentClient->/payments\-details/international\-scheduled\-payments/[internationalScheduledPaymentId];
    
        return response;
    }

    # Create an international standing order
    #
    # + return - InternationalStandingOrderResponse object if successful else returns error
    # + payload - the request payload
    isolated resource function post international\-standing\-orders(@http:Header string? 'x\-fapi\-auth\-date,
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id,
            @http:Header string 'x\-idempotency\-key, @http:Header string 'x\-jws\-signature,
            @http:Header string? 'x\-customer\-user\-agent,
            @http:Payload {
                mediaType: ["application/json", "application/jose+jwe", "application/json; charset=utf-8"]
            } model:InternationalStandingOrderRequest payload)
        returns model:InternationalStandingOrderResponse|error {

        log:printInfo("Initiating a domestic scheduled payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "international-standing-orders");
        if result is error {
            return result;
        }

        model:InternationalStandingOrderResponse|error response =
                        self.paymentClient->/international\-standing\-orders.post(payload);
        return response;
    }

    # Get International Standing Order by payment ID
    #
    # + internationalStandingOrderPaymentId - the input payment ID
    # + return - International Standing Order
    isolated resource function get international\-standing\-orders/[string internationalStandingOrderPaymentId](
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address,
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent)
        returns model:InternationalStandingOrderResponse|error {

        log:printInfo("Retriveing International Standing Order for payment ID: " + internationalStandingOrderPaymentId);
        model:InternationalStandingOrderResponse|error response =
                        self.paymentClient->/international\-standing\-orders/[internationalStandingOrderPaymentId];

        return response;
    }

    # Get International Standing Order Details by payment ID
    #
    # + internationalStandingOrderPaymentId - the input payment ID
    # + return - International Standing Order
    isolated resource function get international\-standing\-orders/[string internationalStandingOrderPaymentId]/'payment\-details(
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address,
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent)
        returns model:PaymentDetailsResponse|error {

        log:printInfo("Retriveing International Standing Order Details for payment ID: " + internationalStandingOrderPaymentId);
        model:PaymentDetailsResponse|error response =
                        self.paymentClient->/payments\-details/international\-standing\-orders/[internationalStandingOrderPaymentId];

        return response;
    }

    # Validate the payload
    #
    # + payload - the payload object
    # + path - the path
    # + return - boolean
    private isolated function validatePayload(anydata payload, string path) returns ()|model:InvalidPayloadError {

        log:printInfo("Validate the payload");
        validator:PayloadValidator payloadValidator = new ();
        ()|model:InvalidPayloadError payloadValidatorResult = payloadValidator
            .add(new validator:PaymentRequestBodyValidator(payload, path))
            .add(new validator:CreditorAccountValidator(payload, path))
            .add(new validator:DebtorAccountValidator(payload, path))
            .validate();

        return payloadValidatorResult;
    }
}
