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

# Validates UUIDs
public class UUIDValidator {
    *IHeaderValidator;
    private final string uuid = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

    # Initializes the UUID validator
    #
    # + header - The UUID to be validated
    public isolated function init(string? header) {
        self.header = header ?: "";
    }

    # Validates the UUID
    #
    # + return - Returns an error if the UUID is invalid
    isolated function validate() returns error? {
        if (self.header == "") {
            // This header is optional. hence, return true
            return ();
        }

        boolean isUuid = regex:matches(self.header, self.uuid);
        return isUuid ? () : error("Found invalid UUID in headers");
    }
}
