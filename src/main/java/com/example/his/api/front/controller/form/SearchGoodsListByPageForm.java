package com.example.his.api.front.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class SearchGoodsListByPageForm {
    @Length(min = 1, max = 50, message = "keyword字数超出范围")
    private String keyword;

    @Pattern(regexp = "^父母体检$|^入职体检$|^职场白领$|^个人高端$|^中青年体检$",
            message = "typeが不正です。")
    private String type;

    @Pattern(regexp = "^男性$|^女性$")
    private String sex;

    @Range(min = 1, max = 4, message = "priceTypeの範囲が不正です。")
    private Integer priceType;

    @Range(min = 1, max = 4, message = "orderTypeの範囲が不正です。")
    private Integer orderType;

    @NotNull(message = "pageを入力してください。")
    @Min(value = 1, message = "pageは1以上でなければなりません。")
    private Integer page;

    @NotNull(message = "lengthを入力してください。")
    @Range(min = 10, max = 50, message = "lengthは10から50の間でなければなりません。")
    private Integer length;
}
