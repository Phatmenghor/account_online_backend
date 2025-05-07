package com.account_sell.feature.order.repository;

import com.account_sell.enumation.OrderStatus;
import com.account_sell.feature.order.models.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    boolean existsByAccountNumberAndStatusIn(String accountNumber, List<OrderStatus> statuses);

    @Query("SELECT o FROM OrderEntity o WHERE " +
            "(:status IS NULL AND o.status IN ('BOOKED', 'ACCEPTED') OR o.status = :status) AND " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(o.accountNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(o.phoneNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<OrderEntity> findBookedOrdersWithSearch(
            @Param("status") OrderStatus status,
            @Param("search") String search,
            Pageable pageable);


    // Find orders not updated for two weeks with BOOKED status
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status AND o.updatedAt < :cutoffDate")
    List<OrderEntity> findOldOrdersByStatus(
            @Param("status") OrderStatus status,
            @Param("cutoffDate") LocalDateTime cutoffDate);
}