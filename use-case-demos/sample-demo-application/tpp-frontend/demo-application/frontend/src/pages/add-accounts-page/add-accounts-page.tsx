import { useEffect, useState } from "react";
import ApplicationLayout from "../../layouts/application-layout/application-layout.tsx";
import { useLocation } from "react-router-dom";
import PaymentAccountPageLayout from "../../layouts/payment-account-page-layout/payment-account-page-layout.tsx";
import type { Bank } from "../../hooks/config-interfaces.ts";
import { Box, Button, Card } from "@oxygen-ui/react";
import "./add-account.scss";
import { RedirectionComponent } from "../../components/redirection-component.tsx";
import { api } from "../../utility/api.ts";
import { resolveImageUrl } from "../../utility/image-utils.ts";


interface NavigationState {
    name: string;
}

const AddAccountsPage = () => {
    const location = useLocation();
    const navigationState = location.state as NavigationState;
    const appName = navigationState?.name;

    const [bankInformations, setBankInformations] = useState<Bank[]>([]);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [isRedirecting, setIsRedirecting] = useState<boolean>(false);

    useEffect(() => {
        api.get<Bank[]>("accounts")
            .then((data) => {
                const patched = data.map((bank) => ({
                    ...bank,
                    image: resolveImageUrl(bank.image)
                }));
                setBankInformations(patched);
            })
            .catch((err) => {
                console.error("Failed to fetch bank information:", err);
            })
            .finally(() => {
                setIsLoading(false);
            });
    }, []);

    const onAddAccountsHandler = async (bankName: string) => {
        setIsRedirecting(true);
        try {
            const data = await api.post<{ redirect: string }>("addaccounts", { bankName });
            window.location.href = data.redirect;
        } catch (err) {
            console.error("Failed to add account:", err);
            setIsRedirecting(false);
        }
    };

    if (isRedirecting) {
        return <RedirectionComponent />;
    }

    return (
        <>
            <ApplicationLayout name={appName}>
                <PaymentAccountPageLayout title={"Add Account"}>
                    <Box className="accounts-outer">
                        <h3 style={{ marginBottom: "1.5rem" }}>Select your Bank</h3>
                        <div className="accounts-buttons-container">
                            {isLoading ? (
                                <p>Loading banks...</p>
                            ) : (
                                bankInformations?.map((account, index) => (
                                    <Button key={index} onClick={() => { onAddAccountsHandler(account.name); }}>
                                        <Card>
                                            <Box className={"account-button-outer"}>
                                                <Box className={"logo-container"} sx={{ marginLeft: "2rem" }}>
                                                    <img src={account.image} alt={`${account.name} logo`} />
                                                </Box>
                                                <p>{account.name}</p>
                                            </Box>
                                        </Card>
                                    </Button>
                                ))
                            )}
                        </div>
                    </Box>
                </PaymentAccountPageLayout>
            </ApplicationLayout>
        </>
    );
};

export default AddAccountsPage;
