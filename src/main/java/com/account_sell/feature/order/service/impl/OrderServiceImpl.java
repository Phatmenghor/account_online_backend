package com.account_sell.feature.order.service.impl;

import com.account_sell.enumation.AccountType;
import com.account_sell.enumation.OrderStatus;
import com.account_sell.exceptions.error.BadRequestException;
import com.account_sell.exceptions.error.NotFoundException;
import com.account_sell.feature.order.dto.request.CreateOrderRequest;
import com.account_sell.feature.order.dto.request.OrderFilterRequest;
import com.account_sell.feature.order.dto.request.UpdateOrderStatusRequest;
import com.account_sell.feature.order.dto.request.ValidateAccountNumberRequest;
import com.account_sell.feature.order.dto.response.OrderHistoryResponse;
import com.account_sell.feature.order.dto.response.OrderListResponse;
import com.account_sell.feature.order.dto.response.OrderResponse;
import com.account_sell.feature.order.dto.response.ValidateAccountNumberResponse;
import com.account_sell.feature.order.mapper.OrderMapper;
import com.account_sell.feature.order.models.OrderEntity;
import com.account_sell.feature.order.models.OrderHistoryEntity;
import com.account_sell.feature.order.repository.OrderHistoryRepository;
import com.account_sell.feature.order.repository.OrderRepository;
import com.account_sell.feature.order.service.BankAccountService;
import com.account_sell.feature.order.service.OrderService;
import com.account_sell.utils.PatternUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderMapper orderMapper;
    private final BankAccountService bankAccountService;

    @Override
    public ValidateAccountNumberResponse validateAccountNumber(ValidateAccountNumberRequest request) {
        log.info("Validating account number: {}", request.getAccountNumber());
        String accountNumber = request.getAccountNumber();

        // Check if account number already exists in our system with BOOKED status
        boolean isAlreadyUsed = orderRepository.existsByAccountNumberAndStatusIn(
                accountNumber, Arrays.asList(OrderStatus.BOOKED, OrderStatus.ACCEPTED));

        if (isAlreadyUsed) {
            log.warn("Account number already exists in our system with BOOKED status: {}", accountNumber);
            return ValidateAccountNumberResponse.builder()
                    .isValid(true)
                    .validBy("BOOKED")
                    .isAvailable(false)
                    .accountNumber(accountNumber)
                    .message("Account number is already booked in our system")
                    .build();
        }

        // Validate with bank's system via SOAP
        boolean existsInBankSystem = bankAccountService.validateBankAccount(accountNumber);

        // If the account doesn't exist in the bank system, it's invalid
        if (existsInBankSystem) {
            log.warn("Account number doesn't exist in bank system: {}", accountNumber);
            return ValidateAccountNumberResponse.builder()
                    .isValid(false)
                    .validBy("BANK")
                    .isAvailable(false)
                    .accountNumber(accountNumber)
                    .message("Account number is valid, exists in bank system, and is not available")
                    .build();
        }

        // Calculate price based on the pattern
        double calculatedPrice = PatternUtil.calculatePrice(accountNumber);

        return ValidateAccountNumberResponse.builder()
                .isValid(true)
                .isAvailable(true)
                .accountNumber(accountNumber)
                .price(BigDecimal.valueOf(calculatedPrice))
                .message("Account number is available")
                .build();
    }

    public ValidateAccountNumberResponse validateAccountBank(ValidateAccountNumberRequest request){

        String accountNumber = request.getAccountNumber();

        // Validate with bank's system via SOAP
        boolean existsInBankSystem = bankAccountService.validateBankAccount(accountNumber);

        // If the account doesn't exist in the bank system, it's invalid
        if (existsInBankSystem) {
            log.warn("Account number doesn't exist in bank system : {}", accountNumber);
            return ValidateAccountNumberResponse.builder()
                    .isValid(false)
                    .validBy("BANK")
                    .isAvailable(false)
                    .accountNumber(accountNumber)
                    .message("Account number is valid, exists in bank system, and is not available")
                    .build();
        }

        // Calculate price based on the pattern
        double calculatedPrice = PatternUtil.calculatePrice(accountNumber);

        return ValidateAccountNumberResponse.builder()
                .isValid(true)
                .isAvailable(true)
                .accountNumber(accountNumber)
                .price(BigDecimal.valueOf(calculatedPrice))
                .message("Account number is available")
                .build();
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for account number: {}", request.getAccountNumber());

        // Create new order
        OrderEntity order = OrderEntity.builder()
                .accountNumber(request.getAccountNumber())
                .price(request.getPrice())
                .ratePrice(request.getRatePrice())
                .customerName(request.getCustomerName())
                .phoneNumber(request.getPhoneNumber())
                .idNumber(request.getIdNumber())
                .accountType(request.getAccountType() != null ? request.getAccountType() : AccountType.NORMAL)
                .filterType(request.getFilterType())
                .status(OrderStatus.BOOKED)
                .build();

        // Save order
        OrderEntity savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());

        // Create initial history record
        OrderHistoryEntity history = OrderHistoryEntity.builder()
                .order(savedOrder)
                .oldStatus(null)
                .newStatus(OrderStatus.BOOKED)
                .remarks("Initial order creation")
                .build();

        orderHistoryRepository.save(history);
        log.info("Initial order history created for order ID: {}", savedOrder.getId());

        // Map to response
        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order with ID: {}", id);

        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", id);
                    return new NotFoundException("Order not found with ID: " + id);
                });

        log.info("Found order with ID: {}", id);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        log.info("Updating status for order ID: {} to {}", id, request.getNewStatus());

        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", id);
                    return new NotFoundException("Order not found with ID: " + id);
                });

        // Validate status transition
        if (order.getStatus() == request.getNewStatus()) {
            log.warn("Order already has status: {}", request.getNewStatus());
            throw new BadRequestException("Order already has status: " + request.getNewStatus());
        }

        // Record history before updating status
        OrderHistoryEntity history = OrderHistoryEntity.builder()
                .order(order)
                .oldStatus(order.getStatus())
                .newStatus(request.getNewStatus())
                .remarks(request.getRemarks())
                .build();

        orderHistoryRepository.save(history);
        log.info("Order history recorded for order ID: {} - Status change from {} to {}",
                id, order.getStatus(), request.getNewStatus());

        // Update order status
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getNewStatus());
        OrderEntity updatedOrder = orderRepository.save(order);

        log.info("Order status updated from {} to {} for order ID: {}",
                oldStatus, request.getNewStatus(), id);

        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    public OrderListResponse<OrderResponse> getBookedOrders(OrderFilterRequest request) {
        log.info("Fetching BOOKED orders with filter - page: {}, size: {}, search: {}",
                request.getPageNo(), request.getPageSize(), request.getSearch());

        // Create pageable with sorting (newest first)
        Pageable pageable = PageRequest.of(
                request.getPageNo(),
                request.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // Get orders with BOOKED status and search
        Page<OrderEntity> ordersPage = orderRepository.findBookedOrdersWithSearch(
                request.getStatus(),
                request.getSearch(),
                pageable);

        log.info("Found {} BOOKED orders matching criteria", ordersPage.getTotalElements());

        // Convert to response
        List<OrderResponse> orderResponses = orderMapper.toOrderResponseList(ordersPage.getContent());

        return orderMapper.toListResponse(ordersPage, orderResponses);
    }

    @Override
    public OrderListResponse<OrderHistoryResponse> getOrderHistory(OrderFilterRequest request) {
        log.info("Fetching order history with filter - page: {}, size: {}, status: {}, search: {}",
                request.getPageNo(), request.getPageSize(), request.getStatus(), request.getSearch());

        // Create pageable (history is always sorted by createdAt DESC)
        Pageable pageable = PageRequest.of(request.getPageNo(), request.getPageSize());

        Page<OrderHistoryEntity> historyPage;


        historyPage = orderHistoryRepository.searchOrderHistory(
                request.getStatus(),
                request.getSearch(),
                pageable);


        log.info("Found {} history records matching criteria", historyPage.getTotalElements());

        // Convert to response
        List<OrderHistoryResponse> historyResponses = orderMapper.toOrderHistoryResponseList(historyPage.getContent());

        return orderMapper.toListResponse(historyPage, historyResponses);
    }

    /**
     * Scheduled task to find and process orders not updated for 2 weeks
     * Runs at 01:00 AM every day
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    @Override
    public void processOldOrders() {
        log.info("Running scheduled task to process old orders");

        // Calculate cutoff date (2 weeks ago)
        LocalDateTime cutoffDate = LocalDateTime.now().minusWeeks(2);

        // Find BOOKED orders not updated for 2 weeks
        List<OrderEntity> oldOrders = orderRepository.findOldOrdersByStatus(OrderStatus.BOOKED, cutoffDate);

        log.info("Found {} BOOKED orders not updated for 2 weeks", oldOrders.size());

        // Process each old order
        for (OrderEntity order : oldOrders) {
            try {
                // Create history record for expiration
                OrderHistoryEntity history = OrderHistoryEntity.builder()
                        .order(order)
                        .oldStatus(order.getStatus())
                        .newStatus(OrderStatus.EXPIRED)
                        .remarks("Automatically expired after 2 weeks of inactivity")
                        .build();

                orderHistoryRepository.save(history);

                // Update order status to EXPIRED
                order.setStatus(OrderStatus.EXPIRED);
                orderRepository.save(order);

                log.info("Successfully expired order ID: {} for account number: {}",
                        order.getId(), order.getAccountNumber());
            } catch (Exception e) {
                log.error("Error processing old order ID: {}: {}", order.getId(), e.getMessage(), e);
            }
        }

        log.info("Completed processing of {} old orders", oldOrders.size());
    }
}