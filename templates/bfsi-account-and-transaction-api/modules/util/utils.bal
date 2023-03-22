// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

import ballerina/random;
import ballerina/time;
import ballerina/uuid;

# Method to generate the current date and time.
#
# + return - current date and time.
public isolated function getDateTime() returns string => time:utcToString(time:utcNow());

# Method to generate a future date and time.
#
# + return - future date and time.
public isolated function getFutureDateTime() returns string =>
    time:utcToString(time:utcAddSeconds(time:utcNow(), generateRandomSeconds()));

# Method to generate a past date and time.
#
# + return - past date and time.
public isolated function getPastDateTime() returns string =>
    time:utcToString(time:utcAddSeconds(time:utcNow(), generateRandomSeconds(true)));

# Method to generate a random time in seconds.
#
# + isNegative - if true, return a negative random time in seconds.
# + return - a random time in seconds.
isolated function generateRandomSeconds(boolean isNegative = false) returns time:Seconds {
    int randomSeconds = checkpanic random:createIntInRange(86400, 864000);
    return isNegative ? <time:Seconds>(randomSeconds * -1) : <time:Seconds>randomSeconds;
}

# Method to generate a random amount.
#
# + return - a random amount.
public isolated function getRandomAmount() returns string => (random:createDecimal() * 1000).toFixedString(2);

# Method to generate a random UUID.
#
# + return - a random UUID.
public isolated function getRandomId() returns string => uuid:createType4AsString();

# Method to check whether a given string is empty.
#
# + value - the string to be checked
# + return - true if the string is empty, else false
public isolated function isEmpty(string value) returns boolean => 
    let string trimmedValue = value.trim() in trimmedValue == "" || trimmedValue.length() == 0;
