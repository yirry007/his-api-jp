package com.example.his.api.mis.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

@Data
public class UpdateRoleForm {

    @NotNull(message = "idを入力してください。")
    @Min(value = 1, message = "idは1以上でなければなりません。")
    private Integer id;

    @NotBlank(message = "roleNameを入力してください。")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]{2,10}", message = "roleNameが不正です。")
    private String roleName;

    @NotEmpty(message = "permissionsを入力してください。")
    private Integer[] permissions;

    @Length(max = 20, message = "descは20文字以内にしてください。")
    private String desc;

    @NotNull(message = "changedを入力してください。")
    private Boolean changed;
}

