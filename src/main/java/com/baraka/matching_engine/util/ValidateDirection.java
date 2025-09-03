package com.baraka.matching_engine.util;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = DirectionValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface ValidateDirection {

	Class<? extends Enum<?>> enumClass();

	String message() default "must be any of enum {Direction}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
