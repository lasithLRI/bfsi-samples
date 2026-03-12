import React from "react";
import ReactDOM from "react-dom/client";
import { AuthProvider } from "@asgardeo/auth-react";
import App from "./App";
import { authConfig } from "./authConfig";

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
    <React.StrictMode>
        <AuthProvider config={authConfig}>
            <App />
        </AuthProvider>
    </React.StrictMode>
);