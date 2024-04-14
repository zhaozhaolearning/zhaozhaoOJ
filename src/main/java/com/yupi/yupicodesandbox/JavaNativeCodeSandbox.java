package com.yupi.yupicodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.yupi.yupicodesandbox.model.ExecuteCodeRequest;
import com.yupi.yupicodesandbox.model.ExecuteCodeResponse;
import com.yupi.yupicodesandbox.model.ExecuteMessage;
import com.yupi.yupicodesandbox.model.JudgeInfo;
import com.yupi.yupicodesandbox.security.DefaultSecurityManager;
import com.yupi.yupicodesandbox.security.DenySecurityManager;
import com.yupi.yupicodesandbox.utils.ProcessUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StopWatch;
import sun.reflect.misc.FieldUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JavaNativeCodeSandbox implements CodeSandbox {

    //全局的文件路径
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "main.java";

    private static final long TIME_OUT = 5000L;

    //定义需要校验的黑名单
    private static final List<String> blackList = Arrays.asList("Files","exec");

    private static final String SECURITY_MANAGER_PATH = "C:\\IdeaProjects\\yupi-code-sandbox\\src\\main\\resources\\security";

    private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";

    private static final WordTree WORD_TREE = new WordTree();

    static {
        WORD_TREE.addWords(blackList);
    }



    public static void main(String[] args) {
        //编写测试代码
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "2 3"));
        //在这里我们将resource中的文件读取到code中
        //使用ResourceUtil.readStr() 直接读取Resource目录下的文件
        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/main.java", StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/simpleCompute/main.java", StandardCharsets.UTF_8);
        String code = ResourceUtil.readStr("testCode/unsafeCode/writeDangerCode.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        //然后将请求体传给接口得到响应
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);

    }


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        //System.setSecurityManager(new DefaultSecurityManager());

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        //使用WorkTree校验黑白名单
//        FoundWord foundWord = WORD_TREE.matchWord(code);
//        if (foundWord != null){
//            System.out.println("包含禁词：" + foundWord.getFoundWord());
//            return null;
//        }

        //获取当前项目的根目录
        String userDir = System.getProperty("user.dir");
        //定义目标文件是否存在 由于在不同的系统中对于分隔符的要求也不一样所以我们使用File.separator来进行分隔达到兼容性强的效果
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        //判断全局代码目录是否存在
        if (!FileUtil.exist(globalCodePathName)) {
            //如果没有这个目录就创建一个
            FileUtil.mkdir(globalCodePathName);
        }
        //接下来保存用户的代码
        //把用户的代码隔离分级存放 每次存放用文件夹隔离存放
        //拿到用户的文件夹目录
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        //实际文件存放的目录
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;

        //然后将代码写入到文件当中 然后定义路径，文件名，最后加上编码规则
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);


        //2.编译代码，得到class 文件
        //编译命令
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        //实现命令行命令
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            //等待程序的执行然后获取错误码
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);

        } catch (Exception e) {
            getErrorResponse(e);
        }
        //3.执行代码，得到输出结果


        //创建list接收结果
        ArrayList<ExecuteMessage> executeMessageArrayList = new ArrayList<>();
        for (String inputArgs : inputList) {

            //String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s main %s", userCodeParentPath, inputArgs);
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s main %s",
                    userCodeParentPath,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS_NAME, inputArgs);

            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                //我们先new一个线程(守护线程) 超过时间没完成就自动销毁正在执行任务的进程
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时啦");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();


                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                //ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, "运行", inputArgs);
                System.out.println(executeMessage);
                executeMessageArrayList.add(executeMessage);
            } catch (Exception e) {
                getErrorResponse(e);
            }
        }
        //4.收集整理输出结果
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        ArrayList<String> outputList = new ArrayList<>();
        //取最大值判断是否超时
        long maxTime = 0;
        //设置判断如果是正确的信息就放进列表里面 如果是错误的信息就报错
        for (ExecuteMessage executeMessage : executeMessageArrayList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                //执行中存在错误：3
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
            //取到最大值
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }
        //如果正常运行 那就大小一致
        if (outputList.size() == executeMessageArrayList.size()) {
            executeCodeResponse.setStatus(1);
        }

        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
//        judgeInfo.setMemory();
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        //5.文件清理 有文件才删除
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
        }


        return executeCodeResponse;
    }

    //6.错误处理
    private ExecuteCodeResponse getErrorResponse(Throwable e){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        //发生错误给响应的信息赋空值
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        //表示代码沙箱错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }


}
