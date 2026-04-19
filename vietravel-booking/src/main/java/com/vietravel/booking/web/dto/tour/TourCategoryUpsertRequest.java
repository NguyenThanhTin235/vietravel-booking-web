package com.vietravel.booking.web.dto.tour;

public class TourCategoryUpsertRequest{
    private String name;
    private String slug;
    private Long parentId;
    private Integer sortOrder;
    private Boolean isActive;

    public String getName(){return name;}
    public void setName(String name){this.name=name;}
    public String getSlug(){return slug;}
    public void setSlug(String slug){this.slug=slug;}
    public Long getParentId(){return parentId;}
    public void setParentId(Long parentId){this.parentId=parentId;}
    public Integer getSortOrder(){return sortOrder;}
    public void setSortOrder(Integer sortOrder){this.sortOrder=sortOrder;}
    public Boolean getIsActive(){return isActive;}
    public void setIsActive(Boolean isActive){this.isActive=isActive;}
}
