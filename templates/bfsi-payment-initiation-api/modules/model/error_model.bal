// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

# An object of detail error codes, and messages, and URLs to documentation to help remediation.
public type ErrorDetail record {|
    # Low level textual error code, e.g., OB.Field.Missing
    string ErrorCode;
    # Recommended but optional reference to the JSON Path of the field with error, e.g., 
    # Data.Initiation.InstructedAmount.Currency
    string Path?;
    # URL to help remediate the problem, or provide more information, or to API Reference, or help etc
    string Url?;
|};

type Error distinct error<ErrorDetail>;

# An error object to represent an invalid resource id.
public type InvalidResourceIdError distinct Error;

# An error object to represent an invalid payload.
public type InvalidPayloadError distinct Error;

# An error object to represent a payload parse error.
public type PayloadParseError distinct Error;
