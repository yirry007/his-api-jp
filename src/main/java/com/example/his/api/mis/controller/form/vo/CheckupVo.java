package com.example.his.api.mis.controller.form.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class CheckupVo {
    @NotBlank(message = "健康診断の項目は必須です。")
    @Length(max = 50, message = "健康診断の項目は50文字以内にしてください。")
    private String title;

    @NotBlank(message = "健康診断の内容は必須です。")
    @Length(max = 500, message = "健康診断の内容は50文字以内にしてください。")
    private String content;
}

