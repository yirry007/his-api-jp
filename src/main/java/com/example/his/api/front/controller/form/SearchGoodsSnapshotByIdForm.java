package com.example.his.api.front.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SearchGoodsSnapshotByIdForm {
    @NotBlank(message = "snapshotIdを入力してください。")
    @Pattern(regexp = "^[0-9a-z]{24}$", message = "snapshotIdが不正です。")
    private String snapshotId;
}
