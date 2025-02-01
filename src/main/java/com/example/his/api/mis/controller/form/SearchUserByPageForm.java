package com.example.his.api.mis.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class SearchUserByPageForm {
    @NotNull(message = "pageを入力してください。")
    @Min(value = 1, message = "pageは1以上でなければなりません。")
    private Integer page;

    @NotNull(message = "lengthを入力してください。")
    @Range(min = 10, max = 50, message = "lengthは10から50の間でなければなりません。")
    private Integer length;

    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,10}$", message = "nameが不正です。")
    private String name;

    @Pattern(regexp = "^男$|^女$", message = "sexが不正です。")
    private String sex;

    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]{2,10}$", message = "roleが不正です。")
    private String role;

    @Min(value = 1, message = "deptは1以上でなければなりません。")
    private Integer deptId;

    @Min(value = 1, message = "statusは1以上でなければなりません。")
    private Integer status;

}

