package com.uberplus.backend.utils;

import com.uberplus.backend.dto.common.MessageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

//@ControllerAdvice  // Global for all controllers
public class GlobalExceptionHandler {

//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<MessageDTO> handleRuntimeException(RuntimeException e) {
//        MessageDTO error = new MessageDTO("Error: "+ e.getMessage(), false);
//        return ResponseEntity.badRequest().body(error);
//    }
}
