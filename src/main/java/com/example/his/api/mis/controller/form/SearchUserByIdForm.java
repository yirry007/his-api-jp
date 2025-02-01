package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class SearchUserByIdForm {

    @NotNull(message = "userIdを入力してください。")
    @Min(value = 1, message = "userIdは1以上でなければなりません。")
    private Integer userId;
}

