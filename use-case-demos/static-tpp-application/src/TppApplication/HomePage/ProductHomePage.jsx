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

import HomeHeaderContent from "./HomeHeader/HomeHeaderContent.jsx";
import useAuthContext from "../../hooks/useAuthContext.js";

/**
 * The main component for the product's home page.
 * It fetches user information using the `useAuthContext` custom hook and
 * passes this data to the `HomeHeaderContent` component to display user-specific content.
 */
const ProductHomePage = () => {
    const userInfo = useAuthContext();
    return (
        <>
            <HomeHeaderContent userInfo={userInfo}/>
        </>
    )
}

export default ProductHomePage;

