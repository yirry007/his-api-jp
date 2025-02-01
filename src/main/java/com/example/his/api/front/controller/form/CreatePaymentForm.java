package com.example.his.api.front.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class CreatePaymentForm {
    @NotNull(message = "goodsIdを入力してください。")
    @Min(value = 1, message = "goodsIdは1以上でなければなりません。")
    private Integer goodsId;

    @NotNull(message = "numberを入力してください。")
    @Min(value = 1, message = "numberは1以上でなければなりません。")
    private Integer number;
}
