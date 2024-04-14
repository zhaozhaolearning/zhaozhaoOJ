package com.yupi.yupicodesandbox.unsafe;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class readFileError {
    /**
     * 读取服务器文件（文件信息泄漏0
     * @param args
     */
    public static void main(String[] args) throws IOException {

        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/application.yml";
        List<String> readAllLines = Files.readAllLines(Paths.get(filePath));
        //在每一个数据前面加上一个回车
        System.out.println(String.join("\n",readAllLines));

    }
}
