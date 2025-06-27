package com.account_sell.controller;

import com.account_sell.enumation.*;
import com.account_sell.exceptions.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/enum")
public class EnumController {

    @PostMapping("/action_user")
    public ApiResponse<List<ActionEnum>> getAllActionUser() {
        return new ApiResponse<>(
                "Success",
                "Get all enum action user successfully...!",
                Arrays.asList(ActionEnum.values())
        );
    }

    @PostMapping("/role")
    public ApiResponse<List<RoleEnum>> getAllRoles() {
        return new ApiResponse<>(
                "Success",
                "Get all enum roles successfully...!",
                Arrays.asList(RoleEnum.values())
        );
    }

    @PostMapping("/status")
    public ApiResponse<List<StatusData>> getAllStatuses() {
        return new ApiResponse<>(
                "Success",
                "Get all enum status data successfully...!",
                Arrays.asList(StatusData.values())
        );
    }

    @PostMapping("/gender")
    public ApiResponse<List<GenderEnum>> getAllGender() {
        return new ApiResponse<>(
                "Success",
                "Get all enum gender data successfully...!",
                Arrays.asList(GenderEnum.values())
        );
    }
}
