import React from "react";
import ReactDOM from "react-dom/client";
import { AuthProvider } from "@asgardeo/auth-react";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { authConfig } from "./authConfig";
import './index.scss'

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
    <React.StrictMode>
        <BrowserRouter>
            <AuthProvider config={authConfig}>
                <App />
            </AuthProvider>
        </BrowserRouter>
    </React.StrictMode>
);
