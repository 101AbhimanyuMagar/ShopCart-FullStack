package com.shopcart.shopcart_backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    private double price;
    private int stock;
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "added_by_admin_id")
    private User addedBy; // Should be an ADMIN user
}
