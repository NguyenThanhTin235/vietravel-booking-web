package com.vietravel.booking.web.dto.upload;

public class CloudinaryUploadResponse{
    private String publicId;
    private String url;
    private String secureUrl;
    private Integer width;
    private Integer height;
    private Long bytes;
    private String format;

    public String getPublicId(){return publicId;}
    public void setPublicId(String publicId){this.publicId=publicId;}

    public String getUrl(){return url;}
    public void setUrl(String url){this.url=url;}

    public String getSecureUrl(){return secureUrl;}
    public void setSecureUrl(String secureUrl){this.secureUrl=secureUrl;}

    public Integer getWidth(){return width;}
    public void setWidth(Integer width){this.width=width;}

    public Integer getHeight(){return height;}
    public void setHeight(Integer height){this.height=height;}

    public Long getBytes(){return bytes;}
    public void setBytes(Long bytes){this.bytes=bytes;}

    public String getFormat(){return format;}
    public void setFormat(String format){this.format=format;}
}
