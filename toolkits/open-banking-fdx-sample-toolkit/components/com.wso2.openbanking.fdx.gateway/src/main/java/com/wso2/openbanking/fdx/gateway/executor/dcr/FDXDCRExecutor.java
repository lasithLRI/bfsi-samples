/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.fdx.gateway.executor.dcr;

import com.wso2.openbanking.accelerator.gateway.executor.dcr.DCRExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OpenBankingExecutorError;
import com.wso2.openbanking.fdx.gateway.util.FDXGatewayConstants;
import com.wso2.openbanking.fdx.gateway.util.FDXGatewayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Executor for adding X-FAPI-INTERACTION-ID to DCR response headers.
 */
public class FDXDCRExecutor extends DCRExecutor {
    private static final Log log = LogFactory.getLog(FDXDCRExecutor.class);
    protected String interactionId;

    @Override
    public void preProcessRequest(OBAPIRequestContext obapiRequestContext) {
        if (obapiRequestContext.getMsgInfo().getHeaders()
                .containsKey(FDXGatewayConstants.INTERACTION_ID_HEADER)) {
            String xFapiInteractionId = obapiRequestContext.getMsgInfo().getHeaders()
                    .get(FDXGatewayConstants.INTERACTION_ID_HEADER);
            if (FDXGatewayUtils.isValidUUID(xFapiInteractionId)) {
                interactionId = xFapiInteractionId;
            } else {
                log.error("Invalid interaction ID format. Must be a UUID.");
                handleBadRequestError(obapiRequestContext, "Invalid interaction ID provided.");
                return;
            }
        } else {
            handleBadRequestError(obapiRequestContext, "Mandatory header x-fapi-interaction-id is not provided");
            return;
        }

        super.preProcessRequest(obapiRequestContext);
    }

    @Override
    public void postProcessRequest(OBAPIRequestContext obapiRequestContext) {

        super.postProcessRequest(obapiRequestContext);
    }

    @Override
    public void preProcessResponse(OBAPIResponseContext obapiResponseContext) {

        super.preProcessResponse(obapiResponseContext);
    }


    @Override
    public void postProcessResponse(OBAPIResponseContext obapiResponseContext) {

        super.postProcessResponse(obapiResponseContext);
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put(FDXGatewayConstants.INTERACTION_ID_HEADER, interactionId);
        obapiResponseContext.setAddedHeaders(responseHeaders);
    }

    private void handleBadRequestError(OBAPIRequestContext obapiRequestContext, String message) {

        OpenBankingExecutorError error = new OpenBankingExecutorError("Bad request",
                "invalid_header_fields", message, "400");
        ArrayList<OpenBankingExecutorError> executorErrors = obapiRequestContext.getErrors();
        executorErrors.add(error);
        obapiRequestContext.setError(true);
        obapiRequestContext.setErrors(executorErrors);

    }
}
