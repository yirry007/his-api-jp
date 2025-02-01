package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class SearchAppointmentByOrderIdForm {
    @NotNull(message = "orderIdを入力してください。")
    @Min(value = 1, message = "orderIdは1以上でなければなりません。")
    private Integer orderId;
}
