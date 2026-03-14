import React, { useEffect, useState } from "react";
import { useAuthContext, type BasicUserInfo } from "@asgardeo/auth-react";
import "./App.scss";
import type { AppConfig } from "./utility/custom-interfaces.ts";
import { loadConfigFile } from "./utility/config-loader.ts";
import HomePage from "./home-page/home-page.tsx";
import AppColorsProvider from "./providers/app-colors-provider.tsx";

const App: React.FC = () => {
    const { state, signIn, getBasicUserInfo } = useAuthContext();
    const [user, setUser] = useState<BasicUserInfo | null>(null);
    const [config, setConfig] = useState<AppConfig | null>(null);

    useEffect(() => {
        if (!state.isAuthenticated && !state.isLoading) {
            signIn();
        }
    }, [state.isAuthenticated, state.isLoading]);

    useEffect(() => {
        if (state.isAuthenticated) {
            loadConfigFile()
                .then(setConfig)
                .catch((err) => console.error("Config load error:", err));

            getBasicUserInfo()
                .then((info) => {
                    console.log("RAW user info:", info);
                    setUser(info);
                })
                .catch((err) => console.error("getBasicUserInfo error:", err));
        }
    }, [state.isAuthenticated]);

    if (state.isLoading || !state.isAuthenticated || !config) {
        return <div>Loading...</div>;
    }

    return (
        <AppColorsProvider colors={config.colors}>
            <HomePage
                appName={config.name.applicationName}
                // userName={user?.username ?? ""}
                userInfo={{
                    name: user?.username ?? "",
                    image: config.user.image,
                    background: config.user.background,
                }}
            />
        </AppColorsProvider>
    );
};

export default App;
