package com.karim.dto;

import java.time.LocalDateTime;
import com.karim.enums.PaymentType;
import com.karim.enums.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderItemDetailsDTO {

    private Long orderId;
    private Long userId;
    private String paymentType;
    private String status;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private Long orderItemId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;

    public OrderItemDetailsDTO(Long orderId, Long userId,
                               PaymentType paymentType, OrderStatus status,
                               Double totalAmount, LocalDateTime createdAt,
                               Long orderItemId, Long productId,
                               String productName, Integer quantity, Double price) {
        this.orderId     = orderId;
        this.userId      = userId;
        this.paymentType = paymentType != null ? paymentType.name() : null;
        this.status      = status      != null ? status.name()      : null;
        this.totalAmount = totalAmount;
        this.createdAt   = createdAt;
        this.orderItemId = orderItemId;
        this.productId   = productId;
        this.productName = productName;
        this.quantity    = quantity;
        this.price       = price;
    }
}