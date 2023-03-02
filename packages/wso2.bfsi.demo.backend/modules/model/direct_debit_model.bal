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

# Account to or from which a cash entry is made.
public type DirectDebit record {
    # A unique and immutable identifier used to identify the account resource. This identifier has no meaning to the account owner.
    readonly string AccountId;
    # A unique and immutable identifier used to identify the direct debit resource. This identifier has no meaning to the account owner.
    readonly string DirectDebitId;
    # Direct Debit reference. For AUDDIS service users provide Core Reference. For non AUDDIS service users provide Core reference if possible or last used reference.
    string MandateIdentification = util:getRandomId();
    # Specifies the status of the direct debit in code form.
    string DirectDebitStatusCode?;
    # Name of Service User.
    string Name;
    # Date of most recent direct debit collection.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string PreviousPaymentDateTime = util:getPastDateTime();
    # Regularity with which direct debit instructions are to be created and processed.
    string Frequency?;
    # The amount of the most recent direct debit collection.
    Amount PreviousPaymentAmount?;
};
