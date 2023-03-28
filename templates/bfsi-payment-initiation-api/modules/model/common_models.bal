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
import ballerina/random;
import ballerina/time;
import ballerina/uuid;

# The Risk section is sent by the initiating party to the bank.
# It is used to specify additional details for risk scoring for Payments.
public type Risk record {|
    # Specifies the payment context
    # * BillPayment - @deprecated
    # * EcommerceGoods - @deprecated
    # * EcommerceServices - @deprecated
    # * Other - @deprecated
    # * PartyToParty - @deprecated
    string PaymentContextCode?;
    # Category code conform to ISO 18245, related to the type of services or goods the merchant provides for the transaction.
    @constraint:String {maxLength: 4, minLength: 3}
    string MerchantCategoryCode?;
    # The unique customer identifier of the user with the merchant.
    @constraint:String {maxLength: 70, minLength: 1}
    string MerchantCustomerIdentification?;
    # Indicates if Payee has a contractual relationship with the TPP.
    boolean ContractPresentInidicator?;
    # Indicates if TPP has immutably prepopulated payment details in for the user.
    boolean BeneficiaryPrepopulatedIndicator?;
    # Category code, related to the type of services or goods that corresponds to the underlying purpose of the payment that conforms to Recommended Purpose Code in ISO 20022 Payment Messaging List
    @constraint:String {maxLength: 4, minLength: 3}
    string PaymentPurposeCode?;
    # Specifies the extended type of account.
    string BeneficiaryAccountType?;
    # Information that locates and identifies a specific address,
    # as defined by postal services or in free format text.
    PostalAddress DeliveryAddress?;
|};

# Unambiguous identification of the refund account to which a refund will be made as a result of the transaction.
public type DataRefund record {|
    # Provides the details to identify an account.
    CreditorAccount Account;
|};

# Set of elements used to provide details of a charge for the payment initiation.
public type DataCharges record {|
    # Specifies which party/parties will bear the charges associated with the processing of the payment transaction.
    string ChargeBearer;
    # Charge type, in a coded form.
    string Type;
    # Amount of money associated with the charge type.
    Amount Amount;
|};

# The multiple authorisation flow response from the bank.
public type MultiAuthorisation record {|
    # Specifies the status of the authorisation flow in code form.
    string Status;
    # Number of authorisations required for payment order (total required at the start of the multi authorisation journey).
    int NumberRequired?;
    # Number of authorisations received.
    int NumberReceived?;
    # Last date and time at the authorisation flow was updated.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string LastUpdateDateTime?;
    # Date and time at which the requested authorisation flow must be completed.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string ExpirationDateTime?;
|};

# Links relevant to the payload.
public type Links record {|
    # Self Link relevant to the payload
    string Self;
    # First Link relevant to the payload
    string First?;
    # Prev Link relevant to the payload
    string Prev?;
    # Next Link relevant to the payload
    string Next?;
    # Last Link relevant to the payload
    string Last?;
|};

# Meta Data relevant to the payload.
public type Meta record {|
    # Count of Metadata
    int TotalPages?;
    # All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string FirstAvailableDateTime?;
    # All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string LastAvailableDateTime?;
|};

# Amount of money of the cash balance.
public type Amount record {|
    # A number of monetary units specified in an active currency where the unit of currency is explicit and compliant with ISO 4217.
    string Amount = getRandomAmount();
    # A code allocated to a currency by a Maintenance Agency under an international identification scheme, as described in the latest edition of the international standard ISO 4217 (Codes for the representation of currencies and funds).
    string Currency = "USD";
|};

# Provides the details to identify the beneficiary account.
public type CreditorAccount record {|
    # Name of the identification scheme, in a coded form as published in an external list.
    string SchemeName;
    # Beneficiary account identification.
    string Identification;
    # The account name is the name or names of the account owner(s) represented at an account level, as displayed by the bank's online channels.
    # Note, the account name is not the product name or the nickname of the account.
    string Name?;
    # This is secondary identification of the account, as assigned by the account servicing institution. 
    # This can be used by building societies to additionally identify accounts with a roll number (in addition to a sort code and account number combination).
    string SecondaryIdentification?;
|};

# Provides the details to identify the beneficiary account.
public type DebtorAccount record {|
    # Name of the identification scheme, in a coded form as published in an external list.
    string SchemeName;
    # Beneficiary account identification.
    string Identification;
    # The account name is the name or names of the account owner(s) represented at an account level, as displayed by the bank's online channels.
    # Note, the account name is not the product name or the nickname of the account.
    string Name?;
    # This is secondary identification of the account, as assigned by the account servicing institution. 
    # This can be used by building societies to additionally identify accounts with a roll number (in addition to a sort code and account number combination).
    string SecondaryIdentification?;
|};

# Party that manages the account on behalf of the account owner, that is manages the registration and booking of entries on the account, calculates balances on the account and provides information about the account.
# This is the servicer of the beneficiary account.
public type CreditorAgent record {|
    # Name of the identification scheme, in a coded form as published in an external list.
    string SchemeName;
    # Unique and unambiguous identification of the servicing institution.
    string Identification;
|};

# Information that locates and identifies a specific address, as defined by postal services.
public type PostalAddress record {|
    # Identifies the nature of the postal address.
    string AddressType?;
    # Identification of a division of a large organisation or building.
    string Department?;
    # Identification of a sub-division of a large organisation or building.
    string SubDepartment?;
    # Name of a street or thoroughfare.
    string StreetName?;
    # Number that identifies the position of a building on a street.
    string BuildingNumber?;
    # Identifier consisting of a group of letters and/or numbers that is added to a postal address to assist the sorting of mail.
    string PostCode?;
    # Name of a built-up area, with defined boundaries, and a local government.
    string TownName?;
    # Identifies a subdivision of a country such as state, region, county.
    string CountrySubDivision?;
    # Nation with its own government.
    string Country?;
    # Describes address line list
    string[] AddressLine?;
|};

# Get current Date and Time.
#
# + return - current date and time.
public isolated function getDateTime() returns string => time:utcToString(time:utcNow());

# Get future Date and Time.
#
# + return - future date and time.
public isolated function getFutureDateTime() returns string =>
time:utcToString(time:utcAddSeconds(time:utcNow(), generateRandomSeconds()));

# Get past Date and Time.
#
# + return - past date and time.
public isolated function getPastDateTime() returns string =>
time:utcToString(time:utcAddSeconds(time:utcNow(), generateRandomSeconds(true)));

# Generate a random time in seconds.
#
# + isNegative - if true, return a negative random time in seconds.
# + return - a random time in seconds.
isolated function generateRandomSeconds(boolean isNegative = false) returns time:Seconds {
    int randomSeconds = checkpanic random:createIntInRange(86400, 864000);
    return isNegative ? <time:Seconds>(randomSeconds * -1) : <time:Seconds>randomSeconds;
}

# Generate a random amount.
#
# + return - a random amount.
public isolated function getRandomAmount() returns string =>
(random:createDecimal() * 1000).toFixedString(2);

# Generate a random UUID.
#
# + return - a random UUID.
public isolated function getRandomId() returns string => uuid:createType4AsString();
