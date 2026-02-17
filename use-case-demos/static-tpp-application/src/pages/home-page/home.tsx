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
import Joyride from "react-joyride";
import { DEMO_STEPS } from "../../utility/onboarding.ts";
import type { CallBackProps } from 'react-joyride';
import ApplicationLayout from "../../layouts/application-layout/application-layout.tsx";

interface AccountsCentralLayoutProps {
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
    setRunTour: (value: boolean) => void;
    onStartTour: () => void;
}

const Home = ({
                  standingOrdersTableHeaderData, name, userInfo, total, chartData,
                  banksWithAccounts, transactions, standingOrderList, appInfo, banksList,
                  overlayInformation, transactionTableHeaderData, runTour, setRunTour, onStartTour
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
     * Called whenever the tour ends for any reason — finished, skipped, or
     * the X button closed. Marks the tour as seen in sessionStorage and
     * smoothly scrolls back to the top of the page.
     */
    const endTour = () => {
        setRunTour(false);
        sessionStorage.setItem('seenTour', 'true');
        window.scrollTo({ top: 0, behavior: 'smooth' });
        if (window.parent !== window) {
            window.parent.postMessage({ type: 'scroll-to-top' }, '*');
        }
    };

    const handleCallback = (data: CallBackProps) => {
        const { status, action } = data;

        // X / close button clicked
        if (action === 'close') {
            endTour();
            return;
        }

        // Tour stepped through to the end, or skip button pressed
        if (status === 'finished' || status === 'skipped') {
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

            <Joyride
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
