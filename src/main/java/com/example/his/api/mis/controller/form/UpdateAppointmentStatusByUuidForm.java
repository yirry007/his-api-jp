package com.example.his.api.mis.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class UpdateAppointmentStatusByUuidForm {
    @NotBlank(message = "uuidを入力してください。")
    @Pattern(regexp = "^[0-9a-zA-Z]{32}$", message = "uuidが不正です。")
    private String uuid;

    @NotNull(message = "statusを入力してください。")
    @Range(min = 1, max = 4, message = "statusが不正です。")
    private Integer status;
}
