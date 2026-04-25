package com.vietravel.booking.service.payment;

import com.vietravel.booking.config.VnpayProperties;
import com.vietravel.booking.domain.entity.booking.Booking;
import com.vietravel.booking.domain.entity.booking.Payment;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VnpayService {

     private static final DateTimeFormatter VNPAY_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

     private final VnpayProperties properties;

     public VnpayService(VnpayProperties properties) {
          this.properties = properties;
     }

     public String buildPaymentUrl(Payment payment, Booking booking, String ipAddress) {
          Map<String, String> params = new HashMap<>();
          params.put("vnp_Version", properties.getVersion());
          params.put("vnp_Command", properties.getCommand());
          params.put("vnp_TmnCode", properties.getTmnCode());
          params.put("vnp_Amount", toVnpAmount(payment.getAmount()));
          params.put("vnp_CurrCode", properties.getCurrency());
          params.put("vnp_TxnRef", payment.getTxnRef());
          String orderInfo = "Thanh toan booking " + booking.getBookingCode();
          params.put("vnp_OrderInfo", java.text.Normalizer.normalize(orderInfo, java.text.Normalizer.Form.NFC));
          params.put("vnp_OrderType", properties.getOrderType());
          params.put("vnp_Locale", properties.getLocale());
          params.put("vnp_ReturnUrl", properties.getReturnUrl());
          params.put("vnp_IpAddr", (ipAddress == null || ipAddress.isBlank()) ? "127.0.0.1" : ipAddress);

          LocalDateTime now = LocalDateTime.now(ZoneId.of(properties.getTimeZone()));
          params.put("vnp_CreateDate", now.format(VNPAY_TIME));
          params.put("vnp_ExpireDate", now.plusMinutes(properties.getExpireMinutes()).format(VNPAY_TIME));

          return buildUrl(params);
     }

     public boolean verifySignature(Map<String, String> params, String secureHash) {
          if (secureHash == null || secureHash.isBlank()) {
               return false;
          }
          Map<String, String> filtered = new HashMap<>(params);
          filtered.remove("vnp_SecureHash");
          filtered.remove("vnp_SecureHashType");
          String hashData = buildHashData(filtered);
          String signValue = hmacSHA512(secret(), hashData);
          return signValue.equalsIgnoreCase(secureHash);
     }

     private String toVnpAmount(BigDecimal amount) {
          if (amount == null) {
               return "0";
          }
          BigDecimal scaled = amount.multiply(BigDecimal.valueOf(100));
          return String.valueOf(scaled.longValue());
     }

     private String buildUrl(Map<String, String> params) {
          List<String> fieldNames = new ArrayList<>(params.keySet());
          Collections.sort(fieldNames);

          StringBuilder query = new StringBuilder();
          StringBuilder hashData = new StringBuilder();
          for (String fieldName : fieldNames) {
               String value = params.get(fieldName);
               if (value == null || value.isBlank()) {
                    continue;
               }
               String encodedValue = encode(value);
               if (hashData.length() > 0) {
                    hashData.append('&');
               }
               hashData.append(fieldName).append('=').append(encodedValue);

               if (query.length() > 0) {
                    query.append('&');
               }
               query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                         .append('=')
                         .append(encodedValue);
          }

          String secureHash = hmacSHA512(secret(), hashData.toString());
          query.append("&vnp_SecureHashType=HmacSHA512");
          query.append("&vnp_SecureHash=").append(secureHash);

          return properties.getPayUrl() + "?" + query;
     }

     private String buildHashData(Map<String, String> params) {
          List<String> fieldNames = new ArrayList<>(params.keySet());
          Collections.sort(fieldNames);
          StringBuilder hashData = new StringBuilder();
          for (String fieldName : fieldNames) {
               String value = params.get(fieldName);
               if (value == null || value.isBlank()) {
                    continue;
               }
               String encodedValue = encode(value);
               if (hashData.length() > 0) {
                    hashData.append('&');
               }
               hashData.append(fieldName).append('=').append(encodedValue);
          }
          return hashData.toString();
     }

     private String encode(String value) {
          return URLEncoder.encode(value, StandardCharsets.UTF_8);
     }

     private String secret() {
          return properties.getHashSecret() == null ? "" : properties.getHashSecret().trim();
     }

     private String hmacSHA512(String key, String data) {
          try {
               Mac hmac = Mac.getInstance("HmacSHA512");
               SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
               hmac.init(secretKey);
               byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
               StringBuilder sb = new StringBuilder(bytes.length * 2);
               for (byte b : bytes) {
                    sb.append(String.format("%02x", b));
               }
               return sb.toString();
          } catch (Exception e) {
               throw new IllegalStateException("Không thể tạo chữ ký VNPAY", e);
          }
     }
}
