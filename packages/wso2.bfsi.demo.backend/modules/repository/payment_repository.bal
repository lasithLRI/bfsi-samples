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

import wso2.bfsi.demo.backend.model;

#Works as the payment service repository.
public class PaymentsRepository {

    #Creates a `DomesticPayments` table type in which each member is uniquely identified using its `PaymentId` field.
    private table<model:PaymentDaoModel> key(PaymentId) domesticPayments = table[];

    #Creates a `DomesticScheduledPayments` table type in which each member is uniquely identified using its `PaymentId` field.
    private table<model:PaymentDaoModel> key(PaymentId) domesticScheduledPayments = table[];

    #Creates a `DomesticStandingOrderPayments` table type in which each member is uniquely identified using its `PaymentId` field.
    private table<model:PaymentDaoModel> key(PaymentId) domesticStandingOrderPayments = table[];

    #Creates a `FilePayments` table type in which each member is uniquely identified using its `PaymentId` field.
    private table<model:FilePaymentDaoModel> key(PaymentId) filePayments = table[];

    #Creates a `InternationalPayments` table type in which each member is uniquely identified using its `PaymentId` field.
    private table<model:PaymentDaoModel> key(PaymentId) internationalPayments = table[];

    #Creates a `InternationalScheduledPayments` table type in which each member is uniquely identified using its `PaymentId` field.
    private table<model:PaymentDaoModel> key(PaymentId) internationalScheduledPayments = table[];

    #Creates a `InternationalStandingOrderPayments` table type in which each member is uniquely identified using its `PaymentId` field.
    private table<model:PaymentDaoModel> key(PaymentId) internationalStandingOrderPayments = table[];
    
    public isolated function insertDomesticPaymentsData(string ConsentId, model:DomesticPaymentInitiation Initiation, model:Risk Risk) returns string {
        string paymentId = uuid:createType1AsString();
        log:printDebug("Initiating Domestic Payments table");
        self.domesticPayments.put({PaymentId: paymentId, ConsentId: ConsentId, Initiation: Initiation.toJson(), Risk: Risk.toJson()});
        return paymentId;
    }

    public isolated function insertDomesticScheduledPaymentsData(string ConsentId, model:DomesticScheduledPaymentInitiation Initiation, model:Risk Risk) returns string {
        string paymentId = uuid:createType1AsString();
        log:printDebug("Initiating Domestic Scheduled Payments table");
        self.domesticScheduledPayments.put({PaymentId: paymentId, ConsentId: ConsentId, Initiation: Initiation.toJson(), Risk: Risk.toJson()});
        return paymentId;
    }

    public isolated function insertDomesticStandingOrderPaymentsData(string ConsentId, model:DomesticStandingOrderInitiation Initiation, model:Risk Risk) returns string {
        string paymentId = uuid:createType1AsString();
        log:printDebug("Initiating Domestic Standing Order Payments table");
        self.domesticStandingOrderPayments.put({PaymentId: paymentId, ConsentId: ConsentId, Initiation: Initiation.toJson(), Risk: Risk.toJson()});
        return paymentId;
    }

    public isolated function insertFilePaymentsData(string ConsentId, model:FilePaymentInitiation Initiation) returns string {
        string paymentId = uuid:createType1AsString();
        log:printDebug("Initiating File Payments table");
        self.filePayments.put({PaymentId: paymentId, ConsentId: ConsentId, Initiation: Initiation.toJson()});
        return paymentId;
    }

    public isolated function insertInternationalPaymentsData(string ConsentId, model:InternationalPaymentInitiation Initiation, model:Risk Risk) returns string {
        string paymentId = uuid:createType1AsString();
        log:printDebug("Initiating International Payments table");
        self.internationalPayments.put({PaymentId: paymentId, ConsentId: ConsentId, Initiation: Initiation.toJson(), Risk: Risk.toJson()});
        return paymentId;
    }

    public isolated function insertInternationalScheduledPaymentsData(string ConsentId, model:InternationalScheduledPaymentInitiation Initiation, model:Risk Risk) returns string {
        string paymentId = uuid:createType1AsString();
        log:printDebug("Initiating International Scheduled Payments table");
        self.internationalScheduledPayments.put({PaymentId: paymentId, ConsentId: ConsentId, Initiation: Initiation.toJson(), Risk: Risk.toJson()});
        return paymentId;
    }

    public isolated function insertInternationalStandingOrderPaymentsData(string ConsentId, model:InternationalStandingOrderInitiation Initiation, model:Risk Risk) returns string {
        string paymentId = uuid:createType1AsString();
        log:printDebug("Initiating International Standing Order Payments table");
        self.internationalStandingOrderPayments.put({PaymentId: paymentId, ConsentId: ConsentId, Initiation: Initiation.toJson(), Risk: Risk.toJson()});
        return paymentId;
    }

}
