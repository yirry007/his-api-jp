package com.example.his.api.front.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SearchPaymentResultForm {
    @NotBlank(message = "outTradeNoを入力してください。")
    @Pattern(regexp = "^[0-9A-Z]{32}$", message = "outTradeNoが不正です。")
    private String outTradeNo;
}
