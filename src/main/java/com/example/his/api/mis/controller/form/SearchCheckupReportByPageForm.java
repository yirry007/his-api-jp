package com.example.his.api.mis.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class SearchCheckupReportByPageForm {
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,10}$", message = "nameが不正です。")
    private String name;

    @Pattern(regexp = "^1[1-9]\\d{9}$", message = "telが不正です。")
    private String tel;

    @Pattern(regexp = "^[0-9A-Za-z]{10,24}$", message = "waybillCodeが不正です。")
    private String waybillCode;

    @Range(min = 1, max = 3, message = "statusが不正です。")
    private Integer status;

    @NotNull(message = "pageを入力してください。")
    @Min(value = 1, message = "pageは1以上でなければなりません。")
    private Integer page;

    @NotNull(message = "lengthを入力してください。")
    @Range(min = 10, max = 50, message = "lengthは10から50の間でなければなりません。")
    private Integer length;
}
