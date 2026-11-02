package ro.dam.salafitness.domain.factory.impl;

import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import ro.dam.salafitness.domain.factory.ClasaFitnessFactory;
import ro.dam.salafitness.domain.model.entity.ClasaFitness;

@Component
public class ClasaFitnessFactoryImpl extends AbstractFactory implements ClasaFitnessFactory {

    public ClasaFitnessFactoryImpl(Validator validator) {
        super(validator);
    }

    @Override
    public ClasaFitness creeazaClasa(String denumire, Integer idAntrenor, int capacitateMaxima, String descriere) {
        return valid(new ClasaFitness(denumire, idAntrenor, capacitateMaxima, descriere));
    }
}
