package com.eventostec.api.utils;

import com.eventostec.api.exceptions.config.ProblemDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Optional;

public final class ExceptionUtil {

    public static final String TIPO_DESCONHECIDO = "tipo desconhecido";
    public static final String VALOR_NAO_INFORMADO = "valor não informado";

    private ExceptionUtil() {
        throw new IllegalStateException("Cannot be instantiated");
    }

    public static ProblemDetails getProblemDetails(HttpServletRequest request, Exception ex) {
        return switch (ex.getClass().getSimpleName()) {
            case "MethodArgumentTypeMismatchException" -> handleMethodArgumentTypeMismatch((MethodArgumentTypeMismatchException) ex, request);
            case "MissingServletRequestParameterException" ->
                    handleMissingServletRequestParameter((MissingServletRequestParameterException) ex, request);
            case "DataIntegrityViolationException" -> handleDataIntegrityViolation(request);
            case "MethodArgumentNotValidException" -> handleMethodArgumentNotValid((MethodArgumentNotValidException) ex, request);
            case "ConversionFailedException" -> handleConversionFailed((ConversionFailedException) ex, request);
            default -> new ProblemDetails(
                    "Erro não especificado",
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "Ocorreu um erro inesperado.",
                    request.getRequestURI()
            );
        };
    }

    private static ProblemDetails handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String title = "Campo inválido informado";
        String fieldName = ex.getName();
        String requiredType = Optional.ofNullable(ex.getRequiredType())
                .map(Class::getSimpleName)
                .orElse(TIPO_DESCONHECIDO);
        String invalidValue = Optional.ofNullable(ex.getValue())
                .map(Object::toString)
                .orElse(VALOR_NAO_INFORMADO);
        String detail = String.format("O campo '%s' recebeu um valor inválido: '%s'. Esperava-se um valor do tipo '%s'.", fieldName, invalidValue, requiredType);

        return new ProblemDetails(title, HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), detail, request.getRequestURI());
    }

    private static ProblemDetails handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String title = "Campo não informado";
        String detail = String.format("O dado do campo '%s' não foi informado.", ex.getParameterName());

        return new ProblemDetails(title, HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), detail, request.getRequestURI());
    }

    private static ProblemDetails handleDataIntegrityViolation(HttpServletRequest request) {
        String title = "Violação de integridade de campo";
        String detail = "Violação de integridade detectada no banco de dados.";

        return new ProblemDetails(title, HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), detail, request.getRequestURI());
    }

    private static ProblemDetails handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String title = "Validação de campo violada";
        String failedValidationMessage = "A validação falhou em um campo não identificado.";
        String detail;

        if (ex.getBindingResult().hasFieldErrors()) {
            FieldError fieldError = ex.getBindingResult().getFieldError();
            if (fieldError != null) {
                String violatedField = fieldError.getField();
                String errorMessage = fieldError.getDefaultMessage();

                if (errorMessage != null && errorMessage.contains("Failed to convert value of type")) {
                    String rejectedValue = Optional.ofNullable(fieldError.getRejectedValue())
                            .map(Object::toString)
                            .orElse(VALOR_NAO_INFORMADO);
                    errorMessage = String.format("'%s' não é válido.", rejectedValue);
                }

                detail = String.format("A validação do campo '%s' falhou: %s.", violatedField, errorMessage);
            } else {
                detail = failedValidationMessage;
            }
        } else {
            detail = failedValidationMessage;
        }

        return new ProblemDetails(title, HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), detail, request.getRequestURI());
    }

    private static ProblemDetails handleConversionFailed(ConversionFailedException ex, HttpServletRequest request) {
        String title = "Campo inválido informado";
        String invalidValue = Optional.ofNullable(ex.getValue())
                .map(Object::toString)
                .orElse(VALOR_NAO_INFORMADO);
        String requiredType = Optional.of(ex.getTargetType())
                .map(typeDescriptor -> typeDescriptor.getType().getSimpleName())
                .orElse(TIPO_DESCONHECIDO);
        String detail = String.format("O valor '%s' fornecido é inválido. Esperava-se um valor do tipo '%s'.", invalidValue, requiredType);

        return new ProblemDetails(title, HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), detail, request.getRequestURI());
    }
}
