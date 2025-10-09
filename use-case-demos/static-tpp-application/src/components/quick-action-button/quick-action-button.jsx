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

import "./quick-action-button.css"
import {Box,IconButton} from "@oxygen-ui/react";

/**
 * A highly reusable and responsive button component specifically designed for a quick action list.
 *
 * It utilizes the **Oxygen UI Grid** system to manage a compact, clickable layout.
 * The component expects the **`children`** prop to be an array containing exactly two elements:
 * 1. The **icon** element (`iconElement`).
 * 2. The **text label** element (`nameText`).
 *
 * This structure allows for precise, responsive alignment of the icon (top) and label (bottom).
 *
 * @param {object} props - The component props.
 * @param {Array<React.ReactNode>} props.children - An array where the first element is the icon component and the second is the name text/label.
 * @param {function} props.onClick - The function to be executed when the button's surrounding `div` is clicked.
 */
export const QuickActionButton = ({children}) => {

     const [iconElement, nameText] = children;

    return (
        <IconButton >
            <Box display="flex" flexDirection="column" width={{md:'2rem',sm:'2rem',xs:'2rem'}} sx={{fontSize: {md:'1rem',sm:'0.8rem',xs:'0.8rem'}, gap:'0.5rem', background:'white', padding:{md:'1rem 3rem',sm:'1rem 3rem',xs:'0.8rem 2rem'}, justifyContent:'center', alignItems: 'center', color:'red', borderRadius:'5%', boxShadow:'4.5px 4.5px 5.625px -2.25px var(--color-primary)'}}>
                {iconElement}
                {nameText}
            </Box>
        </IconButton>
    );
};

