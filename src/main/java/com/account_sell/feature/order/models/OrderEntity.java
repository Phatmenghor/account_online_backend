package com.account_sell.feature.order.models;

import com.account_sell.enumation.AccountType;
import com.account_sell.enumation.FilterType;
import com.account_sell.enumation.OrderStatus;
import com.account_sell.enumation.PriceRange;
import com.account_sell.feature.auth.models.BaseEntity;
import com.account_sell.feature.auth.models.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "account_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    private String ratePrice;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "id_number")
    private String idNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type")
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "filter_type")
    private FilterType filterType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @PrePersist
    @Override
    public void prePersist() {
        super.prePersist();
        if (status == null) {
            status = OrderStatus.BOOKED;
        }
    }
}