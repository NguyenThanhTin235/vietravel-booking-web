package com.vietravel.booking.web.dto.tour;

public class TourImageDto {
     private String url;
     private Boolean isThumbnail;
     private Integer sortOrder;

     public String getUrl() {
          return url;
     }

     public void setUrl(String url) {
          this.url = url;
     }

     public Boolean getIsThumbnail() {
          return isThumbnail;
     }

     public void setIsThumbnail(Boolean isThumbnail) {
          this.isThumbnail = isThumbnail;
     }

     public Integer getSortOrder() {
          return sortOrder;
     }

     public void setSortOrder(Integer sortOrder) {
          this.sortOrder = sortOrder;
     }
}
