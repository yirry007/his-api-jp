package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SearchAppointmentByUuidForm {
    @NotBlank(message = "uuidを入力してください。")
    @Pattern(regexp = "^[0-9A-Za-z]{32}$",message = "uuidが不正です。")
    private String uuid;
}