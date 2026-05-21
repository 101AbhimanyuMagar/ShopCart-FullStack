package com.shopcart.shopcart_backend.dto;



import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {
    private ShippingAddressDTO shippingAddress;
    private String paymentMethod;
    private double totalAmount;
}


