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
        check self.validatePayload(request);
        final string domesticPaymentId = model:getRandomId();
        return {
            Data: {
                DomesticPaymentId: domesticPaymentId,
                ConsentId: request?.Data?.ConsentId,
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: request?.Data?.Initiation
            },
            Links: self.getLinks("domestic-payments/", domesticPaymentId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Get a domestic payment by Id.
    #
    # + domesticPaymentId - Domestic payment Id
    # + return - Domestic payment or error
    isolated resource function get domestic\-payments/[string domesticPaymentId]()
                                                returns model:DomesticPaymentResponse|error {
        check self.validatePaymentId(domesticPaymentId);
        return {
            Data: {
                DomesticPaymentId: domesticPaymentId,
                ConsentId: model:getRandomId(),
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: check util:getDomesticPaymentInitiation().fromJsonWithType()
            },
            Links: self.getLinks("domestic-payments/", domesticPaymentId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Store a domestic scheduled payment object.
    #
    # + request - Domestic scheduled payment payload
    # + return - Domestic scheduled payment with ID or error
    isolated resource function post domestic\-scheduled\-payments(model:DomesticScheduledPaymentRequest request)
                                                    returns model:DomesticScheduledPaymentResponse|error {
        check self.validatePayload(request);
        final string domesticScheduledPaymentId = model:getRandomId();
        return {
            Data: {
                DomesticScheduledPaymentId: domesticScheduledPaymentId,
                ConsentId: request.Data.ConsentId,
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: request.Data.Initiation
            },
            Links: self.getLinks("domestic-scheduled-payments/", domesticScheduledPaymentId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Get a domestic scheduled payment by Id.
    #
    # + domesticScheduledPaymentId - Domestic scheduled payment Id
    # + return - Domestic scheduled payment or error
    isolated resource function get domestic\-scheduled\-payments/[string domesticScheduledPaymentId]()
                                                    returns model:DomesticScheduledPaymentResponse|error {
        check self.validatePaymentId(domesticScheduledPaymentId);
        return {
            Data: {
                DomesticScheduledPaymentId: domesticScheduledPaymentId,
                ConsentId: model:getRandomId(),
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: check util:getDomesticScheduledPaymentInitiation().fromJsonWithType()
            },
            Links: self.getLinks("domestic-scheduled-payments/", domesticScheduledPaymentId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Store a domestic standing order payment object.
    #
    # + request - Domestic standing order payment payload
    # + return - Domestic standing order payment with ID or error
    isolated resource function post domestic\-standing\-orders(model:DomesticStandingOrderRequest request)
                                                    returns model:DomesticStandingOrderResponse|error {
        check self.validatePayload(request);
        final string domesticStandingOrderId = model:getRandomId();
        return {
            Data: {
                DomesticStandingOrderId: domesticStandingOrderId,
                ConsentId: request.Data.ConsentId,
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: request.Data.Initiation
            },
            Links: self.getLinks("domestic-standing-orders/", domesticStandingOrderId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Get a domestic standing order payment by Id.
    #
    # + domesticStandingOrderId - Domestic standing order payment Id
    # + return - Domestic standing order payment or error
    isolated resource function get domestic\-standing\-orders/[string domesticStandingOrderId]()
                                                    returns model:DomesticStandingOrderResponse|error {
        check self.validatePaymentId(domesticStandingOrderId);
        return {
            Data: {
                DomesticStandingOrderId: domesticStandingOrderId,
                ConsentId: model:getRandomId(),
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: check util:getDomesticStandingOrderPaymentInitiation().fromJsonWithType()
            },
            Links: self.getLinks("domestic-standing-orders/", domesticStandingOrderId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Store a file payment object.
    #
    # + request - File payment payload
    # + return - File payment with ID or error
    isolated resource function post file\-payments(model:FilePaymentRequest request)
                                                    returns model:FilePaymentResponse|error {
        check self.validatePayload(request);
        final string filePaymentId = model:getRandomId();
        return {
            Data: {
                FilePaymentId: filePaymentId,
                ConsentId: request.Data.ConsentId,
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: request.Data.Initiation
            },
            Links: self.getLinks("file-payments/", filePaymentId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Get a file payment by Id.
    #
    # + filePaymentId - File payment Id
    # + return - File payment or error
    isolated resource function get file\-payments/[string filePaymentId]()
                                                    returns model:FilePaymentResponse|error {
        check self.validatePaymentId(filePaymentId);
        return {
            Data: {
                FilePaymentId: filePaymentId,
                ConsentId: model:getRandomId(),
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: check util:getFilePaymentInitiation().fromJsonWithType()
            },
            Links: self.getLinks("file-payments/", filePaymentId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Store an international payment object.
    #
    # + request - International payment payload
    # + return - International payment with ID or error
    isolated resource function post international\-payments(model:InternationalPaymentRequest request)
                                                    returns model:InternationalPaymentResponse|error {
        check self.validatePayload(request);
        string internationalPaymentId = model:getRandomId();
        return {
            Data: {
                InternationalPaymentId: internationalPaymentId,
                ConsentId: request.Data.ConsentId,
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: request.Data.Initiation
            },
            Links: self.getLinks("international-payments/", internationalPaymentId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Get an international payment by Id.
    #
    # + internationalPaymentId - International payment Id
    # + return - International payment or error
    isolated resource function get international\-payments/[string internationalPaymentId]()
                                                    returns model:InternationalPaymentResponse|error {
        check self.validatePaymentId(internationalPaymentId);
        return {
            Data: {
                InternationalPaymentId: internationalPaymentId,
                ConsentId: model:getRandomId(),
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: check util:getInternationalPaymentInitiation().fromJsonWithType()
            },
            Links: self.getLinks("international-payments/", internationalPaymentId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Store an international scheduled payment object.
    #
    # + request - International scheduled payment payload
    # + return - International scheduled payment with ID or error
    isolated resource function post international\-scheduled\-payments(model:InternationalScheduledPaymentRequest request)
                                                    returns model:InternationalScheduledPaymentResponse|error {
        check self.validatePayload(request);
        final string internationalScheduledPaymentId = model:getRandomId();
        return {
            Data: {
                InternationalScheduledPaymentId: internationalScheduledPaymentId,
                ConsentId: request.Data.ConsentId,
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: request.Data.Initiation
            },
            Links: self.getLinks("international-schedules-payments/", internationalScheduledPaymentId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Get an international scheduled payment by Id.
    #
    # + internationalScheduledPaymentId - International scheduled payment Id
    # + return - International scheduled payment or error
    isolated resource function get international\-scheduled\-payments/[string internationalScheduledPaymentId]()
                                                        returns model:InternationalScheduledPaymentResponse|error {
        check self.validatePaymentId(internationalScheduledPaymentId);
        return {
            Data: {
                InternationalScheduledPaymentId: internationalScheduledPaymentId,
                ConsentId: model:getRandomId(),
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: check util:getInternatioanlScheduledPaymentInitiation().fromJsonWithType()
            },
            Links: self.getLinks("international-schedules-payments/", internationalScheduledPaymentId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Store an international standing order payment object.
    #
    # + request - International standing order payment payload
    # + return - International standing order payment with ID or error
    isolated resource function post international\-standing\-orders(model:InternationalStandingOrderRequest request)
                                                    returns model:InternationalStandingOrderResponse|error {
        check self.validatePayload(request);
        final string internationalStandingorderId = model:getRandomId();
        return {
            Data: {
                InternationalStandingOrderId: internationalStandingorderId,
                ConsentId: request.Data.ConsentId,
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: request.Data.Initiation
            },
            Links: self.getLinks("international-standing-orders/", internationalStandingorderId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Get an international standing order payment by Id.
    #
    # + internationalStandingOrderId - International standing order payment Id
    # + return - International standing order payment or error
    isolated resource function get international\-standing\-orders/[string internationalStandingOrderId]()
                                                        returns model:InternationalStandingOrderResponse|error {
        check self.validatePaymentId(internationalStandingOrderId);
        return {
            Data: {
                InternationalStandingOrderId: internationalStandingOrderId,
                ConsentId: model:getRandomId(),
                Status: util:STATUS_SETTLEMENT_IN_PROCESS,
                Initiation: check util:getInternatioanlStandingOrderPaymentInitiation().fromJsonWithType()
            },
            Links: self.getLinks("international-standing-orders/", internationalStandingOrderId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Retrieve payment details by Id.
    #
    # + path - Path of the request
    # + paymentId - Domestic payment Id
    # + return - Domestic payment details or error
    isolated resource function get payments\-details/[string path]/[string paymentId]()
                                                        returns model:PaymentDetailsResponse|error {
        check self.validatePaymentId(paymentId);
        return {
            Data: self.getPaymentDetailsData(paymentId),
            Links: self.getLinks(path, paymentId),
            Meta: {
                TotalPages: 1
            }
        };
    }

    # Get the Link matching to the path and consent id.
    #
    # + path - Path of the request
    # + id - Consent Id of the request
    # + return - Link
    private isolated function getLinks(string path, string id) returns model:Links => {
        Self: "https://api.alphabank.com/open-banking/v3.1/pisp/" + path + id
    };

    # Get the payment details data.
    #
    # + id - Payment Id
    # + return - Payment details data
    private isolated function getPaymentDetailsData(string id) returns model:PaymentDetailsResponseData {
        model:PaymentDetailsResponseDataStatusDetail statusDetail = {
            LocalInstrument: "UK.OBIE.BACS",
            Status: "Accepted",
            StatusReason: "PendingFailingSettlement",
            StatusReasonDescription: "Enough amount is in your account to complete the transaction."
        };

        model:PaymentDetailsResponseDataPaymentStatus paymentStatusAcc = {
            PaymentTransactionId: id,
            Status: "Accepted",
            StatusDetail: statusDetail,
            StatusUpdateDateTime: model:getDateTime()
        };

        model:PaymentDetailsResponseDataPaymentStatus paymentStatusAccCP = {
            PaymentTransactionId: id,
            Status: "AcceptedCustomerProfile",
            StatusDetail: statusDetail,
            StatusUpdateDateTime: model:getDateTime()
        };
        return {
            PaymentStatus: [paymentStatusAcc, paymentStatusAccCP]
        };
    }

    # Handle the error.
    #
    # + e - Error
    # + return - Error
    private isolated function handleError(error e) returns model:PayloadParseError {
        log:printError("Failed to generate payments json. Caused by, ", e);
        return error("Try Again! Failed to generate payments response", ErrorCode = util:CODE_INTERNAL_SERVER_ERROR);
    }

    # Validate whether the Request Payload is empty.
    #
    # + request - Request Payload
    # + return - Error if the Request Payload is empty
    private isolated function validatePayload(anydata request) returns error? {
        if request is () || request.toString().length() == 0 {
            log:printDebug(util:EMPTY_REQUEST_BODY);
            return error(util:EMPTY_REQUEST_BODY, ErrorCode = util:CODE_RESOURCE_INVALID_FORMAT);
        }

    }
    # Validate whether the payment Id is empty.
    #
    # + paymentId - Payment Id
    # + return - Error if the payment Id is empty
    private isolated function validatePaymentId(string paymentId) returns error? {
        if paymentId.trim() == "" {
            log:printDebug(util:EMPTY_PAYMENT_ID);
            return error(util:EMPTY_PAYMENT_ID, ErrorCode = util:CODE_FIELD_MISSING);
        }
    }

}
