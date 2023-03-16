Use template (BFSI Account and Transaction API) to create a project exposing Open Banking Accounts and Transaction REST APIs

## Use case

When the different Open Banking endpoints of the service is invoked, the service will respond with a corresponding JSON response.

## Using the Template

### Setup and run

1.  Create Ballerina project from this template.

    ```ballerina
    bal new -t wso2bfsi/bfsi_account_and_transaction_api <PROJECT_NAME>
    ```

2.  Run the project.

    ```ballerina
    bal run
    ```

3.  Invoke the API.

    Example to retrieve an account by ID:

    ```
    curl GET https://<host>:<port>/accounts/A001
    ```

    Example to retrieve transactions:

    ```
    curl GET https://<host>:<port>/transactions
    ```

### Setup and run on Choreo

1. Perform steps 1 & 2 as mentioned above.

2. Push the project to a new GitHub repository.

3. Follow instructions to [connect the project repository to Choreo](https://wso2.com/choreo/docs/develop/manage-repository/connect-your-own-github-repository-to-choreo/)

4. Deploy API by following [instructions to deploy](https://wso2.com/choreo/docs/get-started/tutorials/create-your-first-rest-api/#step-2-deploy) and [test](https://wso2.com/choreo/docs/get-started/tutorials/create-your-first-rest-api/#step-3-test)

5. Navigate to the Manage > Settings section of the newly deployed API and add the following headers to the `Access Control Allow Headers`.

   - x-fapi-auth-date
   - x-fapi-customer-ip-address
   - x-fapi-interaction-id
   - x-customer-user-agent

6. Invoke the API.

   Sample URL to retrieve a transactions by ID:

   `https://<domain>/<component>/<version>/transactions/T001`
