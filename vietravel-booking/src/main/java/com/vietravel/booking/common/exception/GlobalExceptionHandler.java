package com.vietravel.booking.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex){
        String msg="Dữ liệu không hợp lệ";
        String root=ex.getMostSpecificCause()!=null?ex.getMostSpecificCause().getMessage():"";
        if(root!=null&&root.toLowerCase().contains("tour_lines")&&root.toLowerCase().contains("code")){
            msg="Code đã tồn tại";
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message",msg));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message",msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message",ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(ResponseStatusException ex){
        String msg=ex.getReason()!=null?ex.getReason():"Có lỗi xảy ra";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message",msg));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex){
        String msg=ex.getMessage()!=null?ex.getMessage():"Có lỗi xảy ra";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message",msg));
    }
}
