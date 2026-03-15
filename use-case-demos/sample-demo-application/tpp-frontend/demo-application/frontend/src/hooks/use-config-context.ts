/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 * ...
 */

import { useMemo, useState } from "react";
import type { Account, AppInfo, Bank, User } from "./config-interfaces.ts";
import { useConfig } from "./use-config.ts";
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

const useConfigContext = () => {
    const { data: configData, isLoading } = useConfig();

    const [overlayInformation] = useState<OverlayDataProp>({
        flag: false,
        overlayData: {
            context: "",
            secondaryButtonText: "",
            mainButtonText: "",
            title: "",
            onMainButtonClick: () => {}
        }
    });

    const processedBanks = useMemo(() => {
        if (!configData?.banks) return [];
        return processAllBankDates(configData.banks);
    }, [configData?.banks]);

    const totals = useMemo(() => {
        if (!configData) return [];
        return processedBanks.map((bank) => {
            const total = bank.accounts
                .reduce((sum: number, acc: { balance: number }) => sum + acc.balance, 0);
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
            bank.accounts.forEach((acc: { id: string }) => {
                if (!uniqueAccountsMap.has(acc.id)) {
                    uniqueAccountsMap.set(acc.id, acc);
                }
            });
            const uniqueAccounts = Array.from(uniqueAccountsMap.values());
            const total = uniqueAccounts.reduce((sum, acc) => sum + (acc.balance ?? 0), 0);
            return { bank, accounts: uniqueAccounts, total };
        });
    }, [processedBanks, configData]);

    return {
        isLoading,
        appInfo: (configData?.name ?? { route: '', applicationName: '' }) as AppInfo,
        userInfo: (configData?.user ?? { name: '', image: '', background: '' }) as User,
        bankTotals: totals,
        chartInfo: chartInfo,
        total: totalBalances,
        banksWithAccounts: banksWithAllAccounts,
        transactions: configData?.transactions ?? [],
        standingOrderList: configData?.standingOrders ?? [],
        payeesData: configData?.payees ?? [],
        useCases: configData?.types ?? [],
        banksList: processedBanks,
        overlayInformation: overlayInformation,
        transactionTableHeaderData: configData?.transactionTableHeaderData ?? [],
        standingOrdersTableHeaderData: configData?.standingOrdersTableHeaderData ?? [],
        colors: configData?.colors ?? [],
        accountsNumbersToAdd: configData?.accountNumbersToAdd ?? [],
        banksInformation: processedBanks
    };
};

export default useConfigContext;