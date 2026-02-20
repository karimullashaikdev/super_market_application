package com.karim.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.karim.enums.OrderStatus;
import com.karim.enums.PaymentType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//
//import jakarta.persistence.CascadeType;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.OneToMany;
//import jakarta.persistence.Table;
//import lombok.Data;
//
//@Entity
//@Table(name = "orders")
//@Data
//public class Order {
//
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Long id;
//
//	private Long userId;
//
//	private Double totalAmount;
//
//	private String status; // CREATED, PAID, CANCELLED
//
//	private boolean deleted;
//
//	private LocalDateTime createdAt;
//
//	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
//	@JsonManagedReference
//	private List<OrderItem> items;
//
//}

@Entity
@Table(name = "orders")
@Data
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	private Double totalAmount;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;

	private boolean deleted;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<OrderItem> items;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.deleted = false;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
