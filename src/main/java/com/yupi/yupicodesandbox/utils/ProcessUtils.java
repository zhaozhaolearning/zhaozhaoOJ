package com.yupi.yupicodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import com.yupi.yupicodesandbox.model.ExecuteMessage;
import org.springframework.util.StopWatch;

import java.io.*;

/**
 * 进程工具类
 */
public class ProcessUtils {

    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {

        ExecuteMessage executeMessage = new ExecuteMessage();


        try {
            //在这里获取运行的时间
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            //等待程序的执行然后获取错误码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            if (exitValue == 0) {
                System.out.println(opName + "成功");

                //分批获取进程正常的输出 首先获取到输入（runProcess.getInputStream()）流然后读取输入流new InputStreamReader
                //然后将读取到的输入流分成模块赋值
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                //将内容进行拼接
                StringBuilder compileOutStringBuilder = new StringBuilder();
                //逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    compileOutStringBuilder.append(compileOutputLine);
                }
                executeMessage.setMessage(compileOutStringBuilder.toString());
            } else {
                //异常退出
                System.out.println(opName + "失败，错误码：" + exitValue);

                //分批获取进程正常的输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                //将内容进行拼接
                StringBuilder compileOutStringBuilder = new StringBuilder();
                //逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    //拼接
                    compileOutStringBuilder.append(compileOutputLine);
                }
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                //逐行读取
                String errorCompileOutputLine;
                StringBuilder errorCompileOutStringBuilder = new StringBuilder();
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    errorCompileOutStringBuilder.append(errorCompileOutputLine);
                }
                executeMessage.setErrorMessage(errorCompileOutStringBuilder.toString());
            }
            stopWatch.stop();
            //停止之后获取程序总共的输出时间
            long lastTaskTimeMillis = stopWatch.getLastTaskTimeMillis();
            executeMessage.setTime(lastTaskTimeMillis);

        } catch (Exception e) {
            e.printStackTrace();
        }


        return executeMessage;
    }

    /**
     * 执行交互式进程
     *
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String opName, String args) {

        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            //现在要从控制台读取信息和取出错误信息 所以要使用它的输入输出流

            //之前是从输入流获取程序的输出信息 所以要从输出流获取输入信息
            //向控制条输入程序
            OutputStream outputStream = runProcess.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);

            //将写进来的字符串进行分割
            String[] s = args.split(" ");
            String join = StrUtil.join("\n" , s)+"\n";

            outputStreamWriter.write(join);
            //相当于回车 正式将内容输入到控制台
            outputStreamWriter.flush();

            InputStream inputStream = runProcess.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            //将内容进行拼接
            StringBuilder compileOutStringBuilder = new StringBuilder();
            //逐行读取
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutStringBuilder.append(compileOutputLine);
            }
            executeMessage.setMessage(compileOutStringBuilder.toString());

            //记得流使用完之后要销毁 记得资源的释放否则会卡死
            outputStreamWriter.close();
            outputStream.close();
            inputStream.close();
            runProcess.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return executeMessage;
    }
}