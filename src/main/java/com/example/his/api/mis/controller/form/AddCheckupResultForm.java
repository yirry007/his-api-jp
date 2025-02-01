package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;

@Data
public class AddCheckupResultForm {
    @NotBlank(message = "nameを入力してください。")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,10}$", message = "nameが不正です。")
    private String name;

    @NotBlank(message = "uuidを入力してください。")
    @Pattern(regexp = "^[0-9A-Za-z]{32}$", message = "uuidが不正です。")
    private String uuid;

    @NotBlank(message = "placeを入力してください。")
    @Pattern(regexp = "^[0-9A-Za-z\\u4e00-\\u9fa5]{2,30}$", message = "placeが不正です。")
    private String place;

    @NotEmpty(message = "itemを入力してください。")
    private ArrayList item;

    @NotEmpty(message = "templateを入力してください。")
    private String template;
}
