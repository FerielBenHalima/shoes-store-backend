package com.shoes_store.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shoes_store.backend.dto.request.OrderItemRequest;
import com.shoes_store.backend.dto.request.OrderRequest;
import com.shoes_store.backend.dto.response.DashboardResponse;
import com.shoes_store.backend.dto.response.OrderItemResponse;
import com.shoes_store.backend.dto.response.OrderResponse;
import com.shoes_store.backend.models.Order;
import com.shoes_store.backend.models.OrderItem;
import com.shoes_store.backend.models.Variant;
import com.shoes_store.backend.repositories.OrderRepository;
import com.shoes_store.backend.repositories.VariantRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final VariantRepository   variantRepository;


    // ── Create order ──────────────────────────────────────
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // 1 — Check stock and deduct for each item
        for (OrderItemRequest item : request.getItems()) {
            Variant variant = variantRepository.findById(item.getVariantId())
                .orElseThrow(() -> new RuntimeException(
                    "Variant not found: " + item.getVariantId()));

            if (variant.getStock() < item.getQuantity()) {
                throw new RuntimeException(
                    "Insufficient stock for: " + variant.getSku() +
                    " — available: " + variant.getStock() +
                    ", requested: " + item.getQuantity());
            }

            // Deduct stock
            variant.setStock(variant.getStock() - item.getQuantity());
            variantRepository.save(variant);
        }

        // 2 — Build order number
        String orderNumber = "MNS-" + String.valueOf(System.currentTimeMillis()).substring(7);

        // 3 — Build order
        Order order = Order.builder()
            .orderNumber(orderNumber)
            .fullName(request.getFullName())
            .phone(request.getPhone())
            .city(request.getCity())
            .address(request.getAddress())
            .notes(request.getNotes())
            .total(request.getTotal())
            .status(Order.OrderStatus.EN_ATTENTE)
            .build();

        // 4 — Build items
        List<OrderItem> items = request.getItems().stream()
            .map(i -> OrderItem.builder()
                .order(order)
                .productId(i.getProductId())
                .variantId(i.getVariantId())
                .productName(i.getProductName())
                .size(i.getSize())
                .color(i.getColor())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .build())
            .collect(Collectors.toList());

        order.setItems(items);
        return toResponse(orderRepository.save(order));
    }
    
    // ── Get all orders ────────────────────────────────────
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── Get order by id ───────────────────────────────────
    public OrderResponse getById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Commande introuvable"));
        return toResponse(order);
    }

    // ── Update status ─────────────────────────────────────
    @Transactional
    public OrderResponse updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Commande introuvable"));
        
        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        Order.OrderStatus oldStatus = order.getStatus();

        // If cancelling — restore stock for all items
        if (newStatus == Order.OrderStatus.ANNULE
                && oldStatus != Order.OrderStatus.ANNULE) {
            for (OrderItem item : order.getItems()) {
                variantRepository.findById(item.getVariantId()).ifPresent(variant -> {
                    variant.setStock(variant.getStock() + item.getQuantity());
                    variantRepository.save(variant);
                });
            }
        }

        // If reactivating a cancelled order — deduct stock again
        if (oldStatus == Order.OrderStatus.ANNULE
                && newStatus != Order.OrderStatus.ANNULE) {
            for (OrderItem item : order.getItems()) {
                Variant variant = variantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new RuntimeException(
                        "Variante introuvable: " + item.getVariantId()));

                if (variant.getStock() < item.getQuantity()) {
                    throw new RuntimeException(
                        "Stock insuffisant: " + variant.getSku());
                }

                variant.setStock(variant.getStock() - item.getQuantity());
                variantRepository.save(variant);
            }
        }

        order.setStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }


    // ── Dashboard stats ───────────────────────────────────
    public DashboardResponse getDashboard(long totalProducts) {
        List<Order> orders = orderRepository.findAll();

        long totalRevenue = orders.stream()
            .filter(o -> o.getStatus() != Order.OrderStatus.ANNULE)
            .mapToLong(Order::getTotal)
            .sum();

        long pendingOrders = orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.EN_ATTENTE)
            .count();

        List<OrderResponse> recentOrders = orderRepository
            .findAllByOrderByCreatedAtDesc()
            .stream()
            .limit(7)
            .map(this::toResponse)
            .collect(Collectors.toList());

        return DashboardResponse.builder()
            .totalRevenue(totalRevenue)
            .totalOrders((long) orders.size())
            .pendingOrders(pendingOrders)
            .totalProducts(totalProducts)
            .recentOrders(recentOrders)
            .build();
    }

    // ── Map to response ───────────────────────────────────
    private OrderResponse toResponse(Order o) {
        return OrderResponse.builder()
            .id(o.getId())
            .orderNumber(o.getOrderNumber())
            .fullName(o.getFullName())
            .phone(o.getPhone())
            .city(o.getCity())
            .address(o.getAddress())
            .notes(o.getNotes())
            .total(o.getTotal())
            .status(o.getStatus().name())
            .createdAt(o.getCreatedAt())
            .items(o.getItems() == null ? List.of() :
                o.getItems().stream()
                    .map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .variantId(i.getVariantId())
                        .productName(i.getProductName())
                        .size(i.getSize())
                        .color(i.getColor())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                    .collect(Collectors.toList()))
            .build();
    }
}
