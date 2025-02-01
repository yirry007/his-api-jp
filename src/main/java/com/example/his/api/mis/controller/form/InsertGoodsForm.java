package com.example.his.api.mis.controller.form;

import com.example.his.api.mis.controller.form.vo.CheckupVo;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.ArrayList;

@Data
public class InsertGoodsForm {
    @NotBlank(message = "codeを入力してください。")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,20}$", message = "codeが不正です。")
    private String code;

    @NotBlank(message = "titleを入力してください。")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]{2,50}$", message = "titleが不正です。")
    private String title;

    @NotBlank(message = "descriptionを入力してください。")
    @Length(max = 200, message = "descriptionは200文字以内にしてください。")
    private String description;

    @Valid
    private ArrayList<CheckupVo> checkup_1;

    @Valid
    private ArrayList<CheckupVo> checkup_2;

    @Valid
    private ArrayList<CheckupVo> checkup_3;

    @Valid
    private ArrayList<CheckupVo> checkup_4;

    @NotBlank(message = "imageを入力してください。")
    @Pattern(regexp = "^[0-9a-zA-Z/\\.]{1,200}$", message = "imageが不正です。")
    private String image;

    @NotNull(message = "initialPriceを入力してください。")
    @Min(value = 0, message = "initialPrice不能小于0")
    private BigDecimal initialPrice;

    @NotNull(message = "currentPriceを入力してください。")
    @Min(value = 0, message = "currentPrice不能小于0")
    private BigDecimal currentPrice;

    @NotBlank(message = "typeを入力してください。")
    @Pattern(regexp = "^父母体检$|^入职体检$|^职场白领$|^个人高端$|^中青年体检$")
    private String type;

    private String[] tag;

    @Range(min = 1, max = 5, message = "partIdの範囲が不正です。")
    private Integer partId;

    @Min(value = 1, message = "ruleIdは1以上でなければなりません。")
    private Integer ruleId;

}

