package br.com.fourkitchen.ms_produtos.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida uma imagem enviada como Base64.
 *
 * <p>A imagem e opcional: valores nulos ou em branco sao considerados validos.
 * Quando preenchida, aceita Base64 puro ou Data URL no formato
 * {@code data:image/...;base64,...}.</p>
 */
@Documented
@Constraint(validatedBy = ImagemBase64Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ImagemBase64 {

    String message() default "Imagem deve estar em Base64 valido.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
