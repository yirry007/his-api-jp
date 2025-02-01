package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class UpdatePasswordForm {
    @NotBlank(message = "passwordを入力してください。")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "passwordが不正です。")
    String password;

    @NotBlank(message = "newPassword")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$",message = "newPasswordが不正です。")
    private String newPassword;
}

