package com.example.his.api.mis.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class UpdateFlowRegulationForm {
    @NotNull(message = "idを入力してください。")
    @Min(value = 1, message = "idは1以上でなければなりません。")
    private Integer id;

    @NotBlank(message = "placeを入力してください。")
    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5\\(\\)]{2,40}$", message = "placeが不正です。")
    private String place;

    @NotBlank(message = "blueUuidを入力してください。")
    @Pattern(regexp = "^[a-zA-Z0-9]{32}$", message = "blueUuidが不正です。")
    private String blueUuid;

    @NotNull(message = "maxNumを入力してください。")
    @Range(min = 1, max = 1000, message = "maxNumが不正です。")
    private Integer maxNum;

    @NotNull(message = "weightを入力してください。")
    @Range(min = 1, max = 10, message = "weightが不正です。")
    private Integer weight;

    @NotNull(message = "priorityを入力してください。")
    @Range(min = 1, max = 10, message = "priorityが不正です。")
    private Integer priority;
}
