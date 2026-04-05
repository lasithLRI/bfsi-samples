import ApplicationLayout from "../../layouts/application-layout/application-layout.tsx";
import {useLocation} from 'react-router-dom';
import PaymentAccountPageLayout from "../../layouts/payment-account-page-layout/payment-account-page-layout.tsx";
import type {Bank} from "../../hooks/config-interfaces.ts";
import {Box, Button, Card} from "@oxygen-ui/react";
import './add-account.scss'
import {useState} from "react";
import useConfigContext from "../../hooks/use-config-context.ts";
import {RedirectionComponent} from "../../components/redirection-component.tsx";

interface NavigationState {
    name: string;
    banksWithAccounts: Bank[];
}

interface AddAccountsPageProps {
    bankInformations: Bank[];
}

const AddAccountsPage = ({bankInformations}: AddAccountsPageProps) => {

    const {appInfo} = useConfigContext();
    const location = useLocation();
    const navigationState = location.state as NavigationState;
    const appName = navigationState?.name;
    const [isRedirecting, setIsRedirecting] = useState(false);
    const [apiError, setApiError] = useState<string | null>(null);

    const currentPathName = window.location.pathname.replace(/\/$/, "");
    const routeSegment = `/${appInfo?.route}`;
    const routeIndex = appInfo?.route ? currentPathName.indexOf(routeSegment) : -1;
    const rootBasePath = routeIndex >= 0 ? currentPathName.substring(0, routeIndex) : currentPathName;
    const normalizedBasePath = rootBasePath === "/" ? "" : rootBasePath;

    const onAddAccountsHandler = async (bankName: string) => {
        const target = bankInformations.find((bank) => bank.name === bankName);
        if (!target) {
            setApiError("Selected bank could not be found.");
            return;
        }

        setApiError(null);
        setIsRedirecting(true);

        try {
            const callbackUrl = `${window.location.origin}${normalizedBasePath}/callback`;
            const apiBaseUrl = `${window.location.origin}${normalizedBasePath}`;
            console.log(apiBaseUrl);

            const response = await fetch(`${apiBaseUrl}/init/add-accounts`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({bankName, callbackUrl})
            });

            if (!response.ok) {
                const errorBody = await response.text().catch(() => "");
                throw new Error(`Add account API returned ${response.status}: ${errorBody}`);
            }

            const responseData = await response.json().catch(() => null);
            if (responseData?.redirect) {
                window.location.href = responseData.redirect;
                return;
            }

        } catch (error) {
            console.error("Failed to call add account API:", error);
            setApiError("Unable to start the account linking flow. Please try again.");
            setIsRedirecting(false);
        }
    };

    if (isRedirecting) {
        return <RedirectionComponent/>;
    }

    return (
        <>
            <ApplicationLayout name={appName} onStartTour={undefined}>
                <PaymentAccountPageLayout title={"Add Account"}>
                    <Box className="accounts-outer">
                        <h3 style={{marginBottom: "1.5rem"}}>Select your Bank</h3>
                        <div className="accounts-buttons-container">
                            {apiError && (
                                <div className="api-error-message"
                                     style={{color: "#d32f2f", marginBottom: "1rem"}}>
                                    {apiError}
                                </div>
                            )}
                            {bankInformations?.map((account, index) => (
                                <Button
                                    key={index}
                                    onClick={() => onAddAccountsHandler(account.name)}
                                >
                                    <Card>
                                        <Box className={"account-button-outer"}>
                                            <Box className={"logo-container"} sx={{marginLeft: '2rem'}}>
                                                <img src={account.image} alt={`${account.name} logo`}/>
                                            </Box>
                                            <p>{account.name}</p>
                                        </Box>
                                    </Card>
                                </Button>
                            ))}
                        </div>
                    </Box>
                </PaymentAccountPageLayout>
            </ApplicationLayout>
        </>
    );
};

export default AddAccountsPage;
