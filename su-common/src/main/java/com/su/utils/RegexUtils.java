package com.su.utils;

import java.util.regex.Pattern;

public class RegexUtils {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern ID_NUMBER_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$");

    public static boolean isPhoneInvalid(String phone) {
        return phone == null || !PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isIdNumberInvalid(String idNumber) {
        return idNumber == null || !ID_NUMBER_PATTERN.matcher(idNumber).matches();
    }
}
