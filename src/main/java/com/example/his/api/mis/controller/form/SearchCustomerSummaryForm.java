package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class SearchCustomerSummaryForm {
    @NotNull(message = "customerIdを入力してください。")
    @Min(value = 1, message = "customerIdは1以上でなければなりません。")
    private Integer customerId;
}
