package com.naso.restapi.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.ServletException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionAdvice {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<String> handleException(Exception e) {
        String text = new Message<>(false, "Error" + e.getMessage(),-1).toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(text, httpHeaders, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(AccessDeniedException e) {
        String text = new Message<>(false, e.getMessage(),-1).toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(text, httpHeaders, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(NoSuchElementException e) {
        String text = new Message<>(false, e.getMessage(),-1).toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(text, httpHeaders, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServletException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(ServletException e) {
        String text = new Message<>(false, e.getMessage(),-1).toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(text, httpHeaders, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IOException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(IOException e) {
        String text = e.getMessage();
        Message<Long> message = new Message<>(false, "Input error: " + text, Long.parseLong("-1"));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));

        if (text.contains("Incorrect password. Invalid count of log-in: ")) {
            long data = Long.parseLong(text.substring(45));
            message = new Message<>(false, "Input error: " + text.substring(0, 44), data);
        }

        return new ResponseEntity<>(message.toString(), httpHeaders, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(RuntimeException e) {
        String text = new Message<>(false, e.getMessage(),-1).toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(text, httpHeaders, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SignatureException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(SignatureException e) {
        String text = new Message<>(false, e.getMessage(),-1).toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(text, httpHeaders, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MalformedJwtException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(MalformedJwtException e) {
        String text = new Message<>(false, e.getMessage(),-1).toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(text, httpHeaders, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(ExpiredJwtException e) {
        String text = new Message<>(false, e.getMessage(),-1).toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(text, httpHeaders, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(UnsupportedJwtException e) {
        String text = new Message<>(false, e.getMessage(),-1).toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(text, httpHeaders, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<String> handleException(IllegalArgumentException e) {
        String text = new Message<>(false, e.getMessage(),-1).toString();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE));
        return new ResponseEntity<>(text, httpHeaders, HttpStatus.BAD_REQUEST);
    }

}
