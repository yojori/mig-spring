package com.yojori.util;

import java.util.StringTokenizer;

public class StringUtil {
    static final int KOR_S = 0xAC00;
    static final int KOR_F = 0xD743;
    static final String STR_TAIL = "...";
    static final int STR_SZ = 3;

    private StringUtil() {
    }

    public static boolean empty(String str) {
        return str == null || str.length() == 0;
    }

    public static String nvl(String strOrg) {
        if (strOrg != null && strOrg.length() > 0) {
            return strOrg.trim();
        } else {
            return "";
        }
    }

    public static String nvl(Object obj) {
        if (obj == null)
            return "";
        else
            return (String) obj;
    }

    public static String nvl(String strOrg, String strDef) {
        String strValue = nvl(strOrg);

        if (!"".equals(strValue)) {
            return strValue;
        } else {
            return strDef;
        }
    }

    public static int nvl(String strOrg, int nDef) {
        String strValue = nvl(strOrg);

        if (!"".equals(strValue)) {
            return Integer.parseInt(strValue);
        } else {
            return nDef;
        }
    }

    public static long nvl(String strOrg, long nDef) {
        String strValue = nvl(strOrg);

        if (!"".equals(strValue)) {
            return Long.parseLong(strValue);
        } else {
            return nDef;
        }
    }

    public static float nvl(String strOrg, float nDef) {
        String strValue = nvl(strOrg);

        if (!"".equals(strValue)) {
            return Float.parseFloat(strValue);
        } else {
            return nDef;
        }
    }

    public static double nvl(String strOrg, double nDef) {
        String strValue = nvl(strOrg);

        if (!"".equals(strValue)) {
            return Double.parseDouble(strValue);
        } else {
            return nDef;
        }
    }

    public static String toDB(String orgVal) {
        StringBuffer sb = null;
        char[] array = null;

        if (orgVal != null && orgVal.length() > 0) {
            sb = new StringBuffer();
            array = orgVal.toCharArray();

            for (int i = 0; i < array.length; i++) {
                if (array[i] == '<') {
                    sb.append("&lt;");
                } else if (array[i] == '>') {
                    sb.append("&gt;");
                } else if (array[i] == '\'') {
                    sb.append("&#39;");
                } else if (array[i] == '\"') {
                    sb.append("&#34;");
                } else if (array[i] == '-') {
                    sb.append("&#45;");
                } else if (array[i] == '*') {
                    sb.append("&#42;");
                } else {
                    sb.append(array[i]);
                }
            }
        }

        if (sb != null) {
            return sb.toString();
        } else {
            return "";
        }
    }

    public static String fromDB(String orgVal) {
        StringBuffer sb = null;
        char[] array = null;

        if (orgVal != null && orgVal.length() > 0) {
            sb = new StringBuffer();
            array = orgVal.toCharArray();

            for (int i = 0; i < array.length; i++) {
                if (array[i] == '&') {
                    if ((i + 4) <= array.length) {
                        if (array[i + 1] == 'l') {
                            if ((i + 3) <= array.length && array[i + 2] == 't' && array[i + 3] == ';') {
                                sb.append("<");
                                i = i + 3;
                            } else {
                                sb.append(array[i]);
                            }
                        } else if (array[i + 1] == 'g') {
                            if ((i + 3) <= array.length && array[i + 2] == 't' && array[i + 3] == ';') {
                                sb.append(">");
                                i = i + 3;
                            } else {
                                sb.append(array[i]);
                            }
                        } else if (array[i + 1] == '#') {
                            if ((i + 4) <= array.length && array[i + 2] == '3' && array[i + 3] == '9'
                                    && array[i + 4] == ';') {
                                sb.append("'");
                                i = i + 4;
                            } else if ((i + 4) <= array.length && array[i + 2] == '4' && array[i + 3] == '5'
                                    && array[i + 4] == ';') {
                                sb.append("-");
                                i = i + 4;
                            } else if ((i + 4) <= array.length && array[i + 2] == '4' && array[i + 3] == '2'
                                    && array[i + 4] == ';') {
                                sb.append("*");
                                i = i + 4;
                            } else {
                                sb.append(array[i]);
                            }
                        } else {
                            sb.append(array[i]);
                        }
                    } else {
                        sb.append(array[i]);
                    }
                } else {
                    sb.append(array[i]);
                }
            }
        }

        if (sb != null) {
            return sb.toString();
        } else {
            return "";
        }
    }

    public static String replace(String orgStr, String oldStr, String newStr) {
        String convert = new String();
        int pos = 0;
        int begin = 0;

        pos = orgStr.indexOf(oldStr);

        if (pos == -1) {
            return orgStr;
        } else {
            while (pos != -1) {
                convert = convert + orgStr.substring(begin, pos) + newStr;
                begin = pos + oldStr.length();
                pos = orgStr.indexOf(oldStr, begin);
            }

            convert = convert + orgStr.substring(begin);

            return convert;
        }
    }

    public static String[] makeArrayToString(String strOrg, String token) {
        String as[] = null;

        if (!"".equals(nvl(strOrg))) {
            StringTokenizer stringtokenizer = new StringTokenizer(strOrg, token);
            int i = 0;
            as = new String[i = stringtokenizer.countTokens()];
            for (int j = 0; j < i; j++) {
                as[j] = stringtokenizer.nextToken();
            }
        }

        return as;
    }

    public static String makeStringToArray(String[] array, String token) {
        StringBuffer sb = new StringBuffer();

        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                if (i != 0) {
                    sb.append(token);
                }

                sb.append(array[i]);
            }
        }

        return sb.toString();

    }

    public static String separateToNumber(String s, int len, String separator) {
        StringBuffer s1 = new StringBuffer(s).reverse();

        int sLen = s1.toString().length();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < sLen; i++) {
            if (i % len == 0 && i != 0) {
                sb.append(separator);
            }

            sb.append(s1.charAt(i));
        }

        return sb.reverse().toString();
    }

    public static String commaToMoney(long money) {
        return separateToNumber(String.valueOf(money), 3, ",");
    }

    public static String commaToMoney(int money) {
        return separateToNumber(String.valueOf(money), 3, ",");
    }

    public static void mapOverWrite(java.util.Map<String, Object> map, String key, Object value) {
        if (map != null) {
            if (map.containsKey(key)) {
                map.remove(key);
            }

            map.put(key, value);
        }
    }

    public static String sz(String str, int size) {
        StringBuffer sb = new StringBuffer();
        int nChars = 0;
        int len = str.length();
        int blen = str.getBytes().length;

        /** 표시하려는 길이가 같은 경우 */
        if (blen <= size) {
            return str;
        }

        for (int idx = 0; idx < len; idx++) {
            char ch = str.charAt(idx);
            if (nChars >= (size - STR_SZ)) {
                break;
            }
            if (isKor(ch)) {
                nChars += 2;
            } else {
                nChars++;
            }
            sb.append(ch);
        }
        sb.append(STR_TAIL);

        return sb.toString();
    }

    public static boolean isKor(char ch) {
        if (ch >= KOR_S && ch <= KOR_F) {
            return true;
        }

        return false;
    }

    public static String dbSearchChk(String strVal) {
        String str = "";

        if (!"".equals(strVal)) {
            str = replace(strVal, "--", "");
            str = replace(str, "\"", "");
            str = replace(str, "'", "");
            str = replace(str, "/", "");
            str = replace(str, "*", "");
            str = replace(str, "%", "");
        }

        return str;
    }

    // JSP specific method removed: escapeXml
    // JSP specific method removed: convertHtmlchars

    public static String getSeperateString(String value, int index, String seperator) {

        if (value != null && value.length() > 0) {
            String[] values = value.split(seperator);

            return values[index];
        }

        return "";
    }
}
