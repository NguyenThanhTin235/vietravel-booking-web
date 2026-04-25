package com.vietravel.booking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.vnpay")
public class VnpayProperties {
     private String tmnCode;
     private String hashSecret;
     private String payUrl;
     private String returnUrl;
     private String version = "2.1.0";
     private String command = "pay";
     private String currency = "VND";
     private String locale = "vn";
     private String orderType = "other";
     private String timeZone = "Asia/Ho_Chi_Minh";
     private int expireMinutes = 15;

     public String getTmnCode() {
          return tmnCode;
     }

     public void setTmnCode(String tmnCode) {
          this.tmnCode = tmnCode;
     }

     public String getHashSecret() {
          return hashSecret;
     }

     public void setHashSecret(String hashSecret) {
          this.hashSecret = hashSecret;
     }

     public String getPayUrl() {
          return payUrl;
     }

     public void setPayUrl(String payUrl) {
          this.payUrl = payUrl;
     }

     public String getReturnUrl() {
          return returnUrl;
     }

     public void setReturnUrl(String returnUrl) {
          this.returnUrl = returnUrl;
     }

     public String getVersion() {
          return version;
     }

     public void setVersion(String version) {
          this.version = version;
     }

     public String getCommand() {
          return command;
     }

     public void setCommand(String command) {
          this.command = command;
     }

     public String getCurrency() {
          return currency;
     }

     public void setCurrency(String currency) {
          this.currency = currency;
     }

     public String getLocale() {
          return locale;
     }

     public void setLocale(String locale) {
          this.locale = locale;
     }

     public String getOrderType() {
          return orderType;
     }

     public void setOrderType(String orderType) {
          this.orderType = orderType;
     }

     public String getTimeZone() {
          return timeZone;
     }

     public void setTimeZone(String timeZone) {
          this.timeZone = timeZone;
     }

     public int getExpireMinutes() {
          return expireMinutes;
     }

     public void setExpireMinutes(int expireMinutes) {
          this.expireMinutes = expireMinutes;
     }
}
