package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class HasAppointInTodayForm {
    @NotBlank(message = "nameを入力してください。")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,10}$", message = "nameが不正です。")
    private String name;

    @NotBlank(message = "sexを入力してください。")
    @Pattern(regexp = "^男$|^女$", message = "sexが不正です。")
    private String sex;

    @NotBlank(message = "pidを入力してください。")
    @Pattern(regexp = "^[0-9Xx]{18}$", message = "身分証番号が無効です。")
    private String pid;
}
