/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.openbanking.fdx.gateway.executor.dcr;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigurationService;
import com.wso2.openbanking.accelerator.gateway.executor.dcr.DCRExecutor;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIRequestContext;
import com.wso2.openbanking.accelerator.gateway.executor.model.OBAPIResponseContext;
import com.wso2.openbanking.accelerator.gateway.internal.GatewayDataHolder;
import com.wso2.openbanking.accelerator.gateway.util.GatewayConstants;
import com.wso2.openbanking.fdx.gateway.util.FDXGatewayConstants;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.HttpMethod;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Test for FDX DCR executor.
 */
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({OBAPIRequestContext.class, GatewayDataHolder.class})
public class FDXDCRExecutorTest {

    @Mock
    Map<String, Object> urlMap = new HashMap<>();

    Map<String, Object> configMap = new HashMap<>();
    Map<String, List<String>> configuredAPIList = new HashMap<>();

    @Mock
    private GatewayDataHolder gatewayDataHolder;

    @Mock
    OpenBankingConfigurationService openBankingConfigurationService;

    @BeforeTest
    public void init() {

        MockitoAnnotations.initMocks(this);

        urlMap = new HashMap<>();
        urlMap.put("userName", "admin");
        urlMap.put("password", "admin".toCharArray());
        urlMap.put(GatewayConstants.IAM_HOSTNAME, "localhost");

        configMap.put("DCR.RequestJWTValidation", "false");

        List<String> roles = new ArrayList<>();
        configuredAPIList.put("DynamicClientRegistration", roles);
    }

    @Test
    public void testInvalidInteractionIdHeader() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        FDXDCRExecutor dcrExecutor = Mockito.spy(FDXDCRExecutor.class);

        msgInfoDTO.setHttpMethod(HttpMethod.GET);
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(FDXGatewayConstants.INTERACTION_ID_HEADER, "765489876548650");
        msgInfoDTO.setHeaders(requestHeaders);
        Mockito.doReturn(msgInfoDTO).when(obapiRequestContext).getMsgInfo();

        dcrExecutor.preProcessRequest(obapiRequestContext);

        Mockito.verify(obapiRequestContext).setError(true);
    }

    @Test
    public void testInteractionIdHeaderNotProvided() {

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        FDXDCRExecutor dcrExecutor = Mockito.spy(FDXDCRExecutor.class);

        msgInfoDTO.setHttpMethod(HttpMethod.GET);
        Map<String, String> requestHeaders = new HashMap<>();
        msgInfoDTO.setHeaders(requestHeaders);
        Mockito.doReturn(msgInfoDTO).when(obapiRequestContext).getMsgInfo();

        dcrExecutor.preProcessRequest(obapiRequestContext);

        Mockito.verify(obapiRequestContext).setError(true);
    }


    @Test
    public void testSetInteractionIdHeader() {

        Mockito.when(openBankingConfigurationService.getConfigurations()).thenReturn(configMap);
        Mockito.when(gatewayDataHolder.getOpenBankingConfigurationService())
                                        .thenReturn(openBankingConfigurationService);
        mockStatic(GatewayDataHolder.class);
        Mockito.when(GatewayDataHolder.getInstance()).thenReturn(gatewayDataHolder);

        OBAPIRequestContext obapiRequestContext = Mockito.mock(OBAPIRequestContext.class);
        FDXDCRExecutor fdxdcrExecutor = Mockito.spy(new FDXDCRExecutor());

        String uuid = "770aef3-6784-41f7-8e0e-ff5f97bddb3";

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHttpMethod(HttpMethod.GET);
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(FDXGatewayConstants.INTERACTION_ID_HEADER, uuid);
        msgInfoDTO.setHeaders(requestHeaders);
        Mockito.doReturn(msgInfoDTO).when(obapiRequestContext).getMsgInfo();

        fdxdcrExecutor.preProcessRequest(obapiRequestContext);

        Assert.assertEquals(fdxdcrExecutor.interactionId, uuid);
    }

    @Test
    public void testAddInteractionIdHeaderToResponseHeaders() {

        FDXDCRExecutor fdxdcrExecutor = Mockito.spy(new FDXDCRExecutor());
        String interactionId = "770aef3-6784-41f7-8e0e-ff5f97bddb3";
        fdxdcrExecutor.interactionId = interactionId;
        DCRExecutor.setUrlMap(urlMap);
        Map<String, String> responseHeaders = new HashMap<>();
        responseHeaders.put(FDXGatewayConstants.INTERACTION_ID_HEADER, interactionId);
        Mockito.when(openBankingConfigurationService.getAllowedAPIs()).thenReturn(configuredAPIList);
        Mockito.when(gatewayDataHolder.getOpenBankingConfigurationService())
                .thenReturn(openBankingConfigurationService);
        mockStatic(GatewayDataHolder.class);
        Mockito.when(GatewayDataHolder.getInstance()).thenReturn(gatewayDataHolder);

        OBAPIResponseContext obapiResponseContext = Mockito.mock(OBAPIResponseContext.class);
        MsgInfoDTO msgInfoDTO = Mockito.mock(MsgInfoDTO.class);
        Mockito.doReturn(msgInfoDTO).when(obapiResponseContext).getMsgInfo();
        Mockito.doReturn(HttpMethod.GET).when(msgInfoDTO).getHttpMethod();

        fdxdcrExecutor.postProcessResponse(obapiResponseContext);
        Mockito.verify(obapiResponseContext).setAddedHeaders(responseHeaders);
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}

