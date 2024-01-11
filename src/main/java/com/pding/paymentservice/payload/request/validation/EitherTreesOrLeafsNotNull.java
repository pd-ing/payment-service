package com.pding.paymentservice.payload.request.validation;

import com.pding.paymentservice.payload.request.PaymentDetailsRequest;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {EitherTreesOrLeafsNotNullValidator.class})
public @interface EitherTreesOrLeafsNotNull {

    String message() default "Only one of trees or leafs should be not null, or both cannot have a value or both cannot be null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

class EitherTreesOrLeafsNotNullValidator implements ConstraintValidator<EitherTreesOrLeafsNotNull, PaymentDetailsRequest> {
    @Override
    public boolean isValid(PaymentDetailsRequest request, ConstraintValidatorContext context) {
        // Ensure that only one of trees or leafs is not null
        return (request.getTrees() != null && request.getLeafs() == null) ||
                (request.getTrees() == null && request.getLeafs() != null);
    }
}