package com.wso2.openbanking.utils;

import com.wso2.openbanking.ConfigLoader;

public class HtmlResponseBuilder {

    public static String buildAuthRedirectPage() {
        return buildAuthRedirectPage(ConfigLoader.getFrontendHomeUrl());
    }

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
                "        fetch('https://tpp.local.ob/ob_demo_backend_war/init/processAuth?' +\n" +
                "            'code=' + encodeURIComponent(code) +\n" +
                "            '&state=' + encodeURIComponent(state) +\n" +
                "            '&session_state=' + encodeURIComponent(sessionState) +\n" +
                "            '&id_token=' + encodeURIComponent(idToken))\n" +
                "        .then(response => {\n" +
                "            if (response.ok) {\n" +
                "                window.location.href = '" + frontendHomeUrl + "';\n" +
                "            } else {\n" +
                "                document.getElementById('status').innerHTML = 'Processing failed. Please try again.';\n" +
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
