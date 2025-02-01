package com.example.his.api.mis.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

@Data
public class UpdateDeptForm {

    @NotNull(message = "idを入力してください。")
    @Min(value = 1, message = "idは1以上でなければなりません。")
    private Integer id;

    @NotBlank(message = "deptNameを入力してください。")
    private String deptName;

    @Pattern(regexp = "^1\\d{10}$|^(0\\d{2,3}\\-){0,1}[1-9]\\d{6,7}$", message = "telが不正です。")
    private String tel;

    @Email(message = "emailが不正です。")
    private String email;

    @Length(max = 20, message = "descは20文字以内にしてください。")
    private String desc;

}

