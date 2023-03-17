Use template (BFSI Payment Initiation API) to create a project exposing Open Banking Payment Initiation REST APIs

## Use case
The `BFSI Payment Initiation API` template can be used to initiate and retrieve payments for a specific bank user. The API endpoints exposed from this API allow to:

 - Submit the payment-order for processing.
 - Optionally retrieve the status of a payment-order resource.
 - Optionally retrieve the details of a payment-order resource.

## Using the Template

### Setup and run

1.  Create Ballerina project from this template.

    ```ballerina
    bal new -t wso2bfsi/bfsi_payment_initiation_api <PROJECT_NAME>
    ```
2.  If further modifications are needed, open the `<PROJECT_NAME>` directory in your favorite IDE and customize the code.

3. Run the project.

    ```ballerina
    bal run
    ```

4. Invoke the API.

    Example to Initiate a payment:

    ```
    curl --location 'http://<host>:<port>/domestic-payments' \
    --header 'x-idempotency-key: 1234' \
    --header 'x-jws-signature: ivenqiuanwbvuqewcjabvieknwrnuivaed' \
    --header 'Content-Type: application/json' \
    --data '{
        "Data": {
            "ConsentId": "58923",
            "Initiation": {
            "InstructionIdentification": "ACME412",
            "EndToEndIdentification": "FRESCO.21302.GFX.20",
            "InstructedAmount": {
                "Amount": "165.88",
                "Currency": "GBP"
            },
            "CreditorAccount": {
                "SchemeName": "UK.OBIE.SortCodeAccountNumber",
                "Identification": "08080021325698567890",
                "Name": "ACME Inc",
                "SecondaryIdentification": "0002"
            },
            "RemittanceInformation": {
                "Reference": "FRESCO-101",
                "Unstructured": "Internal ops code 5120101"
            }
            }
        },
        "Risk": {
            "PaymentContextCode": "EcommerceMerchantInitiatedPayment",
            "ContractPresentInidicator": false,
            "PaymentPurposeCode": "EPAY",
            "BeneficiaryAccountType": "Business",
            "MerchantCustomerIdentification": "053598653254",
            "DeliveryAddress": {
            "AddressLine": [
                "Flat 7",
                "Acacia Lodge"
            ],
            "StreetName": "Acacia Avenue",
            "BuildingNumber": "27",
            "PostCode": "GU31 2ZZ",
            "TownName": "Sparsholt",
            "CountrySubDivision": "Wessex",
            "Country": "UK"
            }
        }
    }'
    ```
    Example to retrieve payment by payment ID:

    ```
    curl GET 'https://<host>:<port>/domestic-payments/P001'
    ```

     Example to retrieve payment details by payment ID:

    ```
    curl GET 'https://<host>:<port>/domestic-payments/P001/payment-details'
    ```

### Setup and run on Choreo

1. Perform steps 1 to 3 as mentioned above.

2. Push the project to a new Github repository.

3. Follow instructions to [connect the project repository to Choreo](https://wso2.com/choreo/docs/develop/manage-repository/connect-your-own-github-repository-to-choreo/)

4. Deploy API by following [instructions to deploy](https://wso2.com/choreo/docs/get-started/tutorials/create-your-first-rest-api/#step-2-deploy) and [test](https://wso2.com/choreo/docs/get-started/tutorials/create-your-first-rest-api/#step-3-test)

5. Navigate to the Manage > Settings section of the newly deployed API, enable `CORS Configuration` and add the following headers to the `Access Control Allow Headers`.

   - x-fapi-auth-date
   - x-fapi-customer-ip-address
   - x-fapi-interaction-id
   - x-customer-user-agent
   - x-idempotency-key
   - x-jws-signature

6. Invoke the API.

    Sample URL to retrieve payment by payment ID:
    `https://b643d191-c562-4f98-9a19-0831f622b020-prod.e1-us-east-azure.choreoapis.dev/xiqs/bfsi-account-and-transaction-api/1.0.0/transactions/T001`

    Format: `https://<domain>/<component>/<version>/transactions/T001`
