package ro.dam.salafitness.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;
import java.util.Objects;

/**
 * Value object pentru adresa de email (RV01). Adresa este normalizata (trim, litere mici).
 */
@Embeddable
public class Email {

    @NotBlank(message = "Emailul este obligatoriu")
    @jakarta.validation.constraints.Email(message = "Emailul nu are un format valid")
    @Size(max = 150, message = "Emailul are maximum 150 de caractere")
    @Column(name = "email")
    private String adresa;

    protected Email() {
        // JPA
    }

    private Email(String adresa) {
        this.adresa = adresa == null ? null : adresa.trim().toLowerCase(Locale.ROOT);
    }

    public static Email of(String adresa) {
        return new Email(adresa);
    }

    public String getAdresa() {
        return adresa;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Email e && Objects.equals(adresa, e.adresa));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(adresa);
    }

    @Override
    public String toString() {
        return adresa;
    }
}
