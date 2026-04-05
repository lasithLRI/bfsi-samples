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

import { useEffect, useState } from "react";
import {
    Box,
    Button,
    Chip,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Divider,
    Typography,
} from "@oxygen-ui/react";

interface SplashScreenProps {
    onClose: () => void;
}

/**
 * @component SplashScreen
 * @description A modal splash screen for Accounts Central shown once per session.
 * Must be rendered inside AppThemeProvider to receive the correct palette.
 */
const SplashScreen = ({ onClose }: SplashScreenProps) => {
    const [open, setOpen] = useState(false);

    useEffect(() => {
        const timer = setTimeout(() => setOpen(true), 100);
        return () => clearTimeout(timer);
    }, []);

    const handleClose = () => {
        setOpen(false);
        setTimeout(onClose, 300);
    };

    return (
        <Dialog
            open={open}
            onClose={handleClose}
            aria-labelledby="splash-dialog-title"
            aria-describedby="splash-dialog-description"
            maxWidth={false}
            PaperProps={{
                sx: {
                    width: 900,
                    minHeight: 370,
                    borderRadius: "16px",
                    overflow: "hidden",
                    display: "flex",
                    flexDirection: "column",
                    m: "auto",
                },
            }}
        >
            {/* Top accent bar */}
            <Box
                sx={{
                    height: "4px",
                    flexShrink: 0,
                    backgroundColor: "primary.main",
                }}
            />

            {/* ── Title ── */}
            <DialogTitle
                id="splash-dialog-title"
                sx={{ pt: 6, pb: 2, px: 4, textAlign: "center" }}
            >
                <Typography
                    variant="h5"
                    component="span"
                    sx={{
                        fontWeight: 700,
                        fontFamily: "inherit",
                        color: "primary.main",
                        lineHeight: 1.2,
                    }}
                >
                    Accounts Central
                </Typography>
            </DialogTitle>

            {/* ── Body ── */}
            <DialogContent
                id="splash-dialog-description"
                sx={{ pt: 1, pb: 3, px: 4, flex: 1 }}
            >
                {/* Justified body text */}
                <Typography
                    variant="body1"
                    sx={{
                        fontFamily: "inherit",
                        color: "text.primary",
                        lineHeight: 1.8,
                        mb: 2.5,
                        textAlign: "justify",
                    }}
                >
                    Welcome to{" "}
                    <Typography
                        component="span"
                        variant="body1"
                        sx={{ fontWeight: 600, fontFamily: "inherit", color: "primary.main" }}
                    >
                        Accounts Central
                    </Typography>
                    , an Open Banking demo environment. Use the{" "}
                    <Typography
                        component="span"
                        variant="body1"
                        sx={{ fontWeight: 600, fontFamily: "inherit", color: "text.primary" }}
                    >
                        Account Aggregation Flow
                    </Typography>{" "}
                    to securely link and view balances across multiple banks, or try the{" "}
                    <Typography
                        component="span"
                        variant="body1"
                        sx={{ fontWeight: 600, fontFamily: "inherit", color: "text.primary" }}
                    >
                        Payments Flow
                    </Typography>{" "}
                    to simulate real-time bill payments. All login and
                    OTP fields accept any alphanumeric input — no real credentials required.
                </Typography>

                {/* Feature chips — centered */}
                <Box
                    sx={{
                        display: "flex",
                        gap: 1,
                        flexWrap: "wrap",
                        justifyContent: "center",
                    }}
                >
                    {["Account Aggregation Flow", "Payments Flow", "Simulation Mode"].map((label) => (
                        <Chip
                            key={label}
                            label={label}
                            size="small"
                            color="primary"
                            variant="outlined"
                            sx={{
                                borderRadius: "6px",
                                fontFamily: "inherit",
                                fontWeight: 500,
                                fontSize: "0.75rem",
                            }}
                        />
                    ))}
                </Box>
            </DialogContent>

            <Divider />

            {/* ── Footer ── */}
            <DialogActions
                sx={{
                    px: 4,
                    py: 2,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                }}
            >
                <Typography
                    variant="caption"
                    sx={{ fontFamily: "inherit", color: "text.disabled" }}
                >
                    Demo environment — no real data is used
                </Typography>

                <Button
                    variant="contained"
                    color="primary"
                    onClick={handleClose}
                    sx={{
                        fontFamily: "inherit",
                        fontWeight: 600,
                        borderRadius: "50px",
                        px: 3,
                        py: 1,
                        textTransform: "none",
                        fontSize: "0.95rem",
                        boxShadow: "none",
                        "&:hover": {
                            boxShadow: "none",
                        },
                    }}
                >
                    OK, Got It
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default SplashScreen;
