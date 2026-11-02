package ro.dam.salafitness.domain.factory.impl;

import jakarta.validation.Validator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.dam.salafitness.domain.factory.ClientFactory;
import ro.dam.salafitness.domain.factory.DateClientNou;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.vo.ContactUrgenta;
import ro.dam.salafitness.domain.model.vo.Email;
import ro.dam.salafitness.domain.model.vo.Telefon;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class ClientFactoryImpl extends AbstractFactory implements ClientFactory {

    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public ClientFactoryImpl(Validator validator, PasswordEncoder passwordEncoder, Clock clock) {
        super(validator);
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    public Client creeazaClient(DateClientNou date) {
        return valid(new Client(
                date.nume(),
                date.prenume(),
                Email.of(date.email()),
                hash(date.parola()),
                Telefon.of(date.telefon()),
                LocalDateTime.now(clock),
                date.preferinteFitness(),
                ContactUrgenta.of(date.contactUrgenta())));
    }

    /** RV03: parola goala ramane goala, ca sa fie respinsa de @NotBlank. */
    private String hash(String parola) {
        return parola == null || parola.isBlank() ? parola : passwordEncoder.encode(parola);
    }
}
