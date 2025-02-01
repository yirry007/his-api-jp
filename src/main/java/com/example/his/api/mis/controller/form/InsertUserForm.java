package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class InsertUserForm {
    @NotBlank(message = "usernameを入力してください。")
    @Pattern(regexp = "^[a-zA-Z0-9]{5,20}$", message = "usernameが不正です。")
    private String username;

    @NotBlank(message = "passwordを入力してください。")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "passwordが不正です。")
    private String password;

    @NotBlank(message = "nameを入力してください。")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,10}$", message = "nameが不正です。")
    private String name;

    @NotBlank(message = "sexを入力してください。")
    @Pattern(regexp = "^男$|^女$", message = "sexが不正です。")
    private String sex;

    @NotBlank(message = "telを入力してください。")
    @Pattern(regexp = "^1[1-9]\\d{9}$", message = "telが不正です。")
    private String tel;

    @NotBlank(message = "emailが不正です。")
    @Email(message = "emailが不正です。")
    private String email;

    @NotBlank(message = "hiredateを入力してください。")
    @Pattern(regexp = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29))$", message = "hiredateが不正です。")
    private String hiredate;

    @NotEmpty(message = "roleを入力してください。")
    private Integer[] role;

    @Min(value = 1, message = "deptIdは1以上でなければなりません。")
    private Integer deptId;

}

