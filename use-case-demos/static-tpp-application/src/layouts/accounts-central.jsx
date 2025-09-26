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

import "./accounts-central.css"
import {Button} from "../components/forms/button.jsx";
import Logout from "/public/resources/assets/images/icons/logout_icon.svg?react"

/**
 * A layout component that establishes the standard structure for the 'Accounts Central' product view.
 *
 * It renders a fixed product header containing the title "Accounts Central" and a
 * logout/user icon button using the reusable `CommonButton`. The main content area
 * for the page is rendered by the `children` prop.
 *
 * @param {object} props - The component props.
 * @param {React.ReactNode} props.children - The content (usually nested routes or components) to be rendered in the main product content area.
 */
export const AccountsCentral = ({children}) => {
    return (
        <>
            <div className="product-header-outer">
                <p>Accounts Central</p>
                <Button icon={<Logout/>} onClick={() => {
                }} isQuickActionButton={false}/>
            </div>

            <div className="product-content-outer">
                {children}
            </div>
        </>
    );
}

export default AccountsCentral;

