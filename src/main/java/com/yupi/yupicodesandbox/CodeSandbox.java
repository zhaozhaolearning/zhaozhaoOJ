package com.yupi.yupicodesandbox;


import com.yupi.yupicodesandbox.model.ExecuteCodeRequest;
import com.yupi.yupicodesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱的接口定义
 */
public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
