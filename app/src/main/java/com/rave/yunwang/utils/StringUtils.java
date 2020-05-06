package com.rave.yunwang.utils;

import android.text.TextUtils;

public class StringUtils {
    private static final String CHINESE_TEN_THOUSAND = "万";

    /**
     * 全角空格的值，它没有遵从与ASCII的相对偏移，必须单独处理
     */
    static final char SBC_SPACE = 12288; // 全角空格 12288
    /**
     * 半角空格的值，在ASCII中为32(Decimal)
     */
    static final char DBC_SPACE = ' '; // 半角空格
    /**
     * ASCII表中除空格外的可见字符与对应的全角字符的相对偏移
     */
    static final int CONVERT_STEP = 65248; // 全角半角转换间隔

    public static int indexof(String[] arr, String text) {
        if (arr == null || arr.length == 0 || TextUtils.isEmpty(text)) {
            return -1;
        }
        for (int i = 0; i < arr.length; i++) {
            if (text.equals(arr[i])) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isEmpty(CharSequence cs) {

        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {

        return !isEmpty(cs);
    }

    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static String addEnter(String content) {

        if (isEmpty(content))
            return content;

        String rstr = "\n\n";
        String str = "\n";
        String fourSpace = "　　";
        content = content.replace(" ", "　");
        // 先将所有2个回车转成1个回车
        while (content.indexOf(rstr) >= 0) {
            content = content.replaceAll(rstr, str);
        }
        // 清除开头和结尾的空格
        content = content.trim();
        // 回车后加一行
        content = content.replaceAll(str, rstr);

        if (content.indexOf(rstr) == 0) {
            content = str + content.substring(rstr.length());
        } else {
            content += fourSpace;
        }

        if (content.endsWith(rstr)) {
            content = content.substring(0, content.length() - rstr.length())
                    + str;
        }

        return content;
    }

    public static String makeLength(String str, int length) {
        int len = str.length();
        if (len > length) {
            return str.substring(0, length);
        }
        if (len < length) {
            String ts = str;
            for (int i = len; i < length; i++) {
                ts += "0";
            }
            return ts;
        }
        return str;
    }

    public static int calculateTextCount(CharSequence cs) {
        int strLen;
        int count = 0;
        if (cs == null || (strLen = cs.length()) == 0) {
            return count;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                count++;
            }
        }
        return count;
    }

    public static String ToSBC(String input) {
        // 半角转全角：
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 32) {
                c[i] = (char) 12288;
                continue;
            }
            if (c[i] < 127)
                c[i] = (char) (c[i] + 65248);
        }
        return new String(c);
    }

    /**
     * 全角转换为半角
     *
     * @param input
     * @return
     */
    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    /**
     * 当数字太大时转换成合适的中文字符串(如11000->1.1万)
     *
     * @param number 待转换的数字
     * @return
     */
    public static String bigNumberFormat(int number) {
        String result = number + "";
        if (number > 10000) {
            result = number / 10000 + CHINESE_TEN_THOUSAND;
        }
        return result;
    }

    public static String getMaskString(String content, int begin, int end, String mask) {
        if (begin >= content.length() || begin < 0) {
            return content;
        }
        if (end >= content.length() || end < 0) {
            return content;
        }
        if (begin >= end) {
            return content;
        }
        String starStr = "";
        for (int i = begin; i < end; i++) {
            starStr = starStr + mask;
        }
        return content.substring(0, begin) + starStr + content.substring(end, content.length());

    }
}
