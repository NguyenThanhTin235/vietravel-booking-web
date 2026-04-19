package com.vietravel.booking.domain.entity.tour;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="destination")
public class Destination{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false,length=150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private DestinationType type;

    @Column(nullable=false,unique=true,length=200)
    private String slug;

    @Column(name="is_active",nullable=false)
    private Boolean isActive=true;

    @Column(name="sort_order",nullable=false)
    private Integer sortOrder=0;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="category_id",nullable=false)
    private TourCategory category;
}
