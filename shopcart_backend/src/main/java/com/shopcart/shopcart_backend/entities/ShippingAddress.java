package com.shopcart.shopcart_backend.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

// For storing in database with @Embedded in Order
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddress {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
