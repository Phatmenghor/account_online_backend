package com.account_sell.feature.order.controller;

import com.account_sell.exceptions.response.ApiResponse;
import com.account_sell.feature.order.dto.request.CreateOrderRequest;
import com.account_sell.feature.order.dto.request.ValidateAccountNumberRequest;
import com.account_sell.feature.order.dto.response.OrderResponse;
import com.account_sell.feature.order.dto.response.ValidateAccountNumberResponse;
import com.account_sell.feature.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user/orders")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserOrderController {

    private final OrderService orderService;

    @PostMapping("/validate/account-number")
    public ApiResponse<ValidateAccountNumberResponse> validateAccountNumber(
            @RequestBody @Valid ValidateAccountNumberRequest request) {

        log.info("Received request to validate account number: {}", request.getAccountNumber());

        ValidateAccountNumberResponse response = orderService.validateAccountNumber(request);

        log.info("Validation completed: {} - {}", request.getAccountNumber(), response.getMessage());

        return new ApiResponse<>(
                response.isValid() && response.isAvailable() ? "success" : "warning",
                response.getMessage(),
                response
        );
    }

    @PostMapping("/validate/account-bank")
    public ApiResponse<ValidateAccountNumberResponse> validateAccountBank(
            @RequestBody @Valid ValidateAccountNumberRequest request){

        log.info("Received request to validate account bank: {}", request.getAccountNumber());

        ValidateAccountNumberResponse response = orderService.validateAccountBank(request);

        log.info("Validation completed : {} - {}", request.getAccountNumber(), response.getMessage());

        return new ApiResponse<>(
                response.isValid() && response.isAvailable() ? "success" : "warning",
                response.getMessage(),
                response
        );
    }

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest request) {

        log.info("Received request to create order for account number: {}", request.getAccountNumber());

        OrderResponse response = orderService.createOrder(request);

        log.info("Order created successfully with ID: {}", response.getId());

        return new ApiResponse<>(
                "success",
                "Order created successfully",
                response
        );
    }
}
