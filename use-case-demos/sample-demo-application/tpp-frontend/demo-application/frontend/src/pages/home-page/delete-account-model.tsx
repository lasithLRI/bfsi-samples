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

import { useState } from "react";
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    List,
    ListItem,
    ListItemButton,
    ListItemText,
    Typography,
    Divider,
    CircularProgress,
    Box,
    Alert
} from "@oxygen-ui/react";
import useConfigContext from "../../hooks/use-config-context.ts";

interface DeleteAccountModalProps {
    open: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

interface SelectedAccount {
    accountId: string;
    accountName: string;
    bankName: string;
}

export const DeleteAccountModal = ({ open, onClose, onSuccess }: DeleteAccountModalProps) => {
    const { banksWithAccounts, getAffectedAccounts, revokeConsent } = useConfigContext();

    const [selected, setSelected] = useState<SelectedAccount | null>(null);
    const [affectedAccounts, setAffectedAccounts] = useState<{ id: string; name: string }[]>([]);
    const [previewLoading, setPreviewLoading] = useState(false);
    const [deleteLoading, setDeleteLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSelect = async (accountId: string, accountName: string, bankName: string) => {
        setSelected({ accountId, accountName, bankName });
        setError(null);
        setPreviewLoading(true);
        try {
            const affected = await getAffectedAccounts(accountId, bankName);
            setAffectedAccounts(affected);
        } catch {
            setError("Failed to load affected accounts.");
        } finally {
            setPreviewLoading(false);
        }
    };

    const handleConfirm = async () => {
        if (!selected) return;
        setDeleteLoading(true);
        setError(null);
        try {
            const success = await revokeConsent(selected.accountId, selected.bankName);
            if (success) {
                handleClose();
                onSuccess();
            } else {
                setError("Failed to delete account. Please try again.");
            }
        } catch {
            setError("An error occurred. Please try again.");
        } finally {
            setDeleteLoading(false);
        }
    };

    const handleClose = () => {
        setSelected(null);
        setAffectedAccounts([]);
        setError(null);
        onClose();
    };

    return (
        <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
            <DialogTitle>Delete Account</DialogTitle>
            <DialogContent dividers>
                {banksWithAccounts.length === 0 ? (
                    <Typography variant="body2" color="text.secondary">
                        No accounts found.
                    </Typography>
                ) : (
                    banksWithAccounts.map(({ bank, accounts }) => (
                        <Box key={bank.name} mb={2}>
                            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                                {bank.name}
                            </Typography>
                            <Divider />
                            <List disablePadding>
                                {accounts.map((account) => (
                                    <ListItem key={account.id} disablePadding>
                                        <ListItemButton
                                            selected={selected?.accountId === account.id}
                                            onClick={() => handleSelect(account.id, account.name, bank.name)}
                                        >
                                            <ListItemText
                                                primary={account.name}
                                                secondary={account.id}
                                            />
                                        </ListItemButton>
                                    </ListItem>
                                ))}
                            </List>
                        </Box>
                    ))
                )}

                {previewLoading && (
                    <Box display="flex" justifyContent="center" mt={1}>
                        <CircularProgress size={20} />
                    </Box>
                )}

                {!previewLoading && selected && affectedAccounts.length > 1 && (
                    <Alert severity="warning" sx={{ mt: 2 }}>
                        <Typography variant="body2" fontWeight="bold" gutterBottom>
                            The following accounts will also be deleted:
                        </Typography>
                        {affectedAccounts
                            .filter((acc) => acc.id !== selected.accountId)
                            .map((acc) => (
                                <Typography key={acc.id} variant="body2">
                                    • {acc.name} ({acc.id})
                                </Typography>
                            ))}
                    </Alert>
                )}

                {error && (
                    <Typography variant="body2" color="error" mt={1}>
                        {error}
                    </Typography>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose} disabled={deleteLoading}>
                    Cancel
                </Button>
                <Button
                    onClick={handleConfirm}
                    disabled={!selected || previewLoading || deleteLoading}
                    color="error"
                    variant="contained"
                >
                    {deleteLoading ? <CircularProgress size={20} /> : "Delete"}
                </Button>
            </DialogActions>
        </Dialog>
    );
};