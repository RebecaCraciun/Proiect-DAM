package ro.dam.salafitness.domain.factory.impl;

import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import ro.dam.salafitness.domain.factory.VanzareFactory;
import ro.dam.salafitness.domain.model.entity.Vanzare;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class VanzareFactoryImpl extends AbstractFactory implements VanzareFactory {

    private final Clock clock;

    public VanzareFactoryImpl(Validator validator, Clock clock) {
        super(validator);
        this.clock = clock;
    }

    @Override
    public Vanzare deschideVanzare(Integer idClient, Integer idAngajat) {
        return valid(new Vanzare(idClient, idAngajat, LocalDateTime.now(clock)));
    }
}
