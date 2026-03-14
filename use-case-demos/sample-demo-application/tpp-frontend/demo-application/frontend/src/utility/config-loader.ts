import type {AppConfig} from "./custom-interfaces.ts";

export const baseConfigFileLocation = '/ob-demo-backend-1.0.0/configurations/config.json';

let config : AppConfig | null = null;

export async function loadConfigFile(): Promise<AppConfig> {
    if(config) return config;
    const response = await fetch(baseConfigFileLocation)
    if (!response.ok) {
        throw new Error(`Failed to load config: ${response.status}`);
    }
    config = await response.json();
    return config as AppConfig;
}
