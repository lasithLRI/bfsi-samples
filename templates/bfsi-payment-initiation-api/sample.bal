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
import bfsi_payment_initiation_api.util;

//This import represents the backend service of your bank
import wso2bfsi/wso2.bfsi.demo.backend as bfsi;
import wso2bfsi/wso2.bfsi.demo.backend.model;

// Request interceptors handle HTTP requests globally 
interceptor:RequestInterceptor requestInterceptor = new;
interceptor:RequestErrorInterceptor requestErrorInterceptor = new;

http:ListenerConfiguration config = {
    host: "localhost",
    interceptors: [requestInterceptor, requestErrorInterceptor]
};
listener http:Listener interceptorListener = new (9090, config);

service / on interceptorListener {

    private bfsi:PaymentService 'paymentService = new();
    
    # Create a domestic payment
    # 
    # + return - DomesticPaymentResponse object if successful else returns error
    isolated resource function post 'domestic\-payments(@http:Header string? 'x\-fapi\-auth\-date, 
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id, 
            @http:Header string 'x\-idempotency\-key,  @http:Header string 'x\-jws\-signature, 
            @http:Header string? 'x\-customer\-user\-agent, 
            @http:Payload {
                mediaType: ["application/json", "application/jose+jwe", "application/json; charset=utf-8"]
            } model:DomesticPaymentRequest payload
        ) 
        returns model:DomesticPaymentResponse|util:BadRequest {

    
        log:printInfo("Initiating a domestic payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "domestic-payments");
        if result is model:InvalidPayloadError {
            return self.generatePayloadValidationError(result.message(), result.detail().get("ErrorCode"));
        }
        model:DomesticPaymentResponse|model:InvalidResourceIdError response = self.'paymentService.createDomesticPayment(payload);

        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response;
}

    # Get Domestic Payment by payment ID
    # 
    # + domesticPaymentId - the input payment ID 
    # + return - Domestic Payment
    isolated resource function get 'domestic\-payments/[string domesticPaymentId](@http:Header string? 'x\-fapi\-auth\-date, 
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id, 
            @http:Header string? 'x\-customer\-user\-agent) 
        returns model:DomesticPaymentResponse|util:BadRequest {
 
        log:printInfo("Retriveing Domestic Payment for payment ID: " + domesticPaymentId);
        model:DomesticPaymentResponse|model:InvalidResourceIdError|model:PayloadParseError response = 
                        self.'paymentService.getDomesticPayments(domesticPaymentId);

        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }

        if response is model:PayloadParseError {
            return self.generatePayloadParseErrorResponse(response);
        }
        return response;
    }

    # Get Domestic Payment Details by payment ID
    # 
    # + domesticPaymentId - the input payment ID 
    # + return - Domestic Payment
    isolated resource function get 'domestic\-payments/[string domesticPaymentId]/'payment\-details(
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address, 
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent) 
        returns model:PaymentDetailsResponse|util:BadRequest {
 
        log:printInfo("Retriveing Domestic Payment Details for payment ID: " + domesticPaymentId);
        model:PaymentDetailsResponse|model:InvalidResourceIdError response = 
                        self.'paymentService.getPaymentsDetails("domestic-payments", domesticPaymentId);

        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
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
                mediaType: ["application/json", "application/jose+jwe", "application/json; charset=utf-8" ]
            } model:DomesticScheduledPaymentRequest payload
        ) 
        returns model:DomesticScheduledPaymentResponse|util:BadRequest {
 
        log:printInfo("Initiating a domestic scheduled payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "domestic-scheduled-payments");
        if result is model:InvalidPayloadError {
            return self.generatePayloadValidationError(result.message(), result.detail().get("ErrorCode"));
        }

        model:DomesticScheduledPaymentResponse|model:InvalidResourceIdError response = 
                        self.'paymentService.createDomesticScheduledPayment(payload);
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response;
    }

    # Get Domestic Scheduled Payment by payment ID
    # 
    # + domesticScheduledPaymentId - the input payment ID
    # + return - Domestic Scheduled Payment
    isolated resource function get 'domestic\-scheduled\-payments/[string domesticScheduledPaymentId](
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address, 
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent) 
        returns model:DomesticScheduledPaymentResponse|util:BadRequest {
  
        log:printInfo("Retriveing Domestic Scheduled Payment for payment ID: " + domesticScheduledPaymentId);
        model:DomesticScheduledPaymentResponse|model:InvalidResourceIdError|model:PayloadParseError response = 
                    self.'paymentService.getDomesticScheduledPayments(domesticScheduledPaymentId);
                    
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }

        if response is model:PayloadParseError {
            return self.generatePayloadParseErrorResponse(response);
        }
        return response;   
    }

    # Get Domestic Scheduled Payment Details by payment ID
    # 
    # + domesticScheduledPaymentId - the input payment ID
    # + return - Domestic Scheduled Payment
    isolated resource function get 'domestic\-scheduled\-payments/[string domesticScheduledPaymentId]/'payment\-details(
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address, 
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent) 
        returns model:PaymentDetailsResponse|util:BadRequest {
  
        log:printInfo("Retriveing Domestic Scheduled Payment Details for payment ID: " + domesticScheduledPaymentId);
         model:PaymentDetailsResponse|model:InvalidResourceIdError response = 
                    self.'paymentService.getPaymentsDetails("domestic-scheduled-payments", domesticScheduledPaymentId);
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response;   
    }
    
    # Create a domestic standing order
    # 
    # + return - DomesticStandingOrderResponse object if successful else returns error
    isolated resource function post 'domestic\-standing\-orders(@http:Header string? 'x\-fapi\-auth\-date, 
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id, 
            @http:Header string 'x\-idempotency\-key, @http:Header string 'x\-jws\-signature, 
            @http:Header string? 'x\-customer\-user\-agent, 
            @http:Payload {
                mediaType: ["application/json; charset=utf-8", "application/json", "application/jose+jwe"]
            } model:DomesticStandingOrderRequest payload) 
        returns model:DomesticStandingOrderResponse|util:BadRequest {

        log:printInfo("Initiating a domestic scheduled payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "domestic-standing-orders");
        if result is model:InvalidPayloadError {
            return self.generatePayloadValidationError(result.message(), result.detail().get("ErrorCode"));
        }

        model:DomesticStandingOrderResponse|model:InvalidResourceIdError response = 
                        self.'paymentService.createDomesticStandingOrderPayment(payload);
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response;   
    }

    # Get Domestic Standing Order by payment ID
    # 
    # + domesticStandingOrderId - the input payment ID
    # + return - Domestic Standing Order
    isolated resource function get 'domestic\-standing\-orders/[string domesticStandingOrderId](
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address, 
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent) 
        returns model:DomesticStandingOrderResponse|util:BadRequest {

        log:printInfo("Retriveing Domestic Standing Order for payment ID: " + domesticStandingOrderId);
        model:DomesticStandingOrderResponse|model:InvalidResourceIdError|model:PayloadParseError response = 
                        self.'paymentService.getDomesticStandingOrderPayments(domesticStandingOrderId);
        
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }

        if response is model:PayloadParseError {
            return self.generatePayloadParseErrorResponse(response);
        }
        return response;  
    }

    # Get Domestic Standing Order Details by payment ID
    # 
    # + domesticStandingOrderId - the input payment ID
    # + return - Domestic Standing Order
    isolated resource function get 'domestic\-standing\-orders/[string domesticStandingOrderId]/'payment\-details(
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address, 
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent) 
        returns model:PaymentDetailsResponse|util:BadRequest {
  
        log:printInfo("Retriveing Domestic Standing Order Details for payment ID: " + domesticStandingOrderId);
        model:PaymentDetailsResponse|model:InvalidResourceIdError response = 
                        self.'paymentService.getPaymentsDetails("domestic-standing-orders", domesticStandingOrderId);
        
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response;   
    }

    # Create a file payment
    # 
    # + return - FilePaymentResponse object if successful else returns error
    isolated resource function post 'file\-payments(@http:Header string? 'x\-fapi\-auth\-date, 
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id, 
            @http:Header string 'x\-idempotency\-key, @http:Header string 'x\-jws\-signature, 
            @http:Header string? 'x\-customer\-user\-agent, 
            @http:Payload {
                mediaType: ["application/json; charset=utf-8", "application/json", "application/jose+jwe"]
            } model:FilePaymentRequest payload) 
        returns model:FilePaymentResponse|util:BadRequest {
 
        log:printInfo("Initiating a domestic scheduled payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "file-payments");
        if result is model:InvalidPayloadError {
            return self.generatePayloadValidationError(result.message(), result.detail().get("ErrorCode"));
        }

        model:FilePaymentResponse|model:InvalidResourceIdError response = self.'paymentService.createFilePayment(payload);

        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response;  
    }

    # Get File Payment by payment ID
    # 
    # + filePaymentId - the input payment ID
    # + return - File Payment
    isolated resource function get 'file\-payments/[string filePaymentId](@http:Header string? 'x\-fapi\-auth\-date, 
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id, 
            @http:Header string? 'x\-customer\-user\-agent) 
        returns model:FilePaymentResponse|util:BadRequest {

        log:printInfo("Retriveing File Payment for payment ID: " + filePaymentId);
        model:FilePaymentResponse|model:InvalidResourceIdError|model:PayloadParseError response = 
                        self.'paymentService.getFilePayments(filePaymentId);
        
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }

        if response is model:PayloadParseError {
            return self.generatePayloadParseErrorResponse(response);
        }
        return response;   
    }

    # Get File Payment Details by payment ID
    #   
    # + filePaymentId - the input payment ID
    # + return - File Payment
    isolated resource function get 'file\-payments/[string filePaymentId]/'payment\-details(@http:Header string? 'x\-fapi\-auth\-date, 
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id, 
            @http:Header string? 'x\-customer\-user\-agent) 
        returns model:PaymentDetailsResponse|util:BadRequest {

        log:printInfo("Retriveing File Payment Details for payment ID: " + filePaymentId);
        model:PaymentDetailsResponse|model:InvalidResourceIdError response = 
                        self.'paymentService.getPaymentsDetails("file-payments", filePaymentId);
        
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response;   
    }

    # Create an international payment
    # 
    # + return - InternationalPaymentResponse object if successful else returns error
    isolated resource function post 'international\-payments(@http:Header string? 'x\-fapi\-auth\-date, 
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id, 
            @http:Header string 'x\-idempotency\-key, @http:Header string 'x\-jws\-signature, 
            @http:Header string? 'x\-customer\-user\-agent, 
            @http:Payload {
                mediaType: ["application/json; charset=utf-8", "application/json", "application/jose+jwe"]
            } model:InternationalPaymentRequest payload) 
        returns model:InternationalPaymentResponse|util:BadRequest {
  
        log:printInfo("Initiating a domestic scheduled payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "international-payments");
        if result is model:InvalidPayloadError {
            return self.generatePayloadValidationError(result.message(), result.detail().get("ErrorCode"));
        }

        model:InternationalPaymentResponse|model:InvalidResourceIdError response = 
                        self.'paymentService.createInternationalPayment(payload);

        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response;
    }

    # Get International Payment by payment ID
    # 
    # + internationalPaymentId - the input payment ID
    # + return - International Payment
    isolated resource function get 'international\-payments/[string internationalPaymentId](@http:Header string? 'x\-fapi\-auth\-date, 
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id, 
            @http:Header string? 'x\-customer\-user\-agent) 
        returns model:InternationalPaymentResponse|util:BadRequest {
               
        log:printInfo("Retriveing International Payment for payment ID: " + internationalPaymentId);
        model:InternationalPaymentResponse|model:InvalidResourceIdError|model:PayloadParseError response = 
                        self.'paymentService.getInternationalPayments(internationalPaymentId);
        
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }

        if response is model:PayloadParseError {
            return self.generatePayloadParseErrorResponse(response);
        }
        return response;   
    }

    # Get International Payment Details by payment ID
    # 
    # + internationalPaymentId - the input payment ID
    # + return - International Payment
    isolated resource function get 'international\-payments/[string internationalPaymentId]/'payment\-details(
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address, 
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent) 
        returns model:PaymentDetailsResponse|util:BadRequest {
  
        log:printInfo("Retriveing International Payment Details for payment ID: " + internationalPaymentId);
        model:PaymentDetailsResponse|model:InvalidResourceIdError response = 
                        self.'paymentService.getPaymentsDetails("international-payments", internationalPaymentId);
        
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response;   
    }
    
    # Create an international scheduled payment
    # 
    # + return - InternationalScheduledPaymentResponse object if successful else returns error
    isolated resource function post 'international\-scheduled\-payments(@http:Header string? 'x\-fapi\-auth\-date, 
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id, 
            @http:Header string 'x\-idempotency\-key, @http:Header string 'x\-jws\-signature, 
            @http:Header string? 'x\-customer\-user\-agent, 
            @http:Payload {
                mediaType: ["application/json", "application/jose+jwe", "application/json; charset=utf-8"]
            } model:InternationalScheduledPaymentRequest payload) 
        returns model:InternationalScheduledPaymentResponse|util:BadRequest {
   
        log:printInfo("Initiating a domestic scheduled payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "international-scheduled-payments");
        if result is model:InvalidPayloadError {
            return self.generatePayloadValidationError(result.message(), result.detail().get("ErrorCode"));
        }

        model:InternationalScheduledPaymentResponse|model:InvalidResourceIdError response = 
                        self.'paymentService.createInternationalScheduledPayment(payload);
        
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response;   
    }

    # Get International Scheduled Payment by payment ID
    # 
    # + internationalScheduledPaymentId - the input payment ID
    # + return - International Scheduled Payment
    isolated resource function get 'international\-scheduled\-payments/[string internationalScheduledPaymentId](
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address, 
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent) 
        returns model:InternationalScheduledPaymentResponse|util:BadRequest {
 
        log:printInfo("Retriveing International Scheduled Payment for payment ID: " + internationalScheduledPaymentId);
        model:InternationalScheduledPaymentResponse|model:InvalidResourceIdError|model:PayloadParseError response = 
                        self.'paymentService.getInternationalScheduledPayments(internationalScheduledPaymentId);
        
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }

        if response is model:PayloadParseError {
            return self.generatePayloadParseErrorResponse(response);
        }
        return response;
    }

    # Get International Scheduled Payment Details by payment ID
    # 
    # + internationalScheduledPaymentId - the input payment ID
    # + return - International Scheduled Payment
    isolated resource function get 'international\-scheduled\-payments/[string internationalScheduledPaymentId]/'payment\-details(
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address, 
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent) 
        returns model:PaymentDetailsResponse|util:BadRequest {
   
        log:printInfo("Retriveing International Scheduled Payment Details for payment ID: " + internationalScheduledPaymentId);
        model:PaymentDetailsResponse|model:InvalidResourceIdError response = 
                        self.'paymentService.getPaymentsDetails("international-scheduled-payments", internationalScheduledPaymentId);
        
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response;   
    }

    # Create an international standing order
    # 
    # + return - InternationalStandingOrderResponse object if successful else returns error
    # + payload - the request payload
    isolated resource function post 'international\-standing\-orders(@http:Header string? 'x\-fapi\-auth\-date, 
            @http:Header string? 'x\-fapi\-customer\-ip\-address, @http:Header string? 'x\-fapi\-interaction\-id, 
            @http:Header string 'x\-idempotency\-key, @http:Header string 'x\-jws\-signature, 
            @http:Header string? 'x\-customer\-user\-agent, 
            @http:Payload {
                mediaType: ["application/json", "application/jose+jwe", "application/json; charset=utf-8"]
            } model:InternationalStandingOrderRequest payload) 
        returns model:InternationalStandingOrderResponse|util:BadRequest {

        log:printInfo("Initiating a domestic scheduled payment");
        ()|model:InvalidPayloadError result = self.validatePayload(payload, "international-standing-orders");
        if result is model:InvalidPayloadError {
            return self.generatePayloadValidationError(result.message(), result.detail().get("ErrorCode"));
        }

        model:InternationalStandingOrderResponse|model:InvalidResourceIdError response = 
                        self.'paymentService.createInternationalStandingOrderPayment(payload);
        
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response; 
    }

    # Get International Standing Order by payment ID
    # 
    # + internationalStandingOrderPaymentId - the input payment ID
    # + return - International Standing Order
    isolated resource function get 'international\-standing\-orders/[string internationalStandingOrderPaymentId](
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address, 
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent) 
        returns  model:InternationalStandingOrderResponse|util:BadRequest {
  
        log:printInfo("Retriveing International Standing Order for payment ID: " + internationalStandingOrderPaymentId);
        model:InternationalStandingOrderResponse|model:InvalidResourceIdError|model:PayloadParseError response = 
                        self.'paymentService.getInternationalStandingOrderPayments(internationalStandingOrderPaymentId);
        
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }

        if response is model:PayloadParseError {
            return self.generatePayloadParseErrorResponse(response);
        }
        return response;
    }

    # Get International Standing Order Details by payment ID
    #  
    # + internationalStandingOrderPaymentId - the input payment ID
    # + return - International Standing Order
    isolated resource function get 'international\-standing\-orders/[string internationalStandingOrderPaymentId]/'payment\-details(
            @http:Header string? 'x\-fapi\-auth\-date, @http:Header string? 'x\-fapi\-customer\-ip\-address, 
            @http:Header string? 'x\-fapi\-interaction\-id, @http:Header string? 'x\-customer\-user\-agent) 
        returns model:PaymentDetailsResponse|util:BadRequest  {

        log:printInfo("Retriveing International Standing Order Details for payment ID: " + internationalStandingOrderPaymentId);
        model:PaymentDetailsResponse|model:InvalidResourceIdError response = 
                        self.'paymentService.getPaymentsDetails("international-standing-orders", internationalStandingOrderPaymentId);
        
        if response is model:InvalidResourceIdError {
            return self.generateErrorResponse(response);
        }
        return response;   
    }


    # Validate the payload
    # 
    # + payload - the payload object
    # + path - the pathy
    # + return - boolean
    private isolated function validatePayload(json payload, string path) returns ()|model:InvalidPayloadError {

        log:printInfo("Validate the payload");
        validator:PayloadValidator payloadValidator = new();
        ()|model:InvalidPayloadError payloadValidatorResult = payloadValidator
            .add(new validator:PaymentRequestBodyValidator(payload, path))
            .add(new validator:CreditorAccountValidator(payload, path))
            .add(new validator:DebtorAccountValidator(payload, path))
            .validate();

        return payloadValidatorResult;
    }

    # Generate error response
    # 
    # + response - the response object
    # + return - BadRequest object
    private isolated function generateErrorResponse(model:InvalidResourceIdError response) returns util:BadRequest {
        return {
            body: {
                ErrorCode: response.detail().get("ErrorCode"),
                Message: response.message()
            }
        };
    }

    # Generate error response
    # 
    # + response - the response object
    # + return - BadRequest object
    private isolated function generatePayloadParseErrorResponse(model:PayloadParseError response) returns util:BadRequest {
        return {
            body: {
                ErrorCode: response.detail().get("ErrorCode"),
                Message: response.message()
            }
        };
    }

    # Generate payload validation error response
    # 
    # + message - the error message
    # + errorCode - the error code
    # + return - BadRequest object
    private isolated function generatePayloadValidationError(string message, string errorCode) returns util:BadRequest {
        return {
            body: {
                ErrorCode: errorCode, 
                Message: message
            }
        };
    }
}
