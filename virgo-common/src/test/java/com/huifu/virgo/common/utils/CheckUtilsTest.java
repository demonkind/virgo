package com.huifu.virgo.common.utils;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by jianfei.chen on 2015/3/10.
 */
public class CheckUtilsTest {

    @Test
    public void matchURLWithSecondDomain() {
        String p = "^(http|https):\\/\\/([a-z0-9]+\\.)*?(huifu.com)";
        Pattern pattern = Pattern.compile(p);
        assertThat(CheckUtils.matchURL(pattern, "http://huifu.com/asdf")).isTrue();
        assertThat(CheckUtils.matchURL(pattern, "http://www.huifu.com/asdf")).isTrue();
        assertThat(CheckUtils.matchURL(pattern, "http://aa.www.huifu.com/asdf")).isTrue();
        assertThat(CheckUtils.matchURL(pattern, "http://huifu.com")).isTrue();
        assertThat(CheckUtils.matchURL(pattern, "https://huifu.com")).isTrue();
        assertThat(CheckUtils.matchURL(pattern, "https://huifu.com/asdf")).isTrue();

        assertThat(CheckUtils.matchURL(pattern, "http://huifu1.com/?http://huifu.com")).isFalse();
    }

    @Test
    public void matchURLWithFirstDomain() {
        String p = "^(http|https):\\/\\/([a-z0-9]+\\.)*?(www.huifu.com)";
        Pattern pattern = Pattern.compile(p);
        assertThat(CheckUtils.matchURL(pattern, "http://www.huifu.com")).isTrue();
        assertThat(CheckUtils.matchURL(pattern, "http://www.huifu.com/asdf")).isTrue();
        assertThat(CheckUtils.matchURL(pattern, "http://www.qq.huifu.com/asdf")).isFalse();
        assertThat(CheckUtils.matchURL(pattern, "http://huifu.com")).isFalse();
        assertThat(CheckUtils.matchURL(pattern, "https://huifu.com")).isFalse();
        assertThat(CheckUtils.matchURL(pattern, "https://huifu.com/asdf")).isFalse();

        assertThat(CheckUtils.matchURL(pattern, "http://www.huifu1.com/?http://www.huifu.com")).isFalse();
    }


    @Test
    public void matchURLWithIP() {
        String p = "^(http|https):\\/\\/([a-z0-9]+\\.)*?(192.168.12.1)";
        Pattern pattern = Pattern.compile(p);
        assertThat(CheckUtils.matchURL(pattern, "http://192.168.12.1")).isTrue();
        assertThat(CheckUtils.matchURL(pattern, "http://192.168.12.1:12333")).isTrue();
        assertThat(CheckUtils.matchURL(pattern, "http://192.168.12.1/?http://www.huifu.com")).isTrue();
    }

    @Test
    public void checkBlackList() {
        String checkList = "huifu.com,黄页.com,chinapnr.com,,souhu.com,黄页.cn";
        assertThat(CheckUtils.isList(checkList, "http://huifu.com")).isTrue();
        assertThat(CheckUtils.isList(checkList,"http://www.huifu.com")).isTrue();
        assertThat(CheckUtils.isList(checkList,"http://www.chinapnr.com")).isTrue();
        assertThat(CheckUtils.isList(checkList,"http://www.sohu.com")).isFalse();
        assertThat(CheckUtils.isList(checkList,"http://www.sohu.com")).isFalse();
        assertThat(CheckUtils.isList(checkList,"http://www.sohu.com")).isFalse();
        assertThat(CheckUtils.isList(checkList,"http://www.黄页.cn")).isTrue();
    }

}
