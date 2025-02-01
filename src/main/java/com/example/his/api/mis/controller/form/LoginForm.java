package com.example.his.api.mis.controller.form;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class LoginForm {
    @NotBlank(message = "usernameを入力してください。")
    @Pattern(regexp = "^[a-zA-Z0-9]{5,50}$", message = "usernameが不正です。")
    private String username;

    @NotBlank(message = "passwordを入力してください。")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "passwordが不正です。")
    private String password;
}
