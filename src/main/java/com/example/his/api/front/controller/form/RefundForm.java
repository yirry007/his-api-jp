package com.example.his.api.front.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class RefundForm {
    private Integer customerId;
    @NotNull
    @Min(value = 1, message = "idは1以上でなければなりません。")
    private Integer id;
}
