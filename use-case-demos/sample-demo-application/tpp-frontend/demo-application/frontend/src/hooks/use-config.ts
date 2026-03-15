/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * ...
 */

import { useState, useEffect } from 'react';
import type { Config, ConfigResponse } from "./config-interfaces";
import { loadConfigFile } from "../utility/config-loader";
import { api } from "../utility/api.ts";
export const useConfig = () => {
    const [config, setConfig] = useState<Config | null>(null);
    const [error, setError] = useState<Error | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        Promise.all([
            loadConfigFile(),
            api.get<ConfigResponse>('initialize')
                .catch((err) => {
                    console.error('Backend config failed:', err);
                    return null;
                })
        ])
            .then(([localConfig, backendConfig]) => {
                setConfig({
                    ...localConfig,
                    ...(backendConfig ?? {})
                });
            })
            .catch(setError)
            .finally(() => setIsLoading(false));
    }, []);

    return { data: config, error, isLoading };  // ← isLoading already here
};