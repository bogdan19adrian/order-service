package com.bogdanenache.order_service.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Stream;
/**
 * Implementation of the OneOf constraint validator that checks if a given string
 * matches one of the enum constants defined in the specified enum class.
 */

public class OneOfValidator implements ConstraintValidator<OneOf, CharSequence> {

    private List<String> acceptedValues;

    @Override
    public void initialize(OneOf annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .toList();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return acceptedValues.contains(value.toString());
    }

}
