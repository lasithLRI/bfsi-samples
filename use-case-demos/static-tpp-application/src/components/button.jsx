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

import "./button.css"

/**
 * A versatile, reusable button component that can function as either a
 * Quick Action Button (with icon and text/children) or an Icon Button
 * (with only an icon).
 *
 * It determines its style and content based on the presence of `children`.
 *
 * @param {object} props - The component props.
 * @param {React.ReactNode} props.icon - The JSX or component for the button's icon.
 * @param {React.ReactNode} [props.children] - The text or content to display next to the icon (makes it a Quick Action Button).
 * @param {function} props.onClick - The function to call when the button is clicked.
 */
export const Button = ({icon, children, onClick, isQuickActionButton}) => {

    const quickActionClasses = 'quick-action-button-outer';
    const iconButtonClasses = 'product-header-user-icon-button-outer product-header-user-icon-button-content';


    return (
        <div className={`${isQuickActionButton ? quickActionClasses : iconButtonClasses}`}>
            <button onClick={onClick}>
                {icon}
                {isQuickActionButton && <p>{children}</p>}
            </button>
        </div>
    );
};

