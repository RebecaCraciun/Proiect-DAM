package ro.dam.salafitness.domain.factory.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import java.util.Set;

/**
 * Baza comuna a fabricilor: orice agregat produs trece prin JavaBean Validation.
 */
abstract class AbstractFactory {

    private final Validator validator;

    protected AbstractFactory(Validator validator) {
        this.validator = validator;
    }

    protected <T> T valid(T agregat) {
        Set<ConstraintViolation<T>> violari = validator.validate(agregat);
        if (!violari.isEmpty()) {
            throw new ConstraintViolationException(violari);
        }
        return agregat;
    }
}
