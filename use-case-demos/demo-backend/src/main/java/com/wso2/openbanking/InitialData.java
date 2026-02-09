package com.wso2.openbanking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.models.*;
import com.wso2.openbanking.services.AppContext;
import com.wso2.openbanking.services.BankInfoService;
import com.wso2.openbanking.services.HttpTlsClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Path("")
public class InitialData {

    private final ObjectMapper objectMapper = new ObjectMapper();
    BankInfoService bankInfo = new BankInfoService();
    String requestStatus = "accounts";

    public InitialData() throws Exception {
    }

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode getData(){
        String fileName = "config.json";

        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)){

            if (inputStream == null) {
                return null;
            }

            return objectMapper.readTree(inputStream);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Path("/bank")
    public ConfigResponse getBankData(){

        bankInfo.loadBanks();

        return bankInfo.getConfigurations();
    }

    @GET
    @Path("/load-payment")
    @Produces(MediaType.APPLICATION_JSON)
    public LoadPaymentPageResponse getLoadPaymentData(){

        return bankInfo.getPaymentPageInfo();
    }

    @POST
    @Path("/payment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response makePayment(Payment payment) throws Exception {

        requestStatus = "payments";

        String url = bankInfo.paymentRequest(payment);
        Map<String,String> response = new HashMap<>();
        response.put("redirect", url);
        System.out.println("=====================");

         return Response.ok(response).build();
    }
    @GET
    @Path("/accounts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AddAccountBankInfo> addAccountBanks(){
        List<AddAccountBankInfo> banks = bankInfo.addNewAccount();
        return  banks;
    }

    @POST
    @Path("/addaccounts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response selectAccountToAdd(Map<String,String> reqBody) throws Exception {
        String url = bankInfo.processAddAccount(reqBody.get("bankName"));
        Map<String,String> response = new HashMap<>();
        response.put("redirect", url);
        System.out.println("=====================");

        return Response.ok(response).build();
    }

    @GET
    @Path("/redirected")
    @Produces("text/html")
    public Response redirectedPath() throws Exception {

        String frontendHome = "http://localhost:5173/";

        String html =
                "<html>\n" +
                        "<head>\n" +
                        "    <title>Completing Authentication...</title>\n" +
                        "    <style>body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; }</style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div id='status'>Finalizing your connection...</div>\n" +
                        "    <script>\n" +
                        "        const hash = window.location.hash.substring(1);\n" +
                        "        const params = new URLSearchParams(hash);\n" +
                        "\n" +
                        "        const idToken = params.get('id_token');\n" +
                        "        const code = params.get('code');\n" +
                        "        const state = params.get('state');\n" +
                        "        const sessionState = params.get('session_state');\n" +
                        "\n" +
                        "        // 1. Send data to your backend\n" +
                        "        fetch('https://tpp.local.ob/ob_demo_backend_war/init/processAuth?' +\n" +
                        "            'code=' + encodeURIComponent(code) +\n" +
                        "            '&state=' + encodeURIComponent(state) +\n" +
                        "            '&session_state=' + encodeURIComponent(sessionState)+\n" +
                        "            '&id_token=' + encodeURIComponent(idToken))\n" +
                        "        .then(response => {\n" +
                        "            if (response.ok) {\n" +
                        "                // 2. Success! Redirect to frontend home automatically\n" +
                        "                window.location.href = '" + frontendHome + "';\n" +
                        "            } else {\n" +
                        "                document.getElementById('status').innerHTML = 'Processing failed. Please try again.';\n" +
                        "            }\n" +
                        "        })\n" +
                        "        .catch(() => {\n" +
                        "            document.getElementById('status').innerHTML = 'Connection error.';\n" +
                        "        });\n" +
                        "    </script>\n" +
                        "</body>\n" +
                        "</html>";

        return Response.ok(html).build();
    }

    @GET
    @Path("/processAuth")
    public void processAuth(@QueryParam("code") String code, @QueryParam("state") String state, @QueryParam("session_state") String session, @QueryParam("id_token") String idToken ) throws Exception {


        System.out.println("code: " + code);

        String jti = new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toString();

        AppContext context = new AppContext("onKy05vpqDjTenzZSRjfSOfb3ZMa", "sCekNgSWIauQ34klRhDGqfwpjc4", "PS256", "JWT", jti);

        String redirectUri = "https://tpp.local.ob/ob_demo_backend_war/init/redirected";

        String body = "grant_type=authorization_code" +
                "&code=" + code +
                "&scope=accounts openid" +
                "&client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                "&client_id=" + context.getClientId() +
                "&client_assertion=" + context.createClientAsserstion() +
                "&redirect_uri=" + redirectUri;

        HttpTlsClient client = new HttpTlsClient("/obtransport.pem", "/obtransport.key", "/client-truststore.jks", "123456");

        String response = client.postAccesstoken("https://localhost:9446/oauth2/token", body);

        JSONObject json = new JSONObject(response);

        System.out.println("response: " + response);
        System.out.println(json.getString("access_token"));

        String accountsAccessToken = json.getString("access_token");



        String accessToken = json.getString("access_token");
        String refreshToken = json.getString("refresh_token");
        String scope = json.getString("scope");
        String idToken1 = json.getString("id_token");
        int expiresIn = json.getInt("expires_in");

        System.out.println("Access Token: " + accessToken);
        System.out.println("Refresh Token: " + refreshToken);
        System.out.println("Scope: " + scope);
        System.out.println("ID Token: " + idToken1);
        System.out.println("Expires In: " + expiresIn);


        System.out.println("Auth= " + accountsAccessToken);

        bankInfo.setAccessToken(accountsAccessToken);

        if (requestStatus == "accounts"){
            bankInfo.addMockBankAccountsInformation();
        }else if (requestStatus == "payments"){
            System.out.println("payments");
            bankInfo.addPaymentToAccount();
        }
    }

}
