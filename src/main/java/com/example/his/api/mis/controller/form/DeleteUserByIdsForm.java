package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class DeleteUserByIdsForm {
    @NotEmpty(message = "idsを入力してください。")
    private Integer[] ids;
}

