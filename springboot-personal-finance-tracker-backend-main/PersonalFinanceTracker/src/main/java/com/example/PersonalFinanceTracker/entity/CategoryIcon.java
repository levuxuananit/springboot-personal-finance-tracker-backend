package com.example.PersonalFinanceTracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "category_icons")
@Getter
@Setter
public class CategoryIcon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", unique = true)
    private String categoryName;

    private String emoji;

    @Column(name = "icon_url")
    private String iconUrl;
}
