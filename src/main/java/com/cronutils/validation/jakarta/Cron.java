package com.cronutils.validation.jakarta;

import com.cronutils.model.CronType;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CronJakartaValidator.class)
@Inherited
@Documented
public @interface Cron {

    String message() default "UNUSED";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    CronType type();

}