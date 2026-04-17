package com.vietravel.booking.domain.entity.tour;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="tour_lines")
public class TourLine{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false,unique=true,length=30)
    private String code;

    @Column(nullable=false,length=100)
    private String name;

    @Column(name="min_price",nullable=false,precision=12,scale=2)
    private BigDecimal minPrice;

    @Column(name="max_price",nullable=false,precision=12,scale=2)
    private BigDecimal maxPrice;

    @Column(name="is_active",nullable=false)
    private Boolean isActive=true;

    @Column(name="sort_order",nullable=false)
    private Integer sortOrder=0;

    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}
    public String getCode(){return code;}
    public void setCode(String code){this.code=code;}
    public String getName(){return name;}
    public void setName(String name){this.name=name;}
    public BigDecimal getMinPrice(){return minPrice;}
    public void setMinPrice(BigDecimal minPrice){this.minPrice=minPrice;}
    public BigDecimal getMaxPrice(){return maxPrice;}
    public void setMaxPrice(BigDecimal maxPrice){this.maxPrice=maxPrice;}
    public Boolean getIsActive(){return isActive;}
    public void setIsActive(Boolean isActive){this.isActive=isActive;}
    public Integer getSortOrder(){return sortOrder;}
    public void setSortOrder(Integer sortOrder){this.sortOrder=sortOrder;}
}
