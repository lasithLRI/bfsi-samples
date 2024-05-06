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

package com.wso2.openbanking.fdx.gateway.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.UUID;

 /**
  *  FDX Gateway Utils.
  */
public class FDXGatewayUtils {

     private static final Log log = LogFactory.getLog(FDXGatewayUtils.class);

     /**
      * Checks if the given string is a valid UUID.
      *
      * @param uuidString the string to validate
      * @return true if the string is a valid UUID, false otherwise
      */
    public static boolean isValidUUID(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            log.error("Invalid interaction ID format. Must be a UUID.", e);
            return false;
        }
    }

}

