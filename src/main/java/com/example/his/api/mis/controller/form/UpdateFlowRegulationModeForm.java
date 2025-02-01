package com.example.his.api.mis.controller.form;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateFlowRegulationModeForm {
    @NotNull(message = "modeを入力してください。")
    private Boolean mode;
}
