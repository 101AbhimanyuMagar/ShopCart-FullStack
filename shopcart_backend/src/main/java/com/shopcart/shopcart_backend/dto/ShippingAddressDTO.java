package com.shopcart.shopcart_backend.dto;


import com.shopcart.shopcart_backend.entities.ShippingAddress;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddressDTO {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    // ✅ Converter helper
    public static ShippingAddress toEmbeddable(ShippingAddressDTO dto) {
        if (dto == null) return null;
        return ShippingAddress.builder()
                .street(dto.getStreet())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry())
                .build();
    }
}
