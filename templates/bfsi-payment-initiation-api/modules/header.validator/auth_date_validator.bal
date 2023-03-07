// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import ballerina/time;

# Validates IP addresses
public class AuthDateVallidator {
    *IHeaderValidator;

    # Initializes the `AuthDateVallidator` object
    #
    # + header - The header value
    public isolated function init(string? header) {
        self.header = header ?: "";
    }

    # Validates the auth date header value
    #
    # + return - Returns an error if the header value is invalid
    isolated function validate() returns ()|error? {
        if (self.header == "") {
            // This header is optional. hence, return true
            return ();
        }

        time:Utc utc1 = time:utcNow();
        time:Utc utc2;
        do {
            utc2 = check time:utcFromString(self.header);
        } on fail var e {
            return error(e.message());
        }
        time:Seconds seconds = time:utcDiffSeconds(utc1, utc2);

        if seconds > <time:Seconds>0 {
            return ();
        } else {
            return error("Invalid Date found in the header");
        }
    }
}
