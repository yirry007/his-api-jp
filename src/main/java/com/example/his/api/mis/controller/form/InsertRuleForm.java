package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class InsertRuleForm {
    @NotBlank(message = "nameを入力してください。")
    @Pattern(regexp = "^[0-9a-zA-Z\\u4e00-\\u9fa5]{1,20}$", message = "nameが不正です。")
    private String name;

    @NotBlank(message = "ruleを入力してください。")
    private String rule;

    private String remark;
}
