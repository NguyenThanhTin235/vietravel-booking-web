package com.vietravel.booking.web.dto.tour;

public class TourCategoryResponse{
    private Long id;
    private String name;
    private String slug;
    private Long parentId;
    private String parentName;
    private Integer sortOrder;
    private Boolean isActive;

    public Long getId(){return id;}
    public void setId(Long id){this.id=id;}
    public String getName(){return name;}
    public void setName(String name){this.name=name;}
    public String getSlug(){return slug;}
    public void setSlug(String slug){this.slug=slug;}
    public Long getParentId(){return parentId;}
    public void setParentId(Long parentId){this.parentId=parentId;}
    public String getParentName(){return parentName;}
    public void setParentName(String parentName){this.parentName=parentName;}
    public Integer getSortOrder(){return sortOrder;}
    public void setSortOrder(Integer sortOrder){this.sortOrder=sortOrder;}
    public Boolean getIsActive(){return isActive;}
    public void setIsActive(Boolean isActive){this.isActive=isActive;}
}
