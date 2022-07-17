package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
// 登录表单类
public class LoginForm {
    @NotBlank(message = "临时授权不能为空")
    private String code;
}
