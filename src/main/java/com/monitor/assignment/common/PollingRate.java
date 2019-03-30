
package com.monitor.assignment.common;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { PollingRateValidator.class })
public @interface PollingRate {
    String message() default "The polling rate must be 1 second or greater";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class PollingRateValidator implements ConstraintValidator<PollingRate, Integer> {

    private static final int MIN_POLLING_RATE = 1;

    @Override
    public void initialize(PollingRate constraintAnnotation) {
    }

    @Override
    public boolean isValid(Integer pollingRate, ConstraintValidatorContext context) {

        return pollingRate != null && pollingRate >= MIN_POLLING_RATE;
    }
}
