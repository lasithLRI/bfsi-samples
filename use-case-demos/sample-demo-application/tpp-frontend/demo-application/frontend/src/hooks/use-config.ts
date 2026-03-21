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
import type { Config, ConfigResponse } from "./config-interfaces";
import { loadConfigFile } from "../utility/config-loader";
import { api } from "../utility/api.ts";
import { resolveImageUrl } from "../utility/image-utils.ts";

export const useConfig = () => {
    const [config, setConfig] = useState<Config | null>(null);
    const [error, setError] = useState<Error | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [refreshKey, setRefreshKey] = useState(0);

    const refetch = () => setRefreshKey(k => k + 1);

    useEffect(() => {
        setIsLoading(true);
        Promise.all([
            loadConfigFile(),
            api.get<ConfigResponse>('initialize')
                .catch((err) => {
                    console.error('Backend config failed:', err);
                    return null;
                })
        ])
            .then(([localConfig, backendConfig]) => {
                const merged: Config = {
                    ...localConfig,
                    ...(backendConfig ?? {})
                };

                // ── Patch user image ──────────────────────────────────────
                if (merged.user?.image) {
                    merged.user = {
                        ...merged.user,
                        image: resolveImageUrl(merged.user.image)
                    };
                }

                // ── Patch user background image ───────────────────────────
                if (merged.user?.background) {
                    merged.user = {
                        ...merged.user,
                        background: resolveImageUrl(merged.user.background)
                    };
                }

                // ── Patch bank images ─────────────────────────────────────
                if (merged.banks) {
                    merged.banks = merged.banks.map((bank) => ({
                        ...bank,
                        image: resolveImageUrl(bank.image)
                    }));
                }

                setConfig(merged);
            })
            .catch(setError)
            .finally(() => setIsLoading(false));
    }, [refreshKey]);

    return { data: config, error, isLoading, refetch };
};
