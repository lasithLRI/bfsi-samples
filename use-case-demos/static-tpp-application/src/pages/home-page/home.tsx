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

import React from "react";
import { Grid } from "@mui/material";
import HomePageLayout from "../../layouts/home-page-layout/home-page-layout.tsx";
import type {
    AppInfo,
    Bank,
    StandingOrders,
    TableConfigs,
    TransactionData,
    User
} from "../../hooks/config-interfaces.ts";
import type { BanksWithAccounts, ChartData, OverlayDataProp } from "../../hooks/use-config-context.ts";
import { InfographicsContent } from "./infographics-content/infographics-content.tsx";
import ConnectedBanksAccounts from "./connected-banks-accounts/connected-banks-accounts.tsx";
import CustomTitle from "../../components/custom-title/custom-title.tsx";
import { useNavigate } from "react-router-dom";
import OverlayConfirmation from "../../components/overlay-confirmation/overlay-confirmation.tsx";
import TableComponent from "../../components/table-component.tsx";
import Joyride, { ACTIONS, EVENTS, STATUS } from "react-joyride";
import { DEMO_STEPS } from "../../utility/onboarding.ts";
import type { CallBackProps } from 'react-joyride';
import ApplicationLayout from "../../layouts/application-layout/application-layout.tsx";

interface AccountsCentralLayoutProps {
    children?: React.ReactNode;
    name: string;
    userInfo: User;
    total: number;
    chartData: ChartData;
    banksWithAccounts: BanksWithAccounts[];
    transactions: TransactionData[];
    standingOrderList: StandingOrders[];
    appInfo: AppInfo;
    banksList: Bank[];
    overlayInformation: OverlayDataProp;
    transactionTableHeaderData?: TableConfigs[];
    standingOrdersTableHeaderData?: TableConfigs[];
    runTour: boolean;
    tourKey: number;
    setRunTour: (value: boolean) => void;
    onStartTour: () => void;
}

const Home = ({
                  standingOrdersTableHeaderData, name, userInfo, total, chartData,
                  banksWithAccounts, transactions, standingOrderList, appInfo, banksList,
                  overlayInformation, transactionTableHeaderData, runTour, tourKey,
                  setRunTour, onStartTour
              }: AccountsCentralLayoutProps) => {

    const navigate = useNavigate();

    const addAccount = () => {
        navigate(`/${appInfo.route}/accounts`, {
            state: {
                name: appInfo.applicationName,
                banksWithAccounts: banksList,
            }
        });
    };

    const viewMore = (title?: string) => {
        const route = title === "Latest Transactions" ? "transactions" : "standing-orders";
        navigate(`/${appInfo.route}/${route}`);
    };

    const onButtonHandler = (buttonName: string, title?: string) => {
        if (buttonName === "Add Account") {
            addAccount();
        } else if (buttonName === "View More") {
            viewMore(title);
        }
    };

    /**
     * Single exit handler for every way the tour can end.
     * Marks seenTour in sessionStorage and scrolls back to top.
     */
    const endTour = () => {
        setRunTour(false);
        sessionStorage.setItem('seenTour', 'true');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    /**
     * Covers every Joyride exit path so seenTour is always written:
     *  - ACTIONS.CLOSE   → user clicked X on any step
     *  - ACTIONS.SKIP    → user clicked Skip
     *  - ACTIONS.RESET   → tour programmatically reset
     *  - STATUS.FINISHED → user clicked Finish on last step
     *  - STATUS.SKIPPED  → Joyride's internal skipped status
     *  - EVENTS.TOUR_END → safety net — fires after any tour end
     */
    const handleCallback = (data: CallBackProps) => {
        const { status, action, type } = data;

        const isDone =
            action === ACTIONS.CLOSE   ||
            action === ACTIONS.SKIP    ||
            action === ACTIONS.RESET   ||
            status === STATUS.FINISHED ||
            status === STATUS.SKIPPED  ||
            type   === EVENTS.TOUR_END;

        if (isDone) {
            endTour();
        }
    };

    return (
        <>
            <ApplicationLayout name={name} onStartTour={onStartTour} isTourRunning={runTour}>
                <HomePageLayout userInfo={userInfo} appInfo={appInfo}>
                    <Grid className={'info-graphic'}>
                        <InfographicsContent total={total} chartInfo={chartData} />
                    </Grid>
                    <Grid className={'accounts-container'}>
                        <CustomTitle title={"Connected Banks"} buttonName={"Add Account"} buttonType={"contained"}
                                     onPress={onButtonHandler} />
                        <ConnectedBanksAccounts bankAndAccountsInfo={banksWithAccounts} />
                    </Grid>
                    <Grid className={'transactions-container'}>
                        <CustomTitle title={"Latest Transactions"} buttonName={"View More"} buttonType={"outlined"}
                                     onPress={onButtonHandler} />
                        <TableComponent tableData={transactions} tableType={"transaction"}
                                        dataConfigs={transactionTableHeaderData} />
                    </Grid>
                    <Grid className={'standing-orders-container'}>
                        <CustomTitle title={"Standing Orders"} buttonName={"View More"} buttonType={"outlined"}
                                     onPress={onButtonHandler} />
                        <TableComponent tableData={standingOrderList} dataConfigs={standingOrdersTableHeaderData}
                                        tableType={""} />
                    </Grid>
                </HomePageLayout>
            </ApplicationLayout>

            {overlayInformation.flag &&
                <OverlayConfirmation
                    onConfirm={overlayInformation.overlayData.onMainButtonClick}
                    onCancel={() => {}}
                    mainButtonText={overlayInformation.overlayData.mainButtonText}
                    secondaryButtonText={overlayInformation.overlayData.secondaryButtonText}
                    content={overlayInformation.overlayData.context}
                    title={overlayInformation.overlayData.title}
                />
            }

            {/*
              * key={tourKey} — when tourKey increments, React unmounts and
              * remounts Joyride entirely, resetting its internal stepIndex to 0.
              * This guarantees "Start Tour" always begins from the first step.
              */}
            <Joyride
                key={tourKey}
                steps={DEMO_STEPS}
                run={runTour}
                continuous
                callback={handleCallback}
                disableCloseOnEsc={false}
                disableOverlayClose={true}
                showSkipButton={true}
                styles={{
                    options: {
                        zIndex: 99999,
                        primaryColor: 'var(--oxygen-palette-primary-main)'
                    },
                    overlay: {
                        backgroundColor: 'rgba(0, 0, 0, 0.5)'
                    },
                    buttonSkip: {
                        display: 'none'
                    },
                    tooltip: {
                        padding: '8px',
                    },
                    tooltipContent: {
                        padding: '8px 0'
                    },
                    tooltipTitle: {
                        margin: '0 0 8px 0'
                    },
                    tooltipFooter: {
                        marginTop: '8px'
                    }
                }}
                locale={{
                    next: 'Next',
                    last: 'Finish',
                }}
            />
        </>
    );
};

export default Home;
