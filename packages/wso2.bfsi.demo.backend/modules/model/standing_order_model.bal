// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import wso2.bfsi.demo.backend.util;

# Set of elements used to define the standing order details.
public type StandingOrder record {
    # A unique and immutable identifier used to identify the account resource. This identifier has no meaning to the account owner.
    readonly string AccountId;
    # A unique and immutable identifier used to identify the standing order resource. This identifier has no meaning to the account owner.
    readonly string StandingOrderId;
    # Individual Definitions:
    # NotKnown - Not Known
    # EvryDay - Every day
    # EvryWorkgDay - Every working day
    # IntrvlDay - An interval specified in number of calendar days (02 to 31)
    # IntrvlWkDay - An interval specified in weeks (01 to 09), and the day within the week (01 to 07)
    # WkInMnthDay - A monthly interval, specifying the week of the month (01 to 05) and day within the week (01 to 07)
    # IntrvlMnthDay - An interval specified in months (between 01 to 06, 12, 24), specifying the day within the month (-05 to -01, 01 to 31)
    # QtrDay - Quarterly (either ENGLISH, SCOTTISH, or RECEIVED)
    # ENGLISH = Paid on the 25th March, 24th June, 29th September and 25th December.
    # SCOTTISH = Paid on the 2nd February, 15th May, 1st August and 11th November.
    # RECEIVED = Paid on the 20th March, 19th June, 24th September and 20th December.
    # Individual Patterns:
    # NotKnown (ScheduleCode)
    # EvryDay (ScheduleCode)
    # EvryWorkgDay (ScheduleCode)
    # IntrvlDay:NoOfDay (ScheduleCode + NoOfDay)
    # IntrvlWkDay:IntervalInWeeks:DayInWeek (ScheduleCode + IntervalInWeeks + DayInWeek)
    # WkInMnthDay:WeekInMonth:DayInWeek (ScheduleCode + WeekInMonth + DayInWeek)
    # IntrvlMnthDay:IntervalInMonths:DayInMonth (ScheduleCode + IntervalInMonths + DayInMonth)
    # QtrDay: + either (ENGLISH, SCOTTISH or RECEIVED) ScheduleCode + QuarterDay
    # The regular expression for this element combines five smaller versions for each permitted pattern. To aid legibility - the components are presented individually here:
    # NotKnown
    # EvryDay
    # EvryWorkgDay
    # IntrvlDay:((0[2-9])|([1-2][0-9])|3[0-1])
    # IntrvlWkDay:0[1-9]:0[1-7]
    # WkInMnthDay:0[1-5]:0[1-7]
    # IntrvlMnthDay:(0[1-6]|12|24):(-0[1-5]|0[1-9]|[12][0-9]|3[01])
    # QtrDay:(ENGLISH|SCOTTISH|RECEIVED)
    # Full Regular Expression:
    # ^(NotKnown)$|^(EvryDay)$|^(EvryWorkgDay)$|^(IntrvlDay:((0[2-9])|([1-2][0-9])|3[0-1]))$|^(IntrvlWkDay:0[1-9]:0[1-7])$|^(WkInMnthDay:0[1-5]:0[1-7])$|^(IntrvlMnthDay:(0[1-6]|12|24):(-0[1-5]|0[1-9]|[12][0-9]|3[01]))$|^(QtrDay:(ENGLISH|SCOTTISH|RECEIVED))$
    string Frequency;
    # Unique reference, as assigned by the creditor, to unambiguously refer to the payment transaction.
    # Usage: If available, the initiating party should provide this reference in the structured remittance information, to enable reconciliation by the creditor upon receipt of the amount of money.
    # If the business context requires the use of a creditor reference or a payment remit identification, and only one identifier can be passed through the end-to-end chain, the creditor's reference or payment remittance identification should be quoted in the end-to-end transaction identification.
    string Reference?;
    # The date on which the first payment for a Standing Order schedule will be made.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string FirstPaymentDateTime = util:getPastDateTime();
    # The date on which the next payment for a Standing Order schedule will be made.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string NextPaymentDateTime = util:getFutureDateTime();
    # The date on which the last (most recent) payment for a Standing Order schedule was made.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string LastPaymentDateTime = util:getPastDateTime();
    # The date on which the final payment for a Standing Order schedule will be made.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string FinalPaymentDateTime = util:getFutureDateTime();
    # Number of the payments that will be made in completing this frequency sequence including any executed since the sequence start date.
    string NumberOfPayments?;
    # Specifies the status of the standing order in code form.
    string StandingOrderStatusCode?;
    # The amount of the first Standing Order
    Amount FirstPaymentAmount?;
    # The amount of the next Standing Order.
    Amount NextPaymentAmount?;
    # The amount of the last (most recent) Standing Order instruction.
    Amount LastPaymentAmount?;
    # The amount of the final Standing Order
    Amount FinalPaymentAmount?;
    # Party that manages the account on behalf of the account owner, that is manages the registration and booking of entries on the account, calculates balances on the account and provides information about the account.
    # This is the servicer of the beneficiary account.
    CreditorAgent CreditorAgent?;
    # Provides the details to identify the beneficiary account.
    CreditorAccount CreditorAccount?;
    # Additional information that can not be captured in the structured fields and/or any other specific block.
    Object SupplementaryData?;
};
