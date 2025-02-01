package com.example.his.api.mis.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class InsertDeptForm {

    @NotBlank(message = "deptNameを入力してください。")
    private String deptName;

    @Pattern(regexp = "^1[1-9]\\d{9}$|^(0\\d{2,3}\\-){0,1}[1-9]\\d{6,7}$",message = "telが不正です。")
    private String tel;

    @Email(message = "emailが不正です。")
    private String email;

    @Length(max = 20,message = "descは20文字以内にしてください。")
    private String desc;
}

