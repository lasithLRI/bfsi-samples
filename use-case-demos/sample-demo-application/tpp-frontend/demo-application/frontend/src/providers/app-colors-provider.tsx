import React from 'react';
import { ThemeProvider, extendTheme } from '@oxygen-ui/react';
import type { AppConfig } from "../utility/custom-interfaces.ts";

interface AppColorsProviderProps {
    children?: React.ReactNode;
    colors?: AppConfig['colors'];
}

const AppColorsProvider = ({ children, colors }: AppColorsProviderProps) => {
    const customColors = (colors || []).reduce((acc, currentObject) => {
        return { ...acc, ...currentObject };
    }, {} as Record<string, string>);

    const theme = extendTheme({
        typography: {
            fontFamily: 'Inter',
        },
        colorSchemes: {
            light: {
                palette: {
                    primary: {
                        main: customColors.primary,
                        secondaryColor: customColors.secondaryColor,
                        button: customColors.button,
                        backgroundColor: customColors.backgroundColor,
                        tableBackground: customColors.tableBackground,
                        innerButtonBackground: customColors.innerButtonBackground,
                        bankColor1: customColors.bankColor1,
                        bankColor2: customColors.bankColor2,
                        bankColor3: customColors.bankColor3,
                        bankBackground: customColors.bankBackground,
                        formValidationError: customColors.formValidationError,
                        tableHeaderBackground: customColors.tableHeaderBackground,
                        tableHeaderFontColor: customColors.tableHeaderFontColor,
                        tableBodyColor: customColors.tableBackgroundColor,
                        greenArrowColor: customColors.greenArrowColor,
                        redArrowColor: customColors.redArrowColor,
                        requiredStar: customColors.requiredStar,
                    },
                    fontColor: {
                        white: customColors.fontWhite,
                    },
                },
            },
            dark: {
                palette: {
                    primary: {
                        main: '#FF5456',
                    },
                },
            },
        },
    });

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return <ThemeProvider theme={theme as any}>{children}</ThemeProvider>;
};

export default AppColorsProvider;
