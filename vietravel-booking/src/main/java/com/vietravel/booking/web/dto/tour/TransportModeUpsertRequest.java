package com.vietravel.booking.web.dto.tour;

public class TransportModeUpsertRequest{
    private String code;
    private String name;
    private Boolean isActive;
    private Integer sortOrder;

    public String getCode(){return code;}
    public void setCode(String code){this.code=code;}
    public String getName(){return name;}
    public void setName(String name){this.name=name;}
    public Boolean getIsActive(){return isActive;}
    public void setIsActive(Boolean isActive){this.isActive=isActive;}
    public Integer getSortOrder(){return sortOrder;}
    public void setSortOrder(Integer sortOrder){this.sortOrder=sortOrder;}
}
