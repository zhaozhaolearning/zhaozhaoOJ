package com.yupi.yupicodesandbox.security;

import cn.hutool.core.io.FileUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestSecurityManager {
    public static void main(String[] args) {
        //开启安全管理器
        System.setSecurityManager(new MySecurityManager());
        FileUtil.writeString("老大","aaa", Charset.defaultCharset());
    }
}
