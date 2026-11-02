package ro.dam.salafitness.domain.factory.impl;

import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import ro.dam.salafitness.domain.factory.EchipamentFactory;
import ro.dam.salafitness.domain.model.entity.Echipament;

import java.time.LocalDate;

@Component
public class EchipamentFactoryImpl extends AbstractFactory implements EchipamentFactory {

    public EchipamentFactoryImpl(Validator validator) {
        super(validator);
    }

    @Override
    public Echipament creeazaEchipament(String denumire, LocalDate dataAchizitie, String stareUzura,
                                        String locatieInSala) {
        return valid(new Echipament(denumire, dataAchizitie, stareUzura, locatieInSala));
    }
}
