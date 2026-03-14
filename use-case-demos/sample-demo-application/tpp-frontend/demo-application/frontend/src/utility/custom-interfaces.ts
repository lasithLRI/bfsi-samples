
export interface AppConfig {
    user: {
        name: string;
        image: string;
        background: string;
    };
    name: {
        route: string;
        applicationName: string;
    };
    transactionTableHeaderData: Record<string, string>[];
    standingOrdersTableHeaderData: Record<string, string>[];
    colors: Record<string, string>[];
}

export interface UserInfo{
    name: string;
    image: string;
    background: string;
}
