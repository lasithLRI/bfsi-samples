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

import { useState, useEffect } from 'react';
import type { Config } from "./config-interfaces";
import { loadConfigFile } from "../utility/config-loader";

/**
 * @function useConfig
 * @description A custom hook to fetch the application's primary configuration object (`config.json`).
 */
export const useConfig = () => {
    const [config, setConfig] = useState<Config | null>(null);
    const [error, setError] = useState<Error | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        loadConfigFile()
            .then(setConfig)
            .catch(setError)
            .finally(() => setIsLoading(false));
    }, []);

    return { data: config, error, isLoading };
};
