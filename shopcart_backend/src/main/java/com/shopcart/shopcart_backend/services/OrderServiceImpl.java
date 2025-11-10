package com.shopcart.shopcart_backend.services;

import com.shopcart.shopcart_backend.dto.OrderRequestDTO;
import com.shopcart.shopcart_backend.dto.OrderResponseDTO;
import com.shopcart.shopcart_backend.dto.ShippingAddressDTO;
import com.shopcart.shopcart_backend.entities.*;
import com.shopcart.shopcart_backend.exception.BadRequestException;
import com.shopcart.shopcart_backend.exception.ResourceNotFoundException;
import com.shopcart.shopcart_backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderStatusHistoryRepository historyRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    // ✅ Place new order
    @Override
    public OrderResponseDTO placeOrder(String email, OrderRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        double totalAmount = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int qty = cartItem.getQuantity();

            if (product.getStock() < qty) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - qty);
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(qty)
                    .price(product.getPrice())
                    .total(qty * product.getPrice()) // ✅ calculate total
                    .build();

            orderItems.add(orderItem);
            totalAmount += qty * product.getPrice();
        }

        // ✅ Shipping address conversion
        ShippingAddressDTO dto = request.getShippingAddress();
        ShippingAddress address = ShippingAddress.builder()
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry())
                .build();

        Order order = Order.builder()
                .user(user)
                .orderItems(orderItems)
                .totalAmount(totalAmount)
                .status(OrderStatus.PLACED)
                .shippingAddress(address)
                .build();

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }

        order = orderRepository.save(order);
        addOrderStatusHistory(order, OrderStatus.PLACED);
        cartItemRepository.deleteAll(cartItems); // clear cart

        return OrderResponseDTO.from(order);
    }

    // ✅ Orders by logged-in user
    @Override
    public List<OrderResponseDTO> getOrdersByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return orderRepository.findByUser(user).stream()
                .map(OrderResponseDTO::from)
                .collect(Collectors.toList());
    }

    // ✅ Order by ID
    @Override
    public OrderResponseDTO getOrderById(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You are not authorized to view this order.");
        }

        return OrderResponseDTO.from(order);
    }

    // ✅ All orders (for super admin)
    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponseDTO::from)
                .collect(Collectors.toList());
    }

    // ✅ Update order status
    @Override
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setStatus(status);
        orderRepository.save(order);
        addOrderStatusHistory(order, status);
    }

   

    public void updateOrderItemStatus(Long itemId, OrderStatus status) {
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        item.setStatus(status);
        orderItemRepository.save(item);

        Order order = item.getOrder();
        boolean allDelivered = order.getOrderItems().stream()
                .allMatch(i -> i.getStatus() == OrderStatus.DELIVERED);

        if (allDelivered) {
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }
    }

    // ✅ Order status history helper
    private void addOrderStatusHistory(Order order, OrderStatus status) {
    if (order.getStatusHistory() == null) {
        order.setStatusHistory(new ArrayList<>());
    }
    OrderStatusHistory history = OrderStatusHistory.builder()
            .order(order)
            .status(status)
            .changedAt(new Date())
            .build();

    order.getStatusHistory().add(history);
    historyRepository.save(history);
}


    // ✅ Orders only for logged-in admin (using ProductRepository)
@Override
public List<OrderResponseDTO> getOrdersForAdmin(Long adminId) {
    // Fetch all orders (can optimize later)
    List<Order> orders = orderRepository.findAll();

    List<Order> filteredOrders = new ArrayList<>();

    for (Order order : orders) {
        // Keep only items that belong to this admin
        List<OrderItem> adminItems = order.getOrderItems().stream()
                .filter(item -> item.getProduct() != null &&
                        item.getProduct().getAddedBy() != null &&
                        item.getProduct().getAddedBy().getId().equals(adminId))
                .collect(Collectors.toList());

        if (!adminItems.isEmpty()) {
            Order partialOrder = new Order();
            partialOrder.setId(order.getId());
            partialOrder.setUser(order.getUser());
            partialOrder.setOrderItems(adminItems);
            partialOrder.setTotalAmount(
                    adminItems.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum());
            partialOrder.setStatus(order.getStatus());
            partialOrder.setCreatedAt(order.getCreatedAt());
            partialOrder.setShippingAddress(order.getShippingAddress());

            filteredOrders.add(partialOrder);
        }
    }

    return filteredOrders.stream()
            .map(OrderResponseDTO::from)
            .collect(Collectors.toList());
}




}
