package com.example.his.api.mis.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
public class InsertRoleForm {

    @NotBlank(message = "roleNameを入力してください。")
    private String roleName;

    @NotEmpty(message = "permissionsを入力してください。")
    private Integer[] permissions;


    @Length(max = 20,message = "descは20文字以内にしてください。")
    private String desc;
}

