package com.baraka.matching_engine.util;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DirectionValidator implements ConstraintValidator<ValidateDirection, CharSequence> {

	private List<String> acceptedValues;

	@Override
	public void initialize(ValidateDirection annotation) {
		acceptedValues = Stream.of(annotation.enumClass().getEnumConstants()).map(Enum::name)
				.collect(Collectors.toList());
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if (Objects.isNull(value))
			return false;
		return acceptedValues.contains(value.toString());
	}
}