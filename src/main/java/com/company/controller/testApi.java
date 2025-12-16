package com.company.controller;

import com.company.common.BaseResponse;
import com.company.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class testApi {
    @GetMapping("/")
    public BaseResponse<String> testOk(String name) {
        return ResultUtils.success("ok") ;
    }
}
