/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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

export interface AccountsWithPermissions {
    [permissions: string]: Account[];
}

export interface ChartData{
    label: string;
    labels: string[];
    data: number[];
    backgroundColor: string[];
    borderColor: string[];
    borderWidth: number;
    cutout: string;
}

export interface BanksWithAccounts{
    bank: Bank;
    accounts: Account[];
    total: number;
}

export interface OverlayDataProp{
    flag: boolean;
    overlayData:OverlayData;
}

export interface OverlayData{
    title: string;
    context: string;
    mainButtonText: string;
    secondaryButtonText: string;
    onMainButtonClick: () => void;
}

import {useMemo, useState} from "react";
import type {Account, AppInfo, Bank, User} from "./config-interfaces.ts";
import {useConfig} from "./use-config.ts";
import {processAllBankDates} from "../utility/date-utils.ts";

/**
 * @function useConfigContext
 * @description The central state management hook that fetches application configuration,
 * aggregates raw data into calculated properties (e.g., totals, chart data),
 * and critically, handles global state updates following bank redirection flows (payments, account linking).
 *
 * NOW INCLUDES:
 * - Automatic processing of standing orders to convert relative day numbers to actual dates.
 * - Proper transaction ID generation with 8-digit format (T00123460)
 * - Current date assignment for new transactions
 */
const useConfigContext = () => {
    const { data: configData } = useConfig() ;
    const [overlayInformation] = useState<OverlayDataProp>({flag:false,overlayData:{context:"",secondaryButtonText:"",mainButtonText:"",title:"",onMainButtonClick:()=>{}}});

    const processedBanks = useMemo(() => {
        if (!configData?.banks) return [];
        return processAllBankDates(configData.banks);
    }, [configData?.banks]);

    const totals = useMemo(() => {
        if (!configData) return [];
        return processedBanks.map((bank) => {
            const total = bank.accounts
                .reduce((sum: any, acc: { balance: any; }) => sum + acc.balance, 0);
            return { bank, total };
        });
    }, [processedBanks, configData]);

    const chartInfo = useMemo(() => {
        if (!configData) return { label: '', labels: [], data: [], backgroundColor: [], borderColor: [], borderWidth: 0, cutout: '0%' };
        return {
            label: '',
            labels: totals.map((t) => t.bank.name),
            data: totals.map((t) => t.total),
            backgroundColor: totals.map((t) => t.bank.color),
            borderColor: totals.map((t) => t.bank.border),
            borderWidth: 2,
            cutout: '35%'
        }
    }, [configData, totals]);

    const totalBalances = useMemo(() => {
        return totals.reduce((s, b) => s + b.total, 0);
    }, [totals]);

    const banksWithAllAccounts = useMemo(() => {
        if (!configData) return [];
        return processedBanks.map((bank) => {
            const uniqueAccountsMap = new Map();
            bank.accounts.forEach((acc: { id: any; }) => {
                if (!uniqueAccountsMap.has(acc.id)) {
                    uniqueAccountsMap.set(acc.id, acc);
                }
            });
            const uniqueAccounts = Array.from(uniqueAccountsMap.values());
            const total = uniqueAccounts.reduce((sum, acc) => sum + (acc.balance ?? 0), 0);
            return {
                bank,
                accounts: uniqueAccounts,
                total
            };
        });
    }, [processedBanks, configData]);

    const standingOrdersWithDates = useMemo(() => {
        return processedBanks.flatMap(bank => bank.standingOrders || []);
    }, [processedBanks]);

    return {
        appInfo: configData?.name as AppInfo ,
        userInfo: configData?.user as User,
        bankTotals: totals,
        chartInfo: chartInfo,
        total: totalBalances,
        banksWithAccounts: banksWithAllAccounts,
        transactions: processedBanks.flatMap(bank =>
            bank.accounts.flatMap((account: { transactions: any; }) =>  account.transactions || [])
        ),
        standingOrderList: standingOrdersWithDates,
        payeesData: configData?.payees ?? [],
        useCases: configData?.types ?? [],
        banksList: processedBanks,
        overlayInformation: overlayInformation,
        transactionTableHeaderData:configData?.transactionTableHeaderData,
        standingOrdersTableHeaderData:configData?.standingOrdersTableHeaderData,
        colors: configData?.colors,
        accountsNumbersToAdd: configData?.accountNumbersToAdd,
        banksInfomation: processedBanks
    };
};

export default useConfigContext;
