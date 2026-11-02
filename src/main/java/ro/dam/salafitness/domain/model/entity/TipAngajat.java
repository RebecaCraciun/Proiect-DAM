package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ro.dam.salafitness.domain.model.enums.RolAngajat;

import java.util.Objects;

/**
 * Nomenclator de roluri: Antrenor, Receptioner, Femeie de serviciu, Manager, Tehnic.
 */
@Entity
@Table(name = "tip_angajat")
public class TipAngajat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tip_angajat")
    private Integer idTipAngajat;

    /** RV07 */
    @NotBlank(message = "Denumirea tipului de angajat este obligatorie")
    @Size(max = 50)
    @Column(name = "denumire", nullable = false, unique = true, length = 50)
    private String denumire;

    @Size(max = 2000)
    @Column(name = "descriere", length = 2000)
    private String descriere;

    protected TipAngajat() {
        // JPA
    }

    public TipAngajat(String denumire, String descriere) {
        this.denumire = denumire;
        this.descriere = descriere;
    }

    /** RA09, RA10, RA11 */
    public boolean esteRol(RolAngajat rol) {
        return rol.getDenumire().equalsIgnoreCase(denumire);
    }

    public Integer getIdTipAngajat() {
        return idTipAngajat;
    }

    public String getDenumire() {
        return denumire;
    }

    public String getDescriere() {
        return descriere;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof TipAngajat t && idTipAngajat != null && idTipAngajat.equals(t.getIdTipAngajat()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idTipAngajat);
    }
}
