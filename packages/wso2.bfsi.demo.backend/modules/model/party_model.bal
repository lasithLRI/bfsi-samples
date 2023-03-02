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

# Set of elements used to define the party details.
public type Party record {
    # A unique and immutable identifier used to identify the party resource. This identifier has no meaning to the account owner.
    readonly string PartyId;
    # Number assigned by an agent to identify its customer.
    string PartyNumber = util:getRandomId();
    # Party type, in a coded form.
    string PartyType?;
    # Name by which a party is known and which is usually used to identify that party.
    string Name?;
    # Specifies a character string with a maximum length of 350 characters.
    string FullLegalName?;
    # Legal standing of the party.
    string LegalStructure?;
    # Beneficial ownership of the party
    boolean BeneficialOwnership?;
    # A party’s role with respect to the related account.
    string AccountRole?;
    # Address for electronic mail (e-mail).
    string EmailAddress?;
    # Collection of information that identifies a phone number, as defined by telecom services.
    string Phone?;
    # Collection of information that identifies a mobile phone number, as defined by telecom services.
    string Mobile?;
    # The Party's relationships with other resources.
    PartyRelationship Relationships?;
    # Postal address of a party.
    Address[] Address?;
};

# The Party's relationships with other resources.
public type PartyRelationship record {
    # Relationship to the Account resource.
    PartyRelationshipAccount Account?;
};

# The Party's relationships with other resources.
public type PartyRelationshipAccount record {
    # Absolute URI to the related resource.
    string Related;
    # Unique identification as assigned by the bank to uniquely identify the related resource.
    string Id;
};

# Postal address of a party.
public type Address record {
    # Identifies the nature of the postal address.
    string AddressType?;
    # Name of the address line
    string[] AddressLine?;
    # Name of a street or thoroughfare.
    string StreetName?;
    # Number that identifies the position of a building on a street.
    string BuildingNumber?;
    # Identifier consisting of a group of letters and/or numbers that is added to a postal address to assist the sorting of mail.
    string PostCode?;
    # Name of a built-up area, with defined boundaries, and a local government.
    string TownName?;
    # Identifies a subdivision of a country eg, state, region, county.
    string CountrySubDivision?;
    # Nation with its own government, occupying a particular territory.
    string Country;
};
