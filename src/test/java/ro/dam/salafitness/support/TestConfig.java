package ro.dam.salafitness.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.dam.salafitness.config.SalaFitnessConfig;

/**
 * Inlocuieste bean-urile de infrastructura din {@link SalaFitnessConfig} pentru teste.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public CeasControlat ceasControlat() {
        return new CeasControlat(SalaFitnessConfig.FUS_ORAR, AbstractIntegrationTest.ACUM);
    }

    /** Cost BCrypt minim, ca testele sa ruleze repede; algoritmul ramane acelasi (RV03). */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoderTest() {
        return new BCryptPasswordEncoder(4);
    }
}
