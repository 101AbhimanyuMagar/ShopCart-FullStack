package com.shopcart.shopcart_backend.dto;


import lombok.*;

@Getter
@Setter

public class ShippingAddressDTO {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}


