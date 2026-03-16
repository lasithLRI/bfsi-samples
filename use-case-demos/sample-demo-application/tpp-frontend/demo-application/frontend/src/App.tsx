import React, { useEffect, useState } from "react";
import { useAuthContext, type BasicUserInfo } from "@asgardeo/auth-react";
import { Routes, Route, Navigate } from "react-router-dom";
import "./App.scss";
import Home from "./pages/home-page/home.tsx";
import AppThemeProvider from "./providers/app-theme-provider.tsx";
import useConfigContext from "./hooks/use-config-context.ts";
import AddAccountsPage from "./pages/add-accounts-page/add-accounts-page.tsx";

const App: React.FC = () => {
    const { state, signIn, getBasicUserInfo } = useAuthContext();
    const [user, setUser] = useState<BasicUserInfo | null>(null);

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

    if (isLoading || !state.isAuthenticated || !user) {
        return <div>Loading...</div>;
    }

    const base = appInfo.route; // e.g. "account-central"

    const resolvedUserInfo = {
        name: user?.givenName && user?.familyName
            ? `${user.givenName} ${user.familyName}`
            : user?.username || user?.displayName || "User",
        image: userInfo.image,
        background: userInfo.background,
    };

    return (
        <AppThemeProvider color={colors}>
            <Routes>

                {/* Redirect root to app base route */}
                <Route path="/" element={<Navigate to={`/${base}`} replace />} />

                {/* Home */}
                <Route path={`/${base}`} element={
                    <Home
                        name={appInfo.applicationName}
                        userInfo={resolvedUserInfo}
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
                } />

                {/* Add Account */}
                <Route path={`/${base}/accounts`} element={
                    <AddAccountsPage bankInformations={banksList} />
                } />




                {/* Fallback */}
                <Route path="*" element={<Navigate to={`/${base}`} replace />} />

            </Routes>
        </AppThemeProvider>
    );
};

export default App;
