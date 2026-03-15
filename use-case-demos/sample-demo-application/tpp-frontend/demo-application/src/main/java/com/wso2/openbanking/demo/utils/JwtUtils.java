package com.wso2.openbanking.demo.utils;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Utility class providing helper methods for JWT operations.
 * This class is not instantiable.
 */
public class JwtUtils {

    /**
     * Generates a unique JWT ID (jti) by converting a random UUID to a numeric string.
     *
     * @return a unique numeric string suitable for use as a JWT ID
     */
    public static String generateJti() {
        return new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16).toString();
    }
}