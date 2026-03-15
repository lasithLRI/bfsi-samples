import React, { useEffect, useState } from "react";
import { useAuthContext, type BasicUserInfo } from "@asgardeo/auth-react";
import "./App.scss";
import Home from "./pages/home-page/home.tsx";
import AppThemeProvider from "./providers/app-theme-provider.tsx";
import useConfigContext from "./hooks/use-config-context.ts";

const App: React.FC = () => {
    const { state, signIn, getBasicUserInfo } = useAuthContext();
    const [user, setUser] = useState<BasicUserInfo | null>(null);

    // ✅ Call once at the top, destructure everything together
    const {
        isLoading,
        appInfo,
        userInfo,
        total,
        chartInfo,
        banksWithAccounts,
        transactions,
        standingOrderList,
        banksList,
        overlayInformation,
        transactionTableHeaderData,
        standingOrdersTableHeaderData,
        colors
    } = useConfigContext();

    useEffect(() => {
        if (!state.isAuthenticated && !state.isLoading) {
            signIn();
        }
    }, [state.isAuthenticated, state.isLoading, signIn]);

    useEffect(() => {
        if (state.isAuthenticated) {
            getBasicUserInfo()
                .then((info) => {
                    console.log("RAW user info:", info);
                    setUser(info);
                })
                .catch((err) => console.error("getBasicUserInfo error:", err));
        }
    }, [state.isAuthenticated, getBasicUserInfo]);

    // ✅ All conditional returns AFTER all hook calls
    if (isLoading || !state.isAuthenticated || !user) {
        return <div>Loading...</div>;
    }

    return (
        <AppThemeProvider color={colors}>
            <Home
                name={appInfo.applicationName}
                userInfo={{
                    name: user?.givenName && user?.familyName
                        ? `${user.givenName} ${user.familyName}`
                        : user?.username || user?.displayName || "User",
                    image: userInfo.image,
                    background: userInfo.background,
                }}
                total={total}
                chartData={chartInfo}
                banksWithAccounts={banksWithAccounts}
                transactions={transactions}
                standingOrderList={standingOrderList}
                appInfo={appInfo}
                banksList={banksList}
                overlayInformation={overlayInformation}
                transactionTableHeaderData={transactionTableHeaderData}
                standingOrdersTableHeaderData={standingOrdersTableHeaderData}
            />
        </AppThemeProvider>
    );
};

export default App;