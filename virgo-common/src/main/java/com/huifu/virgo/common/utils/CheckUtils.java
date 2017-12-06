package com.huifu.virgo.common.utils;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jianfei.chen on 2015/3/10.
 */
public class CheckUtils {


    /**
     * 在名单中出现
     */
    public static boolean isList(String checkList, String orgURL) {
        if (checkList == null) {
            return false;
        }

        String[] list = checkList.split(",", -1);
        for (String s : list) {
            // 当出现为空的选项时，跳过检查。因为在split中可能出现多个连续的逗号或末尾逗号
            if (StringUtils.isEmpty(s)) continue;
            Pattern pattern = getPattern(s);
            if (matchURL(pattern, orgURL)) return true;
        }
        return false;
    }


    public static boolean matchURL(Pattern pattern, String s) {
        Matcher matcher = pattern.matcher(s.trim().toLowerCase());
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    private static Pattern getPattern(String patternString) {
        String p = "^(http|https):\\/\\/([a-z0-9]+\\.)*?(" + patternString + ")";
        return Pattern.compile(p);
    }


}
