package com.yojori.util;

import java.util.StringTokenizer;

public class StringUtil {
    public static boolean empty(String str) {
        return str == null || str.length() == 0;
    }

    public static String nvl(String strOrg) {
        return (strOrg != null && strOrg.length() > 0) ? strOrg.trim() : "";
    }

    // Basic implementation sufficient for Migration logic
}
