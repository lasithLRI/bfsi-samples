// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import ballerina/regex;

import bfsi_account_and_transaction_api.model;
import bfsi_account_and_transaction_api.util;

const string PATTERN_IP_V4 = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}" +
    "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
const string PATTERN_IP_V6 = "((([0-9a-fA-F]){1,4})\\:){7}([0-9a-fA-F]){1,4}";
const string PATTERN_UUID = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

# Validates the ip address request headers.
#
# + header - The header ip address to be validated.
# + return - The result of the validation.
public isolated function validateIpAddressHeader(string? header) returns model:InvalidHeaderError? {
    if header is () || util:isEmpty(header) {
        // This is an optional header. Hence, if the header is not present, skips the validation.
        return;
    }
    if !(regex:matches(header, PATTERN_IP_V4) || regex:matches(header, PATTERN_IP_V6)) {
        return error("Found invalid ip address in headers", ErrorCode = util:CODE_INVALID_REQUEST_HEADER);
    }
}

# Validates the UUID request headers.
#
# + header - The header UUID to be validated.
# + return - The result of the validation.
public isolated function validateUUIDHeader(string? header) returns model:InvalidHeaderError? {
    if header is () || util:isEmpty(header) {
        // This is an optional header. Hence, if the header is not present, skips the validation.
        return;
    }
    if !regex:matches(header, PATTERN_UUID) {
        return error("Found invalid UUID in headers", ErrorCode = util:CODE_INVALID_REQUEST_HEADER);
    }
}
