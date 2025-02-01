package com.example.his.api.mis.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class SearchGoodsByPageForm {

    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]{1,50}$", message = "keywordが不正です。")
    private String keyword;

    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "codeが不正です。")
    private String code;

    @Pattern(regexp = "^父母体检$|^入职体检$|^职场白领$|^个人高端$|^中青年体检$", message = "typeが不正です。")
    private String type;

    @Range(min = 1, max = 5, message = "partIdの範囲が不正です。")
    private Byte partId;

    private Boolean status;

    @NotNull(message = "pageを入力してください。")
    @Min(value = 1, message = "pageは1以上でなければなりません。")
    private Integer page;

    @NotNull(message = "lengthを入力してください。")
    @Range(min = 10, max = 50, message = "lengthは10から50の間でなければなりません。")
    private Integer length;

}

