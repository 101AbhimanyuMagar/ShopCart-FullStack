package com.shopcart.shopcart_backend.dto;

import java.util.Date;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountDTO {
    private double percentage;
    private Date endDate;
    private boolean active;
}