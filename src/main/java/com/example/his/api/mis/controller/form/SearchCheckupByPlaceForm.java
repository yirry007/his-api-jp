package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SearchCheckupByPlaceForm {
    @NotBlank(message = "uuidを入力してください。")
    @Pattern(regexp = "^[0-9a-zA-Z]{32}$", message = "uuidが不正です。")
    private String uuid;

    @NotBlank(message = "placeを入力してください。")
    @Pattern(regexp = "^[0-9a-zA-Z\\u4e00-\\u9fa5]{2,30}$", message = "placeが不正です。")
    private String place;
}
