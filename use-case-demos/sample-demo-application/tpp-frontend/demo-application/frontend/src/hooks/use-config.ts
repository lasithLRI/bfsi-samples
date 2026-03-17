import { useState, useEffect } from 'react';
import type { Config, ConfigResponse } from "./config-interfaces";
import { loadConfigFile } from "../utility/config-loader";
import { api } from "../utility/api.ts";
import { resolveImageUrl } from "../utility/image-utils.ts";

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
    }, []);

    return { data: config, error, isLoading };
};
