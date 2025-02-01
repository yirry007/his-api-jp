package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class CheckPaymentResultForm {
    @NotEmpty(message = "outTradeNoArrayを入力してください。")
    private String[] outTradeNoArray;
}
