// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.

// This software is the property of WSO2 LLC. and its suppliers, if any.
// Dissemination of any information or reproduction of any material contained
// herein is strictly forbidden, unless permitted by WSO2 in accordance with
// the WSO2 Software License available at: https://wso2.com/licenses/eula/3.2
// For specific language governing the permissions and limitations under
// this license, please see the license as well as any agreement you’ve
// entered into with WSO2 governing the purchase of this software and any
// associated services.

# Product details associated with the Account.
public type Product record {
    # The name of the Product used for marketing purposes from a customer perspective. I.e. what the customer would recognise.
    string ProductName?;
    # The unique ID that has been internally assigned by the financial institution to each of the current account banking products they market to their retail and/or small to medium enterprise (SME) customers.
    readonly string ProductId;
    # A unique and immutable identifier used to identify the account resource. This identifier has no meaning to the account owner.
    readonly string AccountId;
    # Any secondary Identification which  supports Product Identifier to uniquely identify the current account banking products.
    string SecondaryProductId?;
    # Product type : Personal Current Account, Business Current Account
    string ProductType;
    # Unique and unambiguous identification of a  Product Marketing State.
    string MarketingStateId?;
    # Other product type details associated with the account.
    OtherproductType OtherProductType?;
};

# Other product type details associated with the account.
public type OtherproductType record {
    # Long name associated with the product
    string Name;
    # Description of the Product associated with the account
    string Description;
    # Description of the product details
    ProductDetails ProductDetails?;
    # Details about the interest that may be payable to the Account holders
    CreditInterest CreditInterest?;
};

# Description of the product details.
public type ProductDetails record {
    # Holds product segments
    string[] Segment?;
    # The length/duration of the fee free period
    int FeeFreeLength?;
    # The unit of period (days, weeks, months etc.) of the promotional length
    string FeeFreeLengthPeriod?;
    # The maximum relevant charges that could accrue as defined fully in Part 7 of the CMA order
    string MonthlyMaximumCharge?;
    # Holds product notes
    string[] Notes?;
    # Holds other segment details
    OtherSegment OtherSegment?;
};

# Holds other segment details.
public type OtherSegment record {
    # The four letter Mnemonic used within an XML file to identify a code
    string Code?;
    # Long name associated with the code
    string Name;
    # Description to describe the purpose of the code
    string Description;
};

# Details about the interest that may be payable to the Account holders.
public type CreditInterest record {
    # The group of tiers or bands for which credit interest can be applied.
    TierBandSet[] TierBandSet;
};

# The group of tiers or bands for which credit interest can be applied.
public type TierBandSet record {
    # The methodology of how credit interest is paid/applied. It can be:-
    # 1. Banded
    # Interest rates are banded. i.e. Increasing rate on whole balance as balance increases.
    # 2. Tiered
    # Interest rates are tiered. i.e. increasing rate for each tier as balance increases, but interest paid on tier fixed for that tier and not on whole balance.
    # 3. Whole
    # The same interest rate is applied irrespective of the product holder's account balance
    string TierBandMethod;
    # Methods of calculating interest
    string CalculationMethod?;
    # Describes whether accrued interest is payable only to the BCA or to another bank account
    string Destination;
    # Describes tierband notes
    string[] Notes?;
    # Describes calculation method
    OtherCodeType OtherCalculationMethod?;
    # Describes destination
    OtherCodeType OtherDestination?;
    # Describes optional tier bands list
    Tierband[] TierBand;
};

# Describes destinations or calculation methods.
public type OtherCodeType record {
    # The four letter Mnemonic used within an XML file to identify a code
    string Code?;
    # Long name associated with the code
    string Name;
    # Description to describe the purpose of the code
    string Description;
};

# Tier Band Details.
public type Tierband record {
    # Unique and unambiguous identification of a  Tier Band for the Product.
    string Identification?;
    # Minimum deposit value for which the credit interest tier applies.
    string TierValueMinimum;
    # Maximum deposit value for which the credit interest tier applies.
    string TierValueMaximum?;
    # How often is credit interest calculated for the account.
    string CalculationFrequency?;
    # How often is interest applied to the Product for this tier/band i.e. how often the financial institution pays accumulated interest to the customer's account.
    string ApplicationFrequency;
    # Amount on which Interest applied.
    string DepositInterestAppliedCoverage?;
    # Type of interest rate, Fixed or Variable
    string FixedVariableInterestRateType;
    # The annual equivalent rate (AER) is interest that is calculated under the assumption that any interest paid is combined with the original balance and the next interest payment will be based on the slightly higher account balance. Overall, this means that interest can be compounded several times in a year depending on the number of times that interest payments are made. 
    # Read more: Annual Equivalent Rate (AER) http://www.investopedia.com/terms/a/aer.asp#ixzz4gfR7IO1A
    string AER;
    # Interest rate types, other than AER, which financial institutions may use to describe the annual interest rate payable to the account holder's account.
    string BankInterestRateType?;
    # Bank Interest for the product
    string BankInterestRate?;
    # Describes tierband notes list
    string[] Notes?;
    # Other interest rate types which are not available in the standard code list
    OtherCodeType OtherBankInterestType?;
    # Other application frequencies that are not available in the standard code list
    OtherCodeType OtherApplicationFrequency?;
    # Other calculation frequency which is not available in the standard code set.
    OtherCodeType OtherCalculationFrequency?;
};
