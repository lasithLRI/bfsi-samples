/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { useEffect, useMemo, useState } from "react";
import type { Account, AppInfo, Bank, User } from "./config-interfaces.ts";
import { useLocation, useNavigate } from "react-router-dom";
import { useConfig } from "./use-config.ts";
import { queryClient } from "../utility/query-client.ts";
import { processAllBankDates } from "../utility/date-utils.ts";

export interface AccountsWithPermissions {
    [permissions: string]: Account[];
}

export interface ChartData {
    label: string;
    labels: string[];
    data: number[];
    backgroundColor: string[];
    borderColor: string[];
    borderWidth: number;
    cutout: string;
}

export interface BanksWithAccounts {
    bank: Bank;
    accounts: Account[];
    total: number;
}

export interface OverlayDataProp {
    flag: boolean;
    overlayData: OverlayData;
}

export interface OverlayData {
    title: string;
    context: string;
    mainButtonText: string;
    secondaryButtonText: string;
    onMainButtonClick: () => void;
}

const ACCOUNTS_SESSION_KEY = "openbanking_added_accounts";

/**
 * On startup, if sessionStorage has previously added accounts,
 * re-inject them into the React Query cache (bank index 2).
 * Always overwrite — config.json bank[2] has placeholder accounts
 * that must be replaced with the real connected accounts.
 */
const restoreAccountsFromSession = () => {
    const raw = sessionStorage.getItem(ACCOUNTS_SESSION_KEY);
    if (!raw) return;

    try {
        const savedAccounts = JSON.parse(raw);
        const oldConfig = queryClient.getQueryData<any>(["appConfig"]);
        if (!oldConfig) return;

        const targetBank = oldConfig.banks[2];
        if (!targetBank) return;

        queryClient.setQueryData(["appConfig"], {
            ...oldConfig,
            banks: oldConfig.banks.map((bank: any, index: number) =>
                index === 2 ? { ...targetBank, accounts: savedAccounts } : bank
            )
        });
    } catch (e) {
        console.error("Failed to restore accounts from sessionStorage", e);
    }
};

const useConfigContext = () => {
    const { data: configData, isLoading } = useConfig();
    const location = useLocation();
    const navigate = useNavigate();
    const redirectState = location.state?.operationState;

    const [overlayInformation, setOverlayInformation] = useState<OverlayDataProp>({
        flag: false,
        overlayData: {
            context: "",
            secondaryButtonText: "",
            mainButtonText: "",
            title: "",
            onMainButtonClick: () => {}
        }
    });

    const disconnectBank = () => {
        sessionStorage.removeItem(ACCOUNTS_SESSION_KEY);
        sessionStorage.removeItem("openbanking_consent_id");
        setConnectedBankCount(2);
    };

    const handleOverlayClose = () => {
        setOverlayInformation({
            flag: false,
            overlayData: {
                context: "",
                secondaryButtonText: "",
                mainButtonText: "",
                title: "",
                onMainButtonClick: () => {}
            }
        });
    };

    const [connectedBankCount, setConnectedBankCount] = useState(2);

    // Restore accounts from sessionStorage whenever config data loads
    useEffect(() => {
        if (!configData) return;
        restoreAccountsFromSession();

        const raw = sessionStorage.getItem(ACCOUNTS_SESSION_KEY);
        if (raw) {
            setConnectedBankCount(3);
        }
    }, [configData]);

    const processedBanks = useMemo(() => {
        if (!configData?.banks) return [];
        const count = Math.min(connectedBankCount, configData.banks.length);
        return processAllBankDates(configData.banks.slice(0, count));
    }, [configData?.banks, connectedBankCount]);

    const allBankInformations = useMemo(() => {
        if (!configData?.banks) return [];
        return processAllBankDates(configData.banks);
    }, [configData?.banks]);

    const allTransactions = useMemo(() => {
        return processedBanks.flatMap(bank =>
            bank.accounts.flatMap((account: { transactions: any }) => account.transactions || [])
        );
    }, [processedBanks]);

    const [accountsToTransactions, setAccountsToTransactions] = useState<any[]>([]);

    useEffect(() => {
        setAccountsToTransactions(allTransactions);
    }, [allTransactions]);

    const totals = useMemo(() => {
        if (!configData) return [];
        return processedBanks.map((bank) => {
            const total = bank.accounts.reduce((sum: any, acc: { balance: any }) => sum + acc.balance, 0);
            return { bank, total };
        });
    }, [processedBanks, configData]);

    const chartInfo = useMemo(() => {
        if (!configData) return {
            label: '',
            labels: [],
            data: [],
            backgroundColor: [],
            borderColor: [],
            borderWidth: 0,
            cutout: '0%'
        };
        return {
            label: '',
            labels: totals.map((t) => t.bank.name),
            data: totals.map((t) => t.total),
            backgroundColor: totals.map((t) => t.bank.color),
            borderColor: totals.map((t) => t.bank.border),
            borderWidth: 2,
            cutout: '35%'
        };
    }, [configData, totals]);

    const totalBalances = useMemo(() => {
        return totals.reduce((s, b) => s + b.total, 0);
    }, [totals]);

    const banksWithAllAccounts = useMemo(() => {
        if (!configData) return [];
        return processedBanks.map((bank) => {
            const uniqueAccountsMap = new Map();
            bank.accounts.forEach((acc: { id: any }) => {
                if (!uniqueAccountsMap.has(acc.id)) {
                    uniqueAccountsMap.set(acc.id, acc);
                }
            });
            const uniqueAccounts = Array.from(uniqueAccountsMap.values());
            const total = uniqueAccounts.reduce((sum, acc) => sum + (acc.balance ?? 0), 0);
            return { bank, accounts: uniqueAccounts, total };
        });
    }, [processedBanks, configData]);

    const standingOrdersWithDates = useMemo(() => {
        return processedBanks.flatMap(bank => bank.standingOrders || []);
    }, [processedBanks]);

    useEffect(() => {
        if (!configData || !redirectState) return;

        if (redirectState.type === "cancelled") {
            setOverlayInformation({
                flag: true,
                overlayData: {
                    context: "The operation has been cancelled.",
                    secondaryButtonText: "",
                    mainButtonText: "Ok",
                    title: "Operation Cancelled",
                    onMainButtonClick: handleOverlayClose
                }
            });
            navigate(location.pathname, { replace: true, state: {} });

        } else if (redirectState.type === "accounts") {
            setConnectedBankCount(3);
            setOverlayInformation({
                flag: true,
                overlayData: {
                    context: "A new bank has been connected successfully.",
                    secondaryButtonText: "",
                    title: "Bank Connected",
                    mainButtonText: "Ok",
                    onMainButtonClick: handleOverlayClose
                }
            });
            navigate(location.pathname, { replace: true, state: {} });

        } else if (redirectState.type === "payments") {
            const success = redirectState.data?.success;
            setOverlayInformation({
                flag: true,
                overlayData: {
                    context: success
                        ? "Your payment has been successfully processed."
                        : "Payment could not be completed.",
                    secondaryButtonText: "",
                    title: success ? "Payment Successful" : "Payment Failed",
                    mainButtonText: "Ok",
                    onMainButtonClick: handleOverlayClose
                }
            });
            navigate(location.pathname, { replace: true, state: {} });
        }

    }, [redirectState]);

    return {
        appInfo: configData?.name as AppInfo,
        userInfo: configData?.user as User,
        bankTotals: totals,
        chartInfo: chartInfo,
        total: totalBalances,
        banksWithAccounts: banksWithAllAccounts,
        transactions: processedBanks.flatMap(bank =>
            bank.accounts.flatMap((account: { transactions: any }) => account.transactions || [])
        ),
        standingOrderList: standingOrdersWithDates,
        payeesData: configData?.payees ?? [],
        useCases: configData?.types ?? [],
        banksList: processedBanks,
        allBanksInfomation: allBankInformations,
        overlayInformation: overlayInformation,
        transactionTableHeaderData: configData?.transactionTableHeaderData,
        standingOrdersTableHeaderData: configData?.standingOrdersTableHeaderData,
        colors: configData?.colors,
        accountsNumbersToAdd: configData?.accountNumbersToAdd,
        banksInfomation: processedBanks,
        isLoading: isLoading as any,
        disconnectBank
    };
};

export default useConfigContext;
