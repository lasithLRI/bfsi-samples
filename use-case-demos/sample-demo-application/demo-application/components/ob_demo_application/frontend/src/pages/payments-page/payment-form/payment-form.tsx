import {Controller, useForm} from "react-hook-form";
import {Box, Button, FormControl, MenuItem, OutlinedInput, Select} from "@oxygen-ui/react";
import {NumericFormat} from "react-number-format";
import {useState} from "react";
import type {AppInfo, Bank, Payee} from "../../../hooks/config-interfaces.ts";
import OverlayConfirmation from "../../../components/overlay-confirmation/overlay-confirmation.tsx";
import '../payments-page.scss'
import useMediaQuery from "@mui/material/useMediaQuery";
import {useTheme} from "@mui/material";
import type {BanksWithAccounts} from "../../../hooks/use-config-context.ts";
import {RedirectionComponent} from "../../../components/redirection-component.tsx";

export interface PaymentFormData {
    userAccount: string;
    payeeAccount: string;
    currency: string;
    amount: number;
    reference: string;
    appInfo: AppInfo;
}

interface PaymentFormProps {
    banksWithAllAccounts: BanksWithAccounts[];
    payeeData: Payee[];
    banksList: Bank[];
}

export const ErrorMessage = ({error}: {error: any}) => {
    if (!error) return null;
    return <p className={"error-message-payments"}>{error.message}</p>;
};

const PaymentForm = ({banksWithAllAccounts, payeeData, banksList}: PaymentFormProps) => {

    const isSmallScreen = useMediaQuery(useTheme().breakpoints.down('md'));
    const responsiveDirection = isSmallScreen ? 'column' : 'row';

    const {control, handleSubmit, formState: {errors}, reset} = useForm<PaymentFormData>({
        defaultValues: {
            userAccount: '',
            payeeAccount: '',
            currency: 'GBP',
            amount: 0,
            reference: ''
        }
    });

    const [isConfirming, setIsConfirming] = useState(false);
    const [formDataToSubmit, setFormDataToSubmit] = useState<PaymentFormData | null>(null);
    const [isRedirecting, setIsRedirecting] = useState(false);

    // 1st and 2nd banks (index 0, 1) are already added — their accounts are shown but disabled
    const disabledBankNames = new Set(banksList.slice(0, 2).map((b) => b.name));

    const onSubmit = (data: PaymentFormData) => {
        setFormDataToSubmit(data);
        setIsConfirming(true);
    };

    const handleConfirmedAndRedirect = async () => {
        if (!formDataToSubmit) return;
        setIsConfirming(false);
        setIsRedirecting(true);

        try {
            // const backendBase = window.location.pathname.replace(/\/[^/]+$/, "");
            const response = await fetch(
                `https://obiam:9446/ob-demo-backend-1.0.0/init/payment`,
                {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({
                        userAccount: formDataToSubmit.userAccount,
                        payeeAccount: formDataToSubmit.payeeAccount,
                        currency: formDataToSubmit.currency,
                        amount: String(formDataToSubmit.amount),
                        reference: formDataToSubmit.reference
                    })
                }
            );

            if (!response.ok) {
                const body = await response.text().catch(() => "");
                throw new Error(`Payment API returned ${response.status}: ${body}`);
            }

            const data = await response.json();
            console.log("Payment response:", data);

            if (data.redirect) {
                sessionStorage.setItem("pendingPayment", JSON.stringify({
                    userAccount: formDataToSubmit.userAccount,
                    payeeAccount: formDataToSubmit.payeeAccount,
                    currency: formDataToSubmit.currency,
                    amount: String(formDataToSubmit.amount),
                    reference: formDataToSubmit.reference
                }));
                window.location.href = data.redirect;
            } else {
                throw new Error("No redirect URL returned");
            }
        } catch (err) {
            console.error("Payment failed:", err);
            setIsRedirecting(false);
        }
    };

    const handleCancelConfirmation = () => {
        setIsConfirming(false);
        setFormDataToSubmit(null);
    };

    const paymentConfirmationMsg = `Do you wish to proceed with the payment of ${formDataToSubmit?.currency} ${formDataToSubmit?.amount} to payee ${formDataToSubmit?.payeeAccount}?`;

    if (isRedirecting) {
        return <RedirectionComponent/>;
    }

    return (
        <>
            <h2 className={"payment-form-heading"}>Payment Information</h2>
            <form onSubmit={handleSubmit(onSubmit)}>
                <FormControl fullWidth={true} margin={'dense'}>
                    <label>Select Account <span style={{color: "var(--oxygen-palette-primary-requiredStar)"}}>*</span></label>
                    <Controller name={'userAccount'} control={control} rules={{required: true}} render={({field}) => (
                        <Select {...field}
                                displayEmpty
                                renderValue={(value) => {
                                    const selected = value as string;
                                    if (selected === "") {
                                        return <span style={{color: 'rgba(0, 0, 0, 0.38)'}}>Select your account</span>;
                                    }
                                    return selected;
                                }}
                                error={!!errors.userAccount}>
                            {banksWithAllAccounts.map((bankWithAccounts) =>
                                bankWithAccounts.accounts.map((account) => (
                                    <MenuItem key={`${bankWithAccounts.bank.name}-${account.id}`}
                                              value={`${bankWithAccounts.bank.name}-${account.id}`}
                                              disabled={disabledBankNames.has(bankWithAccounts.bank.name)}
                                              style={disabledBankNames.has(bankWithAccounts.bank.name) ? {opacity: 0.45} : {}}>
                                        {bankWithAccounts.bank.name}-{account.id}
                                    </MenuItem>
                                ))
                            )}
                        </Select>
                    )}/>
                    <ErrorMessage error={errors.userAccount}/>
                </FormControl>

                <FormControl fullWidth={true} margin={'dense'}>
                    <label>Biller <span style={{color: "var(--oxygen-palette-primary-requiredStar)"}}>*</span></label>
                    <Controller name={'payeeAccount'} control={control} rules={{required: true}} render={({field}) => (
                        <Select {...field}
                                displayEmpty
                                renderValue={(value) => {
                                    const selected = value as string;
                                    if (selected === "") {
                                        return <span style={{color: 'rgba(0, 0, 0, 0.38)'}}>Select biller account</span>;
                                    }
                                    return selected;
                                }}
                                error={!!errors.payeeAccount}>
                            {payeeData.map((payee, index) => (
                                <MenuItem key={index} value={`${payee.name}-${payee.accountNumber}`}>
                                    {payee.name}-{payee.accountNumber}
                                </MenuItem>
                            ))}
                        </Select>
                    )}/>
                    <ErrorMessage error={errors.payeeAccount}/>
                </FormControl>

                <div style={{display: 'flex', gap: '1rem'}}>
                    <FormControl fullWidth={true} margin={'dense'}>
                        <label>Currency</label>
                        <OutlinedInput value="GBP" disabled/>
                    </FormControl>
                    <FormControl fullWidth={true} margin={'dense'}>
                        <label>Amount <span style={{color: "var(--oxygen-palette-primary-requiredStar)"}}>*</span></label>
                        <Controller name={'amount'} control={control}
                                    rules={{required: true, min: 0.01}}
                                    render={({field}) => (
                                        <NumericFormat
                                            {...field}
                                            value={field.value === 0 ? '' : field.value}
                                            customInput={OutlinedInput}
                                            thousandSeparator={true}
                                            decimalScale={2}
                                            fixedDecimalScale={true}
                                            allowLeadingZeros={false}
                                            allowNegative={false}
                                            onValueChange={(values) => {
                                                field.onChange(values.floatValue || 0);
                                            }}
                                            error={!!errors.amount}
                                            placeholder="0.00"
                                            type="text"
                                        />
                                    )}/>
                        <ErrorMessage error={errors.amount}/>
                    </FormControl>
                </div>

                <FormControl fullWidth={true} margin={'dense'} sx={{height: '2vh'}}>
                    <label>Reference <span style={{color: "var(--oxygen-palette-primary-requiredStar)"}}>*</span></label>
                    <Controller name={'reference'} control={control} rules={{required: true}}
                                render={({field}) => (
                                    <OutlinedInput
                                        {...field}
                                        placeholder={"Enter your reference"}
                                        type={"text"}
                                        error={!!errors.reference}
                                    />
                                )}/>
                    <ErrorMessage error={errors.reference}/>
                </FormControl>

                <Box className={"payment-button-container"} flexDirection={responsiveDirection}>
                    <FormControl fullWidth={true} margin={'dense'}>
                        <Button variant={"contained"} type={"submit"}>Pay Now</Button>
                    </FormControl>
                    <FormControl fullWidth={true} margin={'dense'}>
                        <Button variant={"outlined"} type={"button"} onClick={() => reset()}>Reset</Button>
                    </FormControl>
                </Box>

                {isConfirming && (
                    <OverlayConfirmation
                        title={"Payment Confirmation"}
                        content={paymentConfirmationMsg}
                        onConfirm={handleConfirmedAndRedirect}
                        onCancel={handleCancelConfirmation}
                        mainButtonText={"Confirm"}
                        secondaryButtonText={"Cancel"}
                    />
                )}
            </form>
        </>
    );
};

export default PaymentForm;
