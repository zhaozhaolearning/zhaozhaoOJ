package com.yupi.yupicodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {

    private List<String> inputList;
    /**
     * 执行信息
     */
    private String message;

    private String code;
    /**
     * 执行代码的语言
     */
    private String language;
}
