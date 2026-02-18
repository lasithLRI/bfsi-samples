package com.wso2.openbanking.utils;

import java.math.BigInteger;
import java.util.UUID;

public class JwtUtils {

    private JwtUtils() {}

    public static String generateJti() {
        return new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toString();
    }
}
