package com.vietravel.booking.domain.entity.tour;

import jakarta.persistence.*;

@Entity
@Table(name="transport_modes")
public class TransportMode{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="code",length=30,nullable=false,unique=true)
    private String code;

    @Column(name="name",length=100,nullable=false)
    private String name;

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

    public Boolean getIsActive(){return isActive;}
    public void setIsActive(Boolean isActive){this.isActive=isActive;}

    public Integer getSortOrder(){return sortOrder;}
    public void setSortOrder(Integer sortOrder){this.sortOrder=sortOrder;}
}
