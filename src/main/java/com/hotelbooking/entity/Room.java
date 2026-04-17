package com.hotelbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String photoPath;  // путь к файлу изображения

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<BookingRequest> requests = new ArrayList<>();

    // Методы изменения цены и активности
    public void changePrice(BigDecimal newPrice) {
        this.price = newPrice;
    }

    public void changeActiveStatus(boolean status) {
        this.isActive = status;
    }
}