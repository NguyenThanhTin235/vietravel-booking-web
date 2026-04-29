package com.vietravel.booking.web.dto.tour;

import java.math.BigDecimal;

public class TourLineResponse{
    private Long id;
    private String code;
    private String name;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean isActive;
    private Integer sortOrder;

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
