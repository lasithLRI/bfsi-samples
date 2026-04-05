/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

package com.wso2.openbanking.demo.models;

import com.wso2.openbanking.demo.utils.ConfigLoader;

/** AddAccountBankInfo implementation. */
public class AddAccountBankInfo {
    String name;
    String image;
    boolean flag;

    public AddAccountBankInfo(String name, String image) {
        this.name = name;
        this.image = image;
        if (name.equalsIgnoreCase(ConfigLoader.getMockBankName())) {
            flag = true;
        }
    }

    /**
     * Executes the getName operation and modify the payload if necessary.
     */
    public String getName() {

        return name;
    }

    /**
     * Executes the setName operation and modify the payload if necessary.
     *
     * @param name            The name parameter
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Executes the getImage operation and modify the payload if necessary.
     */
    public String getImage() {

        return image;
    }

    /**
     * Executes the isFlag operation and modify the payload if necessary.
     */
    public boolean isFlag() {

        return flag;
    }
}
