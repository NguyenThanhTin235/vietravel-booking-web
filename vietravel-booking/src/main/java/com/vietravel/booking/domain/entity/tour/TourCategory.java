package com.vietravel.booking.domain.entity.tour;

import jakarta.persistence.*;

@Entity
@Table(name="tour_category")
public class TourCategory{
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false,length=150)
    private String name;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private TourCategory parent;

    @Column(nullable=false,unique=true,length=200)
    private String slug;

    @Column(name="sort_order",nullable=false)
    private Integer sortOrder=0;

    @Column(name="is_active",nullable=false)
    private Boolean isActive=true;

    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}
    public String getName(){return name;}
    public void setName(String name){this.name=name;}
    public TourCategory getParent(){return parent;}
    public void setParent(TourCategory parent){this.parent=parent;}
    public String getSlug(){return slug;}
    public void setSlug(String slug){this.slug=slug;}
    public Integer getSortOrder(){return sortOrder;}
    public void setSortOrder(Integer sortOrder){this.sortOrder=sortOrder;}
    public Boolean getIsActive(){return isActive;}
    public void setIsActive(Boolean isActive){this.isActive=isActive;}
}
