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

package com.wso2.openbanking.demo.utils;

/** HtmlResponseBuilder implementation. */
public class HtmlResponseBuilder {

    /**
     * Executes the buildAuthRedirectPage operation and modify the payload if necessary.
     */
    public static String buildAuthRedirectPage() {
        return buildAuthRedirectPage(ConfigLoader.getFrontendHomeUrl());
    }

    /**
     * Executes the buildAuthRedirectPage operation and modify the payload if necessary.
     *
     * @param frontendHomeUrl The frontendHomeUrl parameter
     */
    public static String buildAuthRedirectPage(String frontendHomeUrl) {
        return "<html>\n" +
                "<head>\n" +
                "    <title>Completing Authentication...</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: sans-serif;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            height: 100vh;\n" +
                "            margin: 0;\n" +
                "            background: white;\n" +
                "            color: black;\n" +
                "        }\n" +
                "        #status {\n" +
                "            font-size: 1.2em;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id='status'>Finalizing your connection...</div>\n" +
                "    <script>\n" +
                "        const hash = window.location.hash.substring(1);\n" +
                "        const params = new URLSearchParams(hash);\n" +
                "\n" +
                "        const idToken = params.get('id_token');\n" +
                "        const code = params.get('code');\n" +
                "        const state = params.get('state');\n" +
                "        const sessionState = params.get('session_state');\n" +
                "\n" +
                "        fetch('https://obiam:9446/ob-demo-backend-1.0.0/init/processAuth?' +\n" +
                "            'code=' + encodeURIComponent(code) +\n" +
                "            '&state=' + encodeURIComponent(state) +\n" +
                "            '&session_state=' + encodeURIComponent(sessionState) +\n" +
                "            '&id_token=' + encodeURIComponent(idToken))\n" +
                "        .then(response => {\n" +
                "            if (response.ok) {\n" +
                "                window.location.href = '" + frontendHomeUrl + "';\n" +
                "            } else {\n" +
                "                document.getElementById('status').innerHTML = 'Processing failed. " +
                "Please try again.';\n" +
                "            }\n" +
                "        })\n" +
                "        .catch(() => {\n" +
                "            document.getElementById('status').innerHTML = 'Connection error.';\n" +
                "        });\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }
}
