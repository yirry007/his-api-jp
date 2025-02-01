package com.example.his.api.front.controller.form;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class InsertAppointmentForm {
    @NotNull(message = "orderIdを入力してください。")
    @Min(value = 1, message = "orderIdは1以上でなければなりません。")
    private Integer orderId;

    @NotBlank(message = "dateを入力してください。")
    @Pattern(regexp = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29))$", message = "dateが不正です。")
    private String date;

    @NotBlank(message = "nameを入力してください。")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,10}$", message = "nameが不正です。")
    private String name;

    @NotBlank(message = "pidを入力してください。")
    @Pattern(regexp = "^[0-9Xx]{18}$", message = "pidが不正です。")
    private String pid;

    @NotBlank(message = "telを入力してください。")
    @Pattern(regexp = "^1[1-9]\\d{9}$", message = "telが不正です。")
    private String tel;

    @NotBlank(message = "mailingAddressを入力してください。")
    @Pattern(regexp = "^[0-9A-Za-z\\u4e00-\\u9fa5\\-_#]{10,100}", message = "mailingAddressが不正です。")
    private String mailingAddress;

    @Pattern(regexp = "^[0-9A-Za-z\\u4e00-\\u9fa5\\-_#]{2,100}", message = "companyが不正です。")
    private String company;
}
