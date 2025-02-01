package com.example.his.api.front.controller.form;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class SearchIndexGoodsByPartForm {
    @NotEmpty(message = "partIdsを入力してください。")
    private Integer[] partIds;
}
