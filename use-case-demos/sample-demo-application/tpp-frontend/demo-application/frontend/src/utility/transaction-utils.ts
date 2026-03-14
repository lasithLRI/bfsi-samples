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

/**
 * Generates a random transaction ID with exactly 8 digits
 * Ensures uniqueness by checking against existing transaction IDs
 *
 * @param {Array} existingTransactions - Array of existing transactions to avoid duplicates
 * @returns {string} Transaction ID in format T00123460 (8 digits)
 *
 * @example
 * generateTransactionId([]) // Returns "T00123460" (random)
 * generateTransactionId([{id: "T00123460"}]) // Returns different random ID
 */
export const generateTransactionId = (existingTransactions: any[] = []): string => {
    const existingIds = new Set(
        existingTransactions.map(txn => txn.id)
    );

    let newId: string;
    let attempts = 0;
    const maxAttempts = 100;

    do {
        // Generate random 8-digit number (10000000 to 99999999)
        const randomNumber = Math.floor(Math.random() * 90000000) + 10000000;
        newId = `T${randomNumber}`;
        attempts++;

        if (attempts >= maxAttempts) {
            // Fallback: use timestamp-based ID if we can't find unique random
            const timestamp = Date.now().toString().slice(-8);
            newId = `T${timestamp}`;
            break;
        }
    } while (existingIds.has(newId));

    return newId;
};

/**
 * Alias for generateTransactionId for backward compatibility
 * Generates a unique random transaction ID
 *
 * @param {Array} existingTransactions - Array of existing transactions
 * @returns {string} Random transaction ID
 *
 * @example
 * const txns = [{ id: "T12345678" }, { id: "T87654321" }];
 * getNextTransactionId(txns) // Returns "T45678901" (random, unique)
 */
export const getNextTransactionId = (existingTransactions: any[]): string => {
    return generateTransactionId(existingTransactions);
};
