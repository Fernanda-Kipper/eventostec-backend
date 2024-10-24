package com.eventostec.api.exceptions.config;

import com.eventostec.api.utils.ExceptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            DataIntegrityViolationException.class,
            MethodArgumentNotValidException.class,
            ConversionFailedException.class
    })
    public ResponseEntity<ProblemDetails> handleException(Exception ex, HttpServletRequest request) {
        ProblemDetails problemDetails = ExceptionUtil.getProblemDetails(request, ex);
        return new ResponseEntity<>(problemDetails, HttpStatus.BAD_REQUEST);
    }
}