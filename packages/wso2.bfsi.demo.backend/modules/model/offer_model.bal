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

# Set of elements used to define the offer details.
public type Offer record {
    # A unique and immutable identifier used to identify the account resource. This identifier has no meaning to the account owner.
    readonly string AccountId;
    # A unique and immutable identifier used to identify the offer resource. This identifier has no meaning to the account owner.
    readonly string OfferId;
    # Offer type, in a coded form.
    string OfferType?;
    # Further details of the offer.
    string Description?;
    # Date and time at which the offer starts.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string StartDateTime = util:getPastDateTime();
    # Date and time at which the offer ends.All dates in the JSON payloads are represented in ISO 8601 date-time format. 
    # All date-time fields in responses must include the timezone. An example is below:
    # 2017-04-05T10:43:07+00:00
    string EndDateTime = util:getFutureDateTime();
    # Rate associated with the offer type.
    string Rate = util:getRandomAmount();
    # Value associated with the offer type.
    int Value?;
    # Further details of the term of the offer.
    string Term?;
    # URL (Uniform Resource Locator) where documentation on the offer can be found
    string URL?;
    # Amount of money associated with the offer type.
    Amount Amount?;
    # Fee associated with the offer type.
    Amount Fee?;
};
