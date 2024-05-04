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

package com.wso2.openbanking.fdx.gateway.util;

import com.wso2.openbanking.fdx.gateway.testutils.GatewayTestDataProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for FDX Gateway Utils.
 */
public class FDXGatewayUtilsTest {

    @Test(dataProvider = "uuid", dataProviderClass = GatewayTestDataProvider.class)
    public void testValidUUID(String uuid, boolean expectedResult) {
        Assert.assertEquals(FDXGatewayUtils.isValidUUID(uuid), expectedResult);
    }
}
