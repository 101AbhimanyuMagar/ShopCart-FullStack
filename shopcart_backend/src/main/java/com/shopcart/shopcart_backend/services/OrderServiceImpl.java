package com.shopcart.shopcart_backend.services;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.shopcart.shopcart_backend.dto.OrderRequestDTO;
import com.shopcart.shopcart_backend.dto.OrderResponseDTO;
import com.shopcart.shopcart_backend.dto.ShippingAddressDTO;
import com.shopcart.shopcart_backend.entities.*;
import com.shopcart.shopcart_backend.exception.BadRequestException;
import com.shopcart.shopcart_backend.exception.ResourceNotFoundException;
import com.shopcart.shopcart_backend.repositories.*;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;


import java.io.ByteArrayOutputStream;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

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
    @Autowired
    private ProductService productService;

    // ✅ Place new order
    @Transactional
    @Override
    public OrderResponseDTO placeOrder(String email, OrderRequestDTO request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // 🛒 Step 1: Fetch cart items
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // 💳 Step 2: Simulate payment
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String transactionId = "TXN-" + System.currentTimeMillis();

        // 🧮 Step 3: Convert CartItems → OrderItems
        double totalAmount = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int qty = cartItem.getQuantity();

            // Stock check
            if (product.getStock() < qty) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }

            // Update stock
            product.setStock(product.getStock() - qty);
            productRepository.save(product);

            // Create OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(qty)
                    .price(product.getPrice())
                    .total(product.getPrice() * qty)
                    .build();

            orderItems.add(orderItem);
            totalAmount += qty * product.getPrice();
        }

        // 📦 Step 4: Build Order entity
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.PLACED);
        order.setTransactionId(transactionId);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus("SUCCESS");
        order.setTotalAmount(totalAmount);
        order.setShippingAddress(ShippingAddressDTO.toEmbeddable(request.getShippingAddress()));

        // Attach items
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setOrderItems(orderItems);

        // 💾 Step 5: Save order
        orderRepository.save(order);

        // 🧹 Step 6: Clear cart
        cartItemRepository.deleteAll(cartItems);

        // 🎯 Step 7: Return response
        return OrderResponseDTO.from(order);
    }

    // ✅ Get all orders for a user
    @Override
    public List<OrderResponseDTO> getOrdersByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return orderRepository.findByUser(user).stream()
                .map(OrderResponseDTO::from)
                .collect(Collectors.toList());
    }

    // ✅ Get specific order by ID
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

    // ✅ Get all orders (for super admin)
    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponseDTO::from)
                .collect(Collectors.toList());
    }

@Transactional
@Override
public void updateOrderStatus(Long orderId, OrderStatus status) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

    // Update the order status
    order.setStatus(status);
    orderRepository.save(order);

    // Update all order items for this order
    List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
    for (OrderItem item : items) {
        item.setStatus(status);
    }
    orderItemRepository.saveAll(items);

    // Add history for order status
    addOrderStatusHistory(order, status);

    logger.info("Order ID {} and its items status updated to {}", orderId, status);
}


    // ✅ Update status for a specific order item
    @Transactional
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

        addOrderStatusHistory(order, status);
    }

    // ✅ Cancel individual order item
@Transactional
public void cancelOrderItem(String email, Long orderId, Long orderItemId) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    if (!order.getUser().getEmail().equals(email)) {
        throw new BadRequestException("You are not authorized to cancel this order.");
    }

    OrderItem orderItem = order.getOrderItems().stream()
            .filter(item -> item.getId().equals(orderItemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

    if (isNonCancelable(orderItem.getStatus())) {
        throw new BadRequestException("This item cannot be cancelled after it is shipped or delivered.");
    }

    // ✅ Restore stock for cancelled item
    Product product = orderItem.getProduct();
    int restoreQty = orderItem.getQuantity();
    product.setStock(product.getStock() + restoreQty);
    productRepository.save(product);

    // ✅ Update order item status
    orderItem.setStatus(OrderStatus.CANCELLED);

    // ✅ Recalculate total (excluding cancelled items)
    double newTotal = order.getOrderItems().stream()
            .filter(item -> item.getStatus() != OrderStatus.CANCELLED)
            .mapToDouble(OrderItem::getTotal)
            .sum();

    order.setTotalAmount(newTotal);

    // ✅ If all items are cancelled → cancel full order
    boolean allCancelled = order.getOrderItems().stream()
            .allMatch(item -> item.getStatus() == OrderStatus.CANCELLED);

    if (allCancelled) {
        order.setStatus(OrderStatus.CANCELLED);
    }

    orderRepository.save(order);
    addOrderStatusHistory(order, OrderStatus.CANCELLED);

    logger.info("Order item {} cancelled for order {} and stock restored ({} pcs).", 
                orderItemId, orderId, restoreQty);
}


    // ✅ Orders visible to a specific admin
    @Override
    public List<OrderResponseDTO> getOrdersForAdmin(Long adminId) {
        List<Order> orders = orderRepository.findAll(); // could be optimized via query
        List<Order> filteredOrders = new ArrayList<>();

        for (Order order : orders) {
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

    // ✅ Helper to map shipping address
    private ShippingAddress mapShippingAddress(ShippingAddressDTO dto) {
        if (dto == null)
            throw new BadRequestException("Shipping address is required.");

        return ShippingAddress.builder()
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry())
                .build();
    }

    // ✅ Helper to add order status history
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

    // ✅ Helper to check non-cancellable statuses
    private boolean isNonCancelable(OrderStatus status) {
        return EnumSet.of(OrderStatus.SHIPPED, OrderStatus.DELIVERED).contains(status);
    }

  @Override 
public byte[] generateInvoicePdf(Long orderId) {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        // ✅ Logo or header
        document.add(new Paragraph("🛍️ ShopCart Invoice", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD)));
        document.add(new Paragraph("Order ID: " + order.getId()));
        document.add(new Paragraph("Order Date: " + order.getCreatedAt()));
        document.add(new Paragraph("Customer: " + order.getUser().getName()));
        document.add(new Paragraph("Email: " + order.getUser().getEmail()));
        document.add(new Paragraph(" ")); // spacing

        // ✅ Table of items
        PdfPTable table = new PdfPTable(4);
        table.addCell("Product");
        table.addCell("Price (₹)");
        table.addCell("Qty");
        table.addCell("Total (₹)");

        for (OrderItem item : order.getOrderItems()) {
            table.addCell(item.getProduct().getName());
            table.addCell(String.valueOf(item.getPrice()));
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(String.valueOf(item.getTotal()));
        }

        document.add(table);
        document.add(new Paragraph(" "));

        // ✅ Total & payment details
        document.add(new Paragraph("Total Amount: ₹" + order.getTotalAmount(), new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD)));
        document.add(new Paragraph("Payment Method: " + order.getPaymentMethod()));
        document.add(new Paragraph("Transaction ID: " + order.getTransactionId()));
        document.add(new Paragraph("Status: " + order.getStatus()));

        document.close();
        return out.toByteArray();
    } catch (Exception e) {
        throw new RuntimeException("Failed to generate invoice PDF", e);
    }
}

}
