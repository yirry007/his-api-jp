package com.example.his.api.front.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SendSmsCodeForm {
    @NotBlank(message = "telを入力してください。")
    @Pattern(regexp = "^1[1-9]\\d{9}$", message = "telが不正です。")
    private String tel;
}
