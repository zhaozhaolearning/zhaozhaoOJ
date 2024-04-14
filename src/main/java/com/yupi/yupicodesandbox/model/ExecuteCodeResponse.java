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
public class ExecuteCodeResponse {

    private List<String> outputList;

    /**
     * 执行信息 接口信息
     */
    private String message;

    /**
     * 执行的状态
     */
    private Integer status;

    private JudgeInfo judgeInfo;
}
