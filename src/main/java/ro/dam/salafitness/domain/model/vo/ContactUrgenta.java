package ro.dam.salafitness.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * Value object pentru persoana de contact in caz de urgenta (nume si telefon intr-o singura coloana).
 * Date personale protejate (RB07).
 */
@Embeddable
public class ContactUrgenta {

    @Size(max = 150, message = "Contactul de urgenta are maximum 150 de caractere")
    @Column(name = "contact_urgenta")
    private String detalii;

    protected ContactUrgenta() {
        // JPA
    }

    private ContactUrgenta(String detalii) {
        this.detalii = detalii.trim();
    }

    public static ContactUrgenta of(String detalii) {
        return detalii == null || detalii.isBlank() ? null : new ContactUrgenta(detalii);
    }

    public String getDetalii() {
        return detalii;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ContactUrgenta c && Objects.equals(detalii, c.detalii));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(detalii);
    }

    @Override
    public String toString() {
        return "[confidential]";
    }
}
