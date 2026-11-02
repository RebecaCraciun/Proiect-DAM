package ro.dam.salafitness.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Configurarea aplicatiei Spring: modelul domeniului (entitati, value objects, convertori de enumerari),
 * depozitele JPA, tranzactiile si bean-urile de infrastructura folosite de fabrici si servicii.
 * Validatorul JavaBean Validation este configurat automat de spring-boot-starter-validation.
 */
@Configuration
@EnableTransactionManagement
@EnableScheduling
@EntityScan(basePackages = "ro.dam.salafitness.domain.model")
@EnableJpaRepositories(basePackages = "ro.dam.salafitness.domain.repository")
public class SalaFitnessConfig {

    public static final ZoneId FUS_ORAR = ZoneId.of("Europe/Bucharest");

    /** Sursa unica pentru "acum" (RC05, RA03, RA04, UC17); in teste se inlocuieste cu un ceas controlat. */
    @Bean
    public Clock clock() {
        return Clock.system(FUS_ORAR);
    }

    /** RV03: in baza de date se stocheaza doar hash-ul BCrypt al parolei. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
