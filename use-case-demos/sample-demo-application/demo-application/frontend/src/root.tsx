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

import { QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '@asgardeo/auth-react';
import App from './app.tsx';
import { authConfig } from './authConfig.ts';
import { queryClient } from './utility/query-client.ts';

const basename = window.location.pathname.split("/").slice(0, 2).join("/") || "/";

/**
 * @component Root
 * @description Wraps the App with global providers.
 * SplashScreen state is managed inside App so it sits within AppThemeProvider.
 */
const Root = () => (
    <QueryClientProvider client={queryClient}>
        <BrowserRouter basename={basename}>
            <AuthProvider config={authConfig}>
                <App />
            </AuthProvider>
        </BrowserRouter>
    </QueryClientProvider>
);

export default Root;
