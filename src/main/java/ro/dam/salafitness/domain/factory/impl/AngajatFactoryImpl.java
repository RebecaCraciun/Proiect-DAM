package ro.dam.salafitness.domain.factory.impl;

import jakarta.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.dam.salafitness.domain.factory.AngajatFactory;
import ro.dam.salafitness.domain.factory.DateAngajatNou;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.model.vo.Email;
import ro.dam.salafitness.domain.model.vo.Telefon;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class AngajatFactoryImpl extends AbstractFactory implements AngajatFactory {

    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public AngajatFactoryImpl(Validator validator, PasswordEncoder passwordEncoder, Clock clock) {
        super(validator);
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    public Angajat creeazaAngajat(DateAngajatNou date) {
        String parola = date.parola() == null || date.parola().isBlank() ? date.parola()
                : passwordEncoder.encode(date.parola());
        return valid(new Angajat(
                date.nume(),
                date.prenume(),
                Email.of(date.email()),
                parola,
                Telefon.of(date.telefon()),
                LocalDateTime.now(clock),
                date.idTipAngajat(),
                date.functie(),
                date.salariu() == null ? Bani.ZERO : Bani.of(date.salariu()),
                date.dataAngajarii() == null ? LocalDate.now(clock) : date.dataAngajarii(),
                date.programLucru()));
    }
}
