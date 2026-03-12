import React, { useEffect, useState } from "react";
import { useAuthContext, type BasicUserInfo } from "@asgardeo/auth-react";

const App: React.FC = () => {
    const { state, signIn, getBasicUserInfo } = useAuthContext();
    const [user, setUser] = useState<BasicUserInfo | null>(null);

    useEffect(() => {
        if (!state.isAuthenticated && !state.isLoading) {
            signIn();
        }
    }, [state.isAuthenticated, state.isLoading]);

    useEffect(() => {
        console.log("isAuthenticated:", state.isAuthenticated); // ← check this
        if (state.isAuthenticated) {
            getBasicUserInfo()
                .then((info) => {
                    console.log("RAW user info:", info); // ← check this
                    setUser(info);
                })
                .catch((err) => {
                    console.error("getBasicUserInfo error:", err); // ← check this
                });
        }
    }, [state.isAuthenticated]);

    if (state.isLoading || !state.isAuthenticated) {
        return <div>Loading...</div>;
    }

    return (
        <div>
            <h1>Welcome, {user?.username}!</h1>
            <pre>{JSON.stringify(user, null, 2)}</pre> {/* ← shows ALL fields on screen */}
        </div>
    );
};

export default App;