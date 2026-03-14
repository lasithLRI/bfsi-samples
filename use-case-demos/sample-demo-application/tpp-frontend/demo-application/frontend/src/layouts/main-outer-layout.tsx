import React from "react";
import Header from "../components/header/header";

interface MainOuterLayoutProps {
    appName: string;
    children: React.ReactNode;
}

const MainOuterLayout = ({ appName, children }: MainOuterLayoutProps) => {
    return (
        <div className="main-outer-container">
            <Header name={appName}/>
            <div className="product-content-outer">
                {children}
            </div>
        </div>
    );
};

export default MainOuterLayout;
