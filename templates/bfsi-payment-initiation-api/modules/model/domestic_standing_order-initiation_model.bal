// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import ballerina/constraint;

# The Initiation payload is sent by the initiating party to the bank. It is used to request movement of funds from the debtor account to a creditor for a domestic standing order.
public type DomesticStandingOrderInitiation record {
    # Individual Definitions:
    # EvryDay - Every day
    # EvryWorkgDay - Every working day
    # IntrvlWkDay - An interval specified in weeks (01 to 09), and the day within the week (01 to 07)
    # WkInMnthDay - A monthly interval, specifying the week of the month (01 to 05) and day within the week (01 to 07)
    # IntrvlMnthDay - An interval specified in months (between 01 to 06, 12, 24), specifying the day within the month (-5 to -1, 1 to 31)
    # QtrDay - Quarterly (either ENGLISH, SCOTTISH, or RECEIVED). 
    # ENGLISH = Paid on the 25th March, 24th June, 29th September and 25th December. 
    # SCOTTISH = Paid on the 2nd February, 15th May, 1st August and 11th November.
    # RECEIVED = Paid on the 20th March, 19th June, 24th September and 20th December. 
    # Individual Patterns:
    # EvryDay (ScheduleCode)
    # EvryWorkgDay (ScheduleCode)
    # IntrvlWkDay:IntervalInWeeks:DayInWeek (ScheduleCode + IntervalInWeeks + DayInWeek)
    # WkInMnthDay:WeekInMonth:DayInWeek (ScheduleCode + WeekInMonth + DayInWeek)
    # IntrvlMnthDay:IntervalInMonths:DayInMonth (ScheduleCode + IntervalInMonths + DayInMonth)
    # QtrDay: + either (ENGLISH, SCOTTISH or RECEIVED) ScheduleCode + QuarterDay
    # The regular expression for this element combines five smaller versions for each permitted pattern. To aid legibility - the components are presented individually here:
    # EvryDay
    # EvryWorkgDay
    # IntrvlWkDay:0[1-9]:0[1-7]
    # WkInMnthDay:0[1-5]:0[1-7]
    # IntrvlMnthDay:(0[1-6]|12|24):(-0[1-5]|0[1-9]|[12][0-9]|3[01])
    # QtrDay:(ENGLISH|SCOTTISH|RECEIVED)
    # Full Regular Expression:
    # ^(EvryDay)$|^(EvryWorkgDay)$|^(IntrvlWkDay:0[1-9]:0[1-7])$|^(WkInMnthDay:0[1-5]:0[1-7])$|^(IntrvlMnthDay:(0[1-6]|12|24):(-0[1-5]|0[1-9]|[12][0-9]|3[01]))$|^(QtrDay:(ENGLISH|SCOTTISH|RECEIVED))$
    string Frequency;
    # Unique reference, as assigned by the creditor, to unambiguously refer to the payment transaction.
    # Usage: If available, the initiating party should provide this reference in the structured remittance information, to enable reconciliation by the creditor upon receipt of the amount of money.
    # If the business context requires the use of a creditor reference or a payment remit identification, and only one identifier can be passed through the end-to-end chain, the creditor's reference or payment remittance identification should be quoted in the end-to-end transaction identification.
    @constraint:String {maxLength: 35, minLength: 1}
    string Reference?;
    # Number of the payments that will be made in completing this frequency sequence including any executed since the sequence start date.
    @constraint:String {maxLength: 35, minLength: 1}
    string NumberOfPayments?;
    # The date on which the first payment for a Standing Order schedule will be made.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string FirstPaymentDateTime = getFutureDateTime();
    # The date on which the first recurring payment for a Standing Order schedule will be made. 
    # Usage: This must be populated only if the first recurring date is different to the first payment date.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string RecurringPaymentDateTime?;
    # The date on which the final payment for a Standing Order schedule will be made.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string FinalPaymentDateTime?;
    # The amount of the first Standing Order
    Amount FirstPaymentAmount;
    # The amount of the recurring Standing Order
    Amount RecurringPaymentAmount?;
    # The amount of the final Standing Order
    Amount FinalPaymentAmount?;
    # Unambiguous identification of the account of the debtor to which a debit entry will be made as a result of the transaction.
    DebtorAccount DebtorAccount?;
    # Identification assigned by an institution to identify an account. This identification is known by the account owner.
    CreditorAccount CreditorAccount;
    # Additional information that can not be captured in the structured fields and/or any other specific block.
    anydata SupplementaryData?;
};
