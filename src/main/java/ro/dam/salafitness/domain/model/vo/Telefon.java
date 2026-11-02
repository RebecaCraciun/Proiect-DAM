package ro.dam.salafitness.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;

import java.util.Objects;

/**
 * Value object pentru numarul de telefon. Date personale protejate (RB07).
 */
@Embeddable
public class Telefon {

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Telefonul trebuie sa aiba intre 10 si 15 cifre")
    @Column(name = "telefon")
    private String numar;

    protected Telefon() {
        // JPA
    }

    private Telefon(String numar) {
        this.numar = numar == null ? null : numar.replaceAll("[\\s.-]", "");
    }

    public static Telefon of(String numar) {
        return numar == null || numar.isBlank() ? null : new Telefon(numar);
    }

    public String getNumar() {
        return numar;
    }

    /** Varianta afisata personalului neautorizat (RB07): 07******89. */
    public String mascat() {
        if (numar == null || numar.length() < 4) {
            return "****";
        }
        return numar.substring(0, 2) + "*".repeat(numar.length() - 4) + numar.substring(numar.length() - 2);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Telefon t && Objects.equals(numar, t.numar));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(numar);
    }

    @Override
    public String toString() {
        return mascat();
    }
}
