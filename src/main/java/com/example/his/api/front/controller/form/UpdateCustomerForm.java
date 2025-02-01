package com.example.his.api.front.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class UpdateCustomerForm {
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,10}$", message = "nameが不正です。")
    private String name;

    @Pattern(regexp = "^男$|^女$", message = "sexが不正です。")
    private String sex;

    @NotBlank(message = "telを入力してください。")
    @Pattern(regexp = "^1[1-9]\\d{9}$", message = "telが不正です。")
    private String tel;
}
