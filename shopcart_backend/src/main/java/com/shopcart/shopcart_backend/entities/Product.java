package com.shopcart.shopcart_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

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

    private double price;  // ✅ This field gives you getPrice() automatically
    private int stock;
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "added_by_admin_id")
    private User addedBy;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Discount> discounts = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}

