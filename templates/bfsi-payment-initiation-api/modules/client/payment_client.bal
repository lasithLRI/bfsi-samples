// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import ballerina/log;
import ballerina/uuid;
import bfsi_payment_initiation_api.model;
import bfsi_payment_initiation_api.util;

# Represents payments API service methods.
public isolated client class PaymentClient {

    # Store a domestic payment object.
    # 
    # + request - Domestic payment payload
    # + return - Domestic payment with ID or error
    isolated resource function post domestic\-payments(model:DomesticPaymentRequest request) 
                                                    returns model:DomesticPaymentResponse|error {
        if request.length() == 0 {
            
            log:printDebug(util:EMPTY_REQUEST_BODY);
            return error(util:EMPTY_REQUEST_BODY, ErrorCode=util:CODE_RESOURCE_INVALID_FORMAT);
        } 
            
        final string domesticPaymentId = uuid:createType1AsString();
        
        model:DomesticPaymentResponseData data = {
            DomesticPaymentId: domesticPaymentId, 
            ConsentId: request?.Data?.ConsentId, 
            Status: "AcceptedSettlementInProcess", 
            Initiation: request?.Data?.Initiation
        };
        
        model:DomesticPaymentResponse response = {
            Data: data,
            Links: self.getLinks("/domestic-payments/", domesticPaymentId),
            Meta: {
                TotalPages: 1
            }
        };
        
        return response;
    }

    # Get a domestic payment by Id.
    # 
    # + domesticPaymentId - Domestic payment Id
    # + return - Domestic payment or error
    isolated resource function get domestic\-payments/[string domesticPaymentId]() 
                                                returns model:DomesticPaymentResponse|error {
        
        if domesticPaymentId == "" {

            log:printDebug(util:EMPTY_PAYMENT_ID);
            return error(util:EMPTY_PAYMENT_ID, ErrorCode=util:CODE_FIELD_MISSING);
        } 
        do {
            final json initiation = util:getDomesticPaymentInitiation();

            model:DomesticPaymentResponseData data = {
                DomesticPaymentId: domesticPaymentId, 
                ConsentId: util:getRandomId(), 
                Status: "AcceptedSettlementInProcess", 
                Initiation: check initiation.fromJsonWithType(model:DomesticPaymentInitiation)
            };

            model:DomesticPaymentResponse response = {
                Data: data,
                Links: self.getLinks("/domestic-payments/", domesticPaymentId),
                Meta: {
                    TotalPages: 1
                }
            };

            return response;
        } on fail var e {
            return self.handleError(e);
        }
    }

    # Store a domestic scheduled payment object.
    # 
    # + request - Domestic scheduled payment payload
    # + return - Domestic scheduled payment with ID or error
    isolated resource function post domestic\-scheduled\-payments(model:DomesticScheduledPaymentRequest request) 
                                                    returns model:DomesticScheduledPaymentResponse|error {
        if request.length() == 0 {
            
            log:printDebug(util:EMPTY_REQUEST_BODY);
            return error(util:EMPTY_REQUEST_BODY, ErrorCode=util:CODE_RESOURCE_INVALID_FORMAT);
        }

        final string domesticScheduledPaymentId = uuid:createType1AsString();

        model:DomesticScheduledPaymentsResponseData data = {
            DomesticScheduledPaymentId: domesticScheduledPaymentId, 
            ConsentId: request.Data.ConsentId, 
            Status: "AcceptedSettlementInProcess", 
            Initiation: request.Data.Initiation
        };

        model:DomesticScheduledPaymentResponse response = {
            Data: data,
            Links: self.getLinks("/domestic-scheduled-payments/", domesticScheduledPaymentId),
            Meta: {
                TotalPages: 1
            }
        };
        
        return response;
    }

    # Get a domestic scheduled payment by Id.
    # 
    # + domesticScheduledPaymentId - Domestic scheduled payment Id
    # + return - Domestic scheduled payment or error
    isolated resource function get domestic\-scheduled\-payments/[string domesticScheduledPaymentId]()
                                                    returns model:DomesticScheduledPaymentResponse|error {
        if domesticScheduledPaymentId == "" {
            
            log:printDebug(util:EMPTY_PAYMENT_ID);
            return error(util:EMPTY_PAYMENT_ID, ErrorCode=util:CODE_FIELD_MISSING);
        } 
            
        do {
            final json initiation = util:getDomesticScheduledPaymentInitiation();

            model:DomesticScheduledPaymentsResponseData data = {
                DomesticScheduledPaymentId: domesticScheduledPaymentId, 
                ConsentId: util:getRandomId(), 
                Status: "AcceptedSettlementInProcess", 
                Initiation: check initiation.fromJsonWithType(model:DomesticScheduledPaymentInitiation)
            };

            model:DomesticScheduledPaymentResponse response = {
                Data: data,
                Links: self.getLinks("/domestic-scheduled-payments/", domesticScheduledPaymentId),
                Meta: {
                    TotalPages: 1
                }
            };

            return response;
        } on fail var e {
            return self.handleError(e);
        }
    }

    # Store a domestic standing order payment object.
    # 
    # + request - Domestic standing order payment payload
    # + return - Domestic standing order payment with ID or error
    isolated resource function post domestic\-standing\-orders(model:DomesticStandingOrderRequest request) 
                                                    returns model:DomesticStandingOrderResponse|error {
        if request.length() == 0 {

            log:printDebug(util:EMPTY_REQUEST_BODY);
            return error(util:EMPTY_REQUEST_BODY, ErrorCode=util:CODE_RESOURCE_INVALID_FORMAT);
        } 
        final string domesticStandingOrderId = uuid:createType1AsString();

        model:DomesticStandingOrderResponseData data = {
            DomesticStandingOrderId: domesticStandingOrderId, 
            ConsentId: request.Data.ConsentId, 
            Status: "AcceptedSettlementInProcess", 
            Initiation: request.Data.Initiation
        };

        model:DomesticStandingOrderResponse response = {
            Data: data,
            Links: self.getLinks("/domestic-standing-orders/", domesticStandingOrderId),
            Meta: {
                TotalPages: 1
            }
        };

        return response;
    }

    # Get a domestic standing order payment by Id.
    # 
    # + domesticStandingOrderId - Domestic standing order payment Id
    # + return - Domestic standing order payment or error
    isolated resource function get domestic\-standing\-orders/[string domesticStandingOrderId]() 
                                                    returns model:DomesticStandingOrderResponse|error {
        if domesticStandingOrderId == "" {
            
            log:printDebug(util:EMPTY_PAYMENT_ID);
            return error(util:EMPTY_PAYMENT_ID, ErrorCode=util:CODE_FIELD_MISSING);
        }       
        do {
            final json initiation = util:getDomesticStandingOrderPaymentInitiation();

            model:DomesticStandingOrderResponseData data = {
                DomesticStandingOrderId: domesticStandingOrderId, 
                ConsentId: util:getRandomId(), 
                Status: "AcceptedSettlementInProcess", 
                Initiation: check initiation.fromJsonWithType(model:DomesticStandingOrderInitiation)
            };

            model:DomesticStandingOrderResponse response = {
                Data: data,
                Links: self.getLinks("/domestic-standing-orders/", domesticStandingOrderId),
                Meta: {
                    TotalPages: 1
                }
            };
            return response;
        } on fail var e {
            return self.handleError(e);
        }
    }

    # Store a file payment object.
    # 
    # + request - File payment payload
    # + return - File payment with ID or error
    isolated resource function post file\-payments(model:FilePaymentRequest request) 
                                                    returns model:FilePaymentResponse|error {
        
        if request.length() == 0 {

            log:printDebug(util:EMPTY_REQUEST_BODY);
            return error(util:EMPTY_REQUEST_BODY, ErrorCode=util:CODE_RESOURCE_INVALID_FORMAT);
        }
        final string filePaymentId = uuid:createType1AsString();

        model:FilePaymentsResponseData data = {
            FilePaymentId: filePaymentId, 
            ConsentId: request.Data.ConsentId, 
            Status: "AcceptedSettlementInProcess", 
            Initiation: request.Data.Initiation
        };

        model:FilePaymentResponse response = {
            Data: data,
            Links: self.getLinks("/file-payments/", filePaymentId),
            Meta: {
                TotalPages: 1
            }
        };
        return response;
    }

    # Get a file payment by Id.
    # 
    # + filePaymentId - File payment Id
    # + return - File payment or error
    isolated resource function get file\-payments/[string filePaymentId]() returns model:FilePaymentResponse|error {
        
        if filePaymentId == "" {

            log:printDebug(util:EMPTY_PAYMENT_ID);
            return error(util:EMPTY_PAYMENT_ID, ErrorCode=util:CODE_FIELD_MISSING);
        }
        do {
            json initiation = util:getFilePaymentInitiation();

            model:FilePaymentsResponseData data = {
                FilePaymentId: filePaymentId, 
                ConsentId: util:getRandomId(), 
                Status: "AcceptedSettlementInProcess", 
                Initiation: check initiation.fromJsonWithType(model:FilePaymentInitiation)
            };

            model:FilePaymentResponse response = {
                Data: data,
                Links: self.getLinks("/file-payments/", filePaymentId),
                Meta: {
                    TotalPages: 1
                }
            };

            return response;
        } on fail var e {
            return self.handleError(e);
        }
    }

    # Store an international payment object.
    # 
    # + request - International payment payload
    # + return - International payment with ID or error
    isolated resource function post international\-payments(model:InternationalPaymentRequest request) 
                                                    returns model:InternationalPaymentResponse|error {
        if request.length() == 0 {
            
            log:printDebug(util:EMPTY_REQUEST_BODY);
            return error(util:EMPTY_REQUEST_BODY, ErrorCode=util:CODE_RESOURCE_INVALID_FORMAT);
        }
        do {
            string internationalPaymentId = uuid:createType1AsString();

            model:InternationalPaymentResponseData data = {
                InternationalPaymentId: internationalPaymentId, 
                ConsentId: request.Data.ConsentId, 
                Status: "AcceptedSettlementInProcess", 
                Initiation: request.Data.Initiation
            };
            model:InternationalPaymentResponse response = {
                Data: data,
                Links: self.getLinks("/international-payments/", internationalPaymentId),
                Meta: {
                    TotalPages: 1
                }
            };
            return response;
        } on fail var e {
            return self.handleError(e);
        }
    }

    # Get an international payment by Id.
    # 
    # + internationalPaymentId - International payment Id
    # + return - International payment or error
    isolated resource function get international\-payments/[string internationalPaymentId]() 
                                                    returns model:InternationalPaymentResponse|error {
        if internationalPaymentId == "" {
            
            log:printDebug(util:EMPTY_PAYMENT_ID);
            return error(util:EMPTY_PAYMENT_ID, ErrorCode=util:CODE_FIELD_MISSING);
        }
        do { 
            final json initiation = util:getInternationalPaymentInitiation();

            model:InternationalPaymentResponseData data = {
                InternationalPaymentId: internationalPaymentId, 
                ConsentId: util:getRandomId(), 
                Status: "AcceptedSettlementInProcess", 
                Initiation: check initiation.fromJsonWithType(model:InternationalPaymentInitiation)
            };

            model:InternationalPaymentResponse response = {
                Data: data,
                Links: self.getLinks("/international-payments/", internationalPaymentId),
                Meta: {
                    TotalPages: 1
                }
            };
            
            return response;
        } on fail var e {
            return self.handleError(e);
        }
    }

    # Store an international scheduled payment object.
    # 
    # + request - International scheduled payment payload
    # + return - International scheduled payment with ID or error
    isolated resource function post international\-scheduled\-payments(model:InternationalScheduledPaymentRequest request) 
                                                    returns model:InternationalScheduledPaymentResponse|error {
        if request.length() == 0 {
            
            log:printDebug(util:EMPTY_REQUEST_BODY);
            return error(util:EMPTY_REQUEST_BODY, ErrorCode=util:CODE_RESOURCE_INVALID_FORMAT);
        }
        final string internationalScheduledPaymentId = uuid:createType1AsString();

        model:InternationalScheduledResponseData data = {
            InternationalScheduledPaymentId: internationalScheduledPaymentId, 
            ConsentId: request.Data.ConsentId, 
            Status: "AcceptedSettlementInProcess", 
            Initiation: request.Data.Initiation
        };

        model:InternationalScheduledPaymentResponse response = {
            Data: data,
            Links: self.getLinks("/international-schedules-payments/", internationalScheduledPaymentId),
            Meta: {
                TotalPages: 1
            }
        };

        return response;
    }

    # Get an international scheduled payment by Id.
    # 
    # + internationalScheduledPaymentId - International scheduled payment Id
    # + return - International scheduled payment or error
    isolated resource function get international\-scheduled\-payments/[string internationalScheduledPaymentId]() 
                                                        returns model:InternationalScheduledPaymentResponse|error {
        if internationalScheduledPaymentId == "" {
            
            log:printDebug(util:EMPTY_PAYMENT_ID);
            return error(util:EMPTY_PAYMENT_ID, ErrorCode=util:CODE_FIELD_MISSING);
        }
        do {
            final json initiation = util:getInternatioanlScheduledPaymentInitiation();

            model:InternationalScheduledResponseData data = {
                InternationalScheduledPaymentId: internationalScheduledPaymentId, 
                ConsentId: util:getRandomId(), 
                Status: "AcceptedSettlementInProcess", 
                Initiation: check initiation.fromJsonWithType(model:InternationalScheduledPaymentInitiation)
            };

            model:InternationalScheduledPaymentResponse response = {
                Data: data,
                Links: self.getLinks("/international-schedules-payments/", internationalScheduledPaymentId),
                Meta: {
                    TotalPages: 1
                }
            };

            return response;
        } on fail var e {
            return self.handleError(e);
        }
    }

    # Store an international standing order payment object.
    # 
    # + request - International standing order payment payload
    # + return - International standing order payment with ID or error
    isolated resource function post international\-standing\-orders(model:InternationalStandingOrderRequest request) 
                                                    returns model:InternationalStandingOrderResponse|error {
        if request.length() == 0 {
            
            log:printDebug(util:EMPTY_REQUEST_BODY);
            return error(util:EMPTY_REQUEST_BODY, ErrorCode=util:CODE_RESOURCE_INVALID_FORMAT);
        }
        final string internationalStandingorderId = uuid:createType1AsString();

        model:InternationalStandingOrderResponseData data = {
            InternationalStandingOrderId: internationalStandingorderId, 
            ConsentId: request.Data.ConsentId, 
            Status: "AcceptedSettlementInProcess", 
            Initiation: request.Data.Initiation
        };
        model:InternationalStandingOrderResponse response = {
            Data: data,
            Links: self.getLinks("/international-standing-orders/", internationalStandingorderId),
            Meta: {
                TotalPages: 1
            }
        };
        return response;
    }

    # Get an international standing order payment by Id.
    # 
    # + internationalStandingOrderId - International standing order payment Id
    # + return - International standing order payment or error
    isolated resource function get international\-standing\-orders/[string internationalStandingOrderId]() 
                                                        returns model:InternationalStandingOrderResponse|error {
        if internationalStandingOrderId == "" {
            
            log:printDebug(util:EMPTY_PAYMENT_ID);
            return error(util:EMPTY_PAYMENT_ID, ErrorCode=util:CODE_FIELD_MISSING);
        }
        do {
            final json initiation = util:getInternatioanlStandingOrderPaymentInitiation();
            
            model:InternationalStandingOrderResponseData data = {
                InternationalStandingOrderId: internationalStandingOrderId, 
                ConsentId: util:getRandomId(), 
                Status: "AcceptedSettlementInProcess", 
                Initiation: check initiation.fromJsonWithType(model:InternationalStandingOrderInitiation)
            };
            
            model:InternationalStandingOrderResponse response = {
                Data: data,
                Links: self.getLinks("/international-standing-orders/", internationalStandingOrderId),
                Meta: {
                    TotalPages: 1
                }
            };
            return response;
        } on fail var e {
            return self.handleError(e);
        }
    }

    # Retrieve payment details by Id.
    #   
    # + path - Path of the request
    # + paymentId - Domestic payment Id
    # + return - Domestic payment details or error
    isolated resource function get payments\-details/[string path]/[string paymentId]() returns model:PaymentDetailsResponse|error {
        
        if paymentId == "" {

            log:printDebug(util:EMPTY_PAYMENT_ID);
            return error(util:EMPTY_PAYMENT_ID, ErrorCode=util:CODE_FIELD_MISSING);
        }

        do {
            model:PaymentDetailsResponseData data = self.getPaymentDetailsData(paymentId);

            model:PaymentDetailsResponse response = {
                Data: data,
                Links: self.getLinks(path, paymentId),
                Meta: {
                    TotalPages: 1
                }
            };

            return response;
        } on fail var e {
            return self.handleError(e);
        }
    }

    # Get the Link matching to the path and consent id
    # 
    # + path - Path of the request
    # + id - Consent Id of the request
    # + return - Link
    private isolated function getLinks(string path, string id) returns model:Links {
        return { Self: "https://api.alphabank.com/open-banking/v3.1/pisp" + path + id };
    }

    # Get the payment details data
    #   
    # + id - Payment Id
    # + return - Payment details data
    private isolated function getPaymentDetailsData(string id) returns model:PaymentDetailsResponseData {
        model:PaymentDetailsResponseDataStatusDetail statusDetail = {
            LocalInstrument:"UK.OBIE.BACS",
            Status: "Accepted",
            StatusReason: "PendingFailingSettlement",
            StatusReasonDescription: "Enough amount is in your account to complete the transaction."
        };

        model:PaymentDetailsResponseDataPaymentStatus paymentStatus = {
            PaymentTransactionId: id,
            Status: "Accepted",
            StatusDetail: statusDetail, 
            StatusUpdateDateTime: util:getDateTime()
        };

        model:PaymentDetailsResponseDataPaymentStatus paymentStatus1 = {
            PaymentTransactionId: id,
            Status: "AcceptedCustomerProfile",
            StatusDetail: statusDetail, 
            StatusUpdateDateTime: util:getDateTime()
        };

        model:PaymentDetailsResponseDataPaymentStatus[] PaymentStatus = [paymentStatus, paymentStatus1];

        model:PaymentDetailsResponseData data = {
            PaymentStatus: PaymentStatus
        };
        return data;
    }

    # Handle the error
    # 
    # + e - Error
    # + return - Error
    private isolated function handleError(error e) returns model:PayloadParseError {
        log:printError("Failed to generate payments json. Caused by, ", e);
        return error("Try Again! Failed to generate payments response", ErrorCode=util:CODE_INTERNAL_SERVER_ERROR);
    }

}
