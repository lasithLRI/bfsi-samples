import {useState} from "react";
import {
    Accordion, AccordionDetails, AccordionSummary, Card, Grid, Table, TableBody, TableCell, TableRow,
    Typography
} from "@oxygen-ui/react";
// @ts-ignore
import {ChevronDownIcon, TrashIcon} from "@oxygen-ui/react-icons";
import type {BanksWithAccounts} from "../../../hooks/use-config-context.ts";
import '../home.scss'
import {formatCurrency} from "../../../utility/number-formatter.ts";
import CustomTitle from "../../../components/custom-title/custom-title.tsx";
import './connected-banks-accounts.scss'
import {useMediaQuery, useTheme} from "@mui/material";
import {Checkbox, FormControlLabel} from "@mui/material";
import {queryClient} from "../../../utility/query-client.ts";
import type {Config} from "../../../hooks/config-interfaces.ts";

const ACCOUNTS_SESSION_KEY = "openbanking_added_accounts";
const BACKEND_BASE = "https://obiam:9446/ob-demo-backend-1.0.0/init";

interface ConsentGroup {
    consentId: string;
    bankName: string;
    accounts: { id: string; name: string }[];
}

interface ConnectedBanksAccountsProps {
    bankAndAccountsInfo: BanksWithAccounts[];
    onBankRemoved: () => void;
}

const ConnectedBanksAccounts = ({bankAndAccountsInfo, onBankRemoved}: ConnectedBanksAccountsProps) => {

    const isLargeScreen = useMediaQuery(useTheme().breakpoints.down('md'));
    const responsiveDirections = isLargeScreen ? 'column' : 'row';

    const [showDeleteOverlay, setShowDeleteOverlay] = useState(false);
    const [consentGroups, setConsentGroups] = useState<ConsentGroup[]>([]);
    const [selectedConsentIds, setSelectedConsentIds] = useState<string[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isRevoking, setIsRevoking] = useState(false);

    const thirdBank = bankAndAccountsInfo[2];

    const handleDeleteClick = async (e: React.MouseEvent) => {
        e.stopPropagation();
        setSelectedConsentIds([]);
        setIsLoading(true);
        setShowDeleteOverlay(true);

        try {
            const savedRaw = sessionStorage.getItem(ACCOUNTS_SESSION_KEY);
            const savedAccounts: any[] = savedRaw ? JSON.parse(savedRaw) : [];

            if (savedAccounts.length === 0) {
                setConsentGroups([]);
                return;
            }

            // Group accounts by consentId
            const groupMap = new Map<string, { id: string; name: string }[]>();
            for (const acc of savedAccounts) {
                if (!acc.consentId) continue;
                if (!groupMap.has(acc.consentId)) {
                    groupMap.set(acc.consentId, []);
                }
                groupMap.get(acc.consentId)!.push({ id: acc.id, name: acc.name });
            }

            const groups: ConsentGroup[] = Array.from(groupMap.entries()).map(([consentId, accounts]) => ({
                consentId,
                bankName: thirdBank.bank.name,
                accounts
            }));

            setConsentGroups(groups);
        } catch (err) {
            console.error("Failed to load consent groups:", err);
            setConsentGroups([]);
        } finally {
            setIsLoading(false);
        }
    };

    const toggleConsentGroup = (consentId: string) => {
        setSelectedConsentIds(prev =>
            prev.includes(consentId)
                ? prev.filter(id => id !== consentId)
                : [...prev, consentId]
        );
    };

    const handleRevokeConfirm = async () => {
        if (selectedConsentIds.length === 0) return;
        setIsRevoking(true);

        const bankName = thirdBank.bank.name;

        try {
            const accountIdsToRemove = consentGroups
                .filter(g => selectedConsentIds.includes(g.consentId))
                .flatMap(g => g.accounts.map(a => a.id));

            await Promise.all(
                consentGroups
                    .filter(g => selectedConsentIds.includes(g.consentId))
                    .map(g =>
                        fetch(
                            `${BACKEND_BASE}/revoke-consent?accountId=${encodeURIComponent(g.accounts[0].id)}&bankName=${encodeURIComponent(bankName)}`,
                            {method: "DELETE"}
                        )
                    )
            );

            const oldConfig = queryClient.getQueryData<Config>(["appConfig"]);
            if (!oldConfig) return;

            const savedRaw = sessionStorage.getItem(ACCOUNTS_SESSION_KEY);
            const savedAccounts: any[] = savedRaw ? JSON.parse(savedRaw) : [];

            const remainingAccounts = savedAccounts.filter(
                acc => !accountIdsToRemove.includes(acc.id)
            );

            if (remainingAccounts.length === 0) {
                sessionStorage.removeItem(ACCOUNTS_SESSION_KEY);
                sessionStorage.removeItem("openbanking_consent_id");
                queryClient.setQueryData(["appConfig"], {
                    ...oldConfig,
                    banks: oldConfig.banks.map((bank, index) =>
                        index === 2 ? {...bank, accounts: []} : bank
                    )
                });
                onBankRemoved();
            } else {
                sessionStorage.setItem(ACCOUNTS_SESSION_KEY, JSON.stringify(remainingAccounts));
                queryClient.setQueryData(["appConfig"], {
                    ...oldConfig,
                    banks: oldConfig.banks.map((bank, index) =>
                        index === 2 ? {...bank, accounts: remainingAccounts} : bank
                    )
                });
            }

        } catch (err) {
            console.error("Revoke consent failed:", err);
        } finally {
            setIsRevoking(false);
            setShowDeleteOverlay(false);
            setSelectedConsentIds([]);
            setConsentGroups([]);
        }
    };

    const handleRevokeCancel = () => {
        setShowDeleteOverlay(false);
        setSelectedConsentIds([]);
        setConsentGroups([]);
    };

    return (
        <>
            <div className="main-connected-banks-outer" style={{display: "flex", flexDirection: "column"}}>
                <Grid className="card-outer" sx={{flexDirection: responsiveDirections}}>
                    {bankAndAccountsInfo.map((bank, index) => (
                        <Card key={index} className={'card-inner-bank-info'}>
                            <div className="card-top-container" style={{position: "relative"}}>
                                {index === 2 && (
                                    <div
                                        onClick={handleDeleteClick}
                                        style={{
                                            position: "absolute",
                                            top: 0,
                                            right: 0,
                                            cursor: "pointer",
                                            padding: "4px",
                                            color: "var(--oxygen-palette-error-main, #d32f2f)"
                                        }}
                                        title="Remove accounts"
                                    >
                                        <TrashIcon/>
                                    </div>
                                )}
                                <div className="logo">
                                    <img src={bank.bank.image} alt=""/>
                                </div>
                                <div className="bank-name-container">
                                    <p>{bank.bank.name}</p>
                                </div>
                            </div>
                            <div className="card-total-container">
                                <p>{bank.bank.currency}</p>
                                <p>{formatCurrency(bank.total)}</p>
                            </div>
                        </Card>
                    ))}
                </Grid>

                <CustomTitle title={"Connected Accounts"}/>

                <div>
                    {bankAndAccountsInfo.map((bank, index) => (
                        <Accordion key={index}>
                            <AccordionSummary expandIcon={<ChevronDownIcon/>}
                                              aria-controls={`${index}`}
                                              id={`${index}-header`}>
                                <div className="accordian-header-container">
                                    <div className="bank-container">
                                        <div className="bank-logo-container">
                                            <img src={bank.bank.image} alt="bank logo" className={'bank-logo'}/>
                                        </div>
                                        <Typography>{bank.bank.name} - Connected Accounts</Typography>
                                    </div>
                                </div>
                            </AccordionSummary>
                            <AccordionDetails>
                                <Table>
                                    <TableBody>
                                        {bank.accounts.map((account, idx) => {
                                            const border = idx === bank.accounts.length - 1;
                                            return (
                                                <TableRow key={idx} hideBorder={border} className={"table-row"}>
                                                    <TableCell className={"table-body"}>{account.name}</TableCell>
                                                    <TableCell className={"table-body"}>{account.id}</TableCell>
                                                    <TableCell className={"table-body"}>
                                                        {bank.bank.currency}
                                                    </TableCell>
                                                    <TableCell className={"table-body"}>
                                                        {formatCurrency(account.balance)}
                                                    </TableCell>
                                                </TableRow>
                                            );
                                        })}
                                    </TableBody>
                                </Table>
                            </AccordionDetails>
                        </Accordion>
                    ))}
                </div>
            </div>

            {/* Delete accounts overlay */}
            {showDeleteOverlay && thirdBank && (
                <div style={{
                    position: "fixed",
                    top: 0, left: 0, right: 0, bottom: 0,
                    backgroundColor: "rgba(0,0,0,0.5)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    zIndex: 1300
                }}>
                    <div style={{
                        backgroundColor: "var(--oxygen-palette-background-paper, #fff)",
                        borderRadius: "8px",
                        padding: "2rem",
                        minWidth: "340px",
                        maxWidth: "480px",
                        width: "100%"
                    }}>
                        <Typography variant="h6" style={{marginBottom: "1rem"}}>
                            Remove Accounts
                        </Typography>

                        {isLoading ? (
                            <Typography variant="body2" style={{padding: "1rem 0"}}>
                                Loading consent groups...
                            </Typography>
                        ) : consentGroups.length === 0 ? (
                            <Typography variant="body2" style={{padding: "1rem 0"}}>
                                No consent groups found for {thirdBank.bank.name}.
                            </Typography>
                        ) : (
                            <>
                                <Typography variant="body2"
                                            style={{
                                                marginBottom: "1rem",
                                                color: "var(--oxygen-palette-text-secondary)"
                                            }}>
                                    Select consent groups to revoke from {thirdBank.bank.name}.
                                    All accounts under a selected group will be removed.
                                </Typography>

                                <div style={{marginBottom: "1.5rem"}}>
                                    {consentGroups.map((group) => (
                                        <div key={group.consentId} style={{
                                            border: "1px solid var(--oxygen-palette-divider, #e0e0e0)",
                                            borderRadius: "6px",
                                            padding: "0.75rem",
                                            marginBottom: "0.75rem"
                                        }}>
                                            <FormControlLabel
                                                control={
                                                    <Checkbox
                                                        checked={selectedConsentIds.includes(group.consentId)}
                                                        onChange={() => toggleConsentGroup(group.consentId)}
                                                    />
                                                }
                                                label={
                                                    <Typography variant="body2" style={{fontWeight: 600}}>
                                                        Consent: {group.consentId}
                                                    </Typography>
                                                }
                                            />
                                            {/* Accounts under this consent */}
                                            <div style={{paddingLeft: "2rem"}}>
                                                {group.accounts.map(acc => (
                                                    <div key={acc.id} style={{
                                                        display: "flex",
                                                        justifyContent: "space-between",
                                                        padding: "0.2rem 0",
                                                        color: "var(--oxygen-palette-text-secondary)"
                                                    }}>
                                                        <Typography variant="body2">{acc.name}</Typography>
                                                        <Typography variant="body2"
                                                                    style={{fontSize: "0.8rem"}}>
                                                            {acc.id}
                                                        </Typography>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </>
                        )}

                        <div style={{display: "flex", gap: "1rem", justifyContent: "flex-end"}}>
                            <button
                                onClick={handleRevokeCancel}
                                disabled={isRevoking}
                                style={{
                                    padding: "0.5rem 1.5rem",
                                    border: "1px solid var(--oxygen-palette-divider)",
                                    borderRadius: "4px",
                                    cursor: "pointer",
                                    backgroundColor: "transparent"
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleRevokeConfirm}
                                disabled={selectedConsentIds.length === 0 || isRevoking || isLoading}
                                style={{
                                    padding: "0.5rem 1.5rem",
                                    border: "none",
                                    borderRadius: "4px",
                                    cursor: selectedConsentIds.length === 0 ? "not-allowed" : "pointer",
                                    backgroundColor: "var(--oxygen-palette-error-main, #d32f2f)",
                                    color: "#fff",
                                    opacity: selectedConsentIds.length === 0 ? 0.6 : 1
                                }}
                            >
                                {isRevoking ? "Revoking..." : "Revoke Selected"}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
};

export default ConnectedBanksAccounts;
