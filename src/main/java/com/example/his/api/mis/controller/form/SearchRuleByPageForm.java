package com.example.his.api.mis.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class SearchRuleByPageForm {

    @Pattern(regexp = "^[0-9a-zA-Z\\u4e00-\\u9fa5]{1,20}$", message = "nameが不正です。")
    private String name;

    @NotNull(message = "pageを入力してください。")
    @Min(value = 1, message = "pageは1以上でなければなりません。")
    private Integer page;

    @NotNull(message = "lengthを入力してください。")
    @Range(min = 10, max = 50, message = "lengthは10から50の間でなければなりません。")
    private Integer length;
}
