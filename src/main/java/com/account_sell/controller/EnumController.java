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

    @PostMapping("/account-type")
    public ApiResponse<List<AccountType>> getAllAccountTypes() {
        return new ApiResponse<>(
                "Success",
                "Get all enum account type successfully...!",
                Arrays.asList(AccountType.values())
        );
    }

    @PostMapping("/filter-type")
    public ApiResponse<List<FilterType>> getAllFilterTypes() {
        return new ApiResponse<>(
                "Success",
                "Get all enum filter type successfully...!",
                Arrays.asList(FilterType.values())
        );
    }

    @PostMapping("/order-status")
    public ApiResponse<List<OrderStatus>> getAllOrderStatuses() {
        return new ApiResponse<>(
                "Success",
                "Get all enum order status successfully...!",
                Arrays.asList(OrderStatus.values())
        );
    }

    @PostMapping("/price-range")
    public ApiResponse<List<PriceRange>> getAllPriceRanges() {
        return new ApiResponse<>(
                "Success",
                "Get all enum price range successfully...!",
                Arrays.asList(PriceRange.values())
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
}
