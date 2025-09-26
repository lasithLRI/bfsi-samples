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

import {useEffect, useRef, useState} from "react";

/**
 * A custom React hook that fetches and manages user information.
 * It uses `useEffect` to asynchronously fetch user data from the
 * '/configurations/config.json' endpoint and returns the `user` object
 * from the response.
 * @returns {object|null} The user information object, or `null` while loading.
 */
const useAuthContext = () => {

    const [user, setUser] = useState({});
    const fetchedRef = useRef(false);

    useEffect(() => {

        if (fetchedRef.current) {
            return;
        }
            const fetchData = async () => {

                const response = await fetch("/configurations/config.json");
                const data = await response.json();
                setUser(data.user);
                fetchedRef.current = true;
            }
            fetchData()

    }, [])

    return user
};

export default useAuthContext;

