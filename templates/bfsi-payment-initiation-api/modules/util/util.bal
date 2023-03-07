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

# Represents a subtype of BAD_REQUEST error
public type BadRequest record {|
    *http:BadRequest;

    # Open Banking error format
    record {
        # Low level texual error code
        string ErrorCode;
        # Description of the error occured
        string Message;
        # Recommended but optional reference to the JSON Path of the field with error
        string Path?;
        # URL to help remediate the problem, or provide more information, or to API Reference, or help etc
        string Url?;
    } body;
|};

public const string CODE_RESOURCE_INVALID_FORMAT = "UK.OBIE.Resource.InvalidFormat";
public const string CODE_HEADER_INVALID = "UK.OBIE.Header.Invalid";
public const string CODE_FIELD_INVALID = "UK.OBIE.Field.Invalid";
public const string CODE_FIELD_MISSING = "UK.OBIE.Field.Missing";