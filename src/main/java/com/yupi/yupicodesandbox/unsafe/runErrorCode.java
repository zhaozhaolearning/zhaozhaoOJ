package com.yupi.yupicodesandbox.unsafe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class runErrorCode {
    public static void main(String[] args) throws IOException, InterruptedException {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/木马程序.bat";

        Process process = Runtime.getRuntime().exec(filePath);
        process.waitFor();
        //分批获取进程的正常输出
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        //逐行读取
        String compileOutLine;
        while ((compileOutLine = bufferedReader.readLine()) != null) {
            System.out.println(compileOutLine);
        }
        System.out.println("执行异常程序成功");
    }
}
