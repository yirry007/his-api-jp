package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class DownloadCheckupExcelForm {
    @NotNull(message = "idを入力してください。")
    @Min(value = 1, message = "idは1以上でなければなりません。")
    private Integer id;
}

