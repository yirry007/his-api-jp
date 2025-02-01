package com.example.his.api.front.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class CloseOrderByIdForm {
    @NotNull(message = "IDを入力してください。")
    @Min(value = 1, message = "IDは1以上でなければなりません。")
    private Integer id;

    private Integer customerId;
}
