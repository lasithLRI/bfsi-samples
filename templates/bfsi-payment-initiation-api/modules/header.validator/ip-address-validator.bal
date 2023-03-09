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

# Validates IP addresses
public class IpAddressValidator {
    *IHeaderValidator;
    private final string ipv4 = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])";
    private final string ipv6 = "((([0-9a-fA-F]){1,4})\\:){7}([0-9a-fA-F]){1,4}";

    # Initializes the IP address validator
    public isolated function init(string? header) {
        self.header = header ?: "";
    }

    # Validates the IP address
    #
    # + return - Returns an error if the IP address is invalid
    isolated function validate() returns error? {
        if (self.header == "") {
            // This header is optional. hence, return true
            return ();
        }

        boolean isIpv4 = regex:matches(self.header, self.ipv4);
        boolean isIpv6 = regex:matches(self.header, self.ipv6);

        if isIpv4 || isIpv6 {
            return ();
        } else {
            return error("Found invalid ip address in headers");
        }
    }
}
