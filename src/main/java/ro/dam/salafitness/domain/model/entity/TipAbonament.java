package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ro.dam.salafitness.domain.model.vo.Bani;

import java.util.Objects;

/**
 * Nomenclator (agregat cu o singura entitate): denumire, durata in zile, pret.
 */
@Entity
@Table(name = "tipuri_abonament")
public class TipAbonament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tip_abonament")
    private Integer idTipAbonament;

    /** RV07 */
    @NotBlank(message = "Denumirea tipului de abonament este obligatorie")
    @Size(max = 100)
    @Column(name = "denumire", nullable = false, unique = true, length = 100)
    private String denumire;

    /** RV08 */
    @NotNull
    @Positive(message = "Durata abonamentului trebuie sa fie > 0 zile")
    @Column(name = "durata_zile", nullable = false)
    private Integer durataZile;

    /** RV08 */
    @NotNull
    @Valid
    @Embedded
    @AttributeOverride(name = "valoare", column = @Column(name = "pret", nullable = false, precision = 10, scale = 2))
    private Bani pret;

    protected TipAbonament() {
        // JPA
    }

    public TipAbonament(String denumire, Integer durataZile, Bani pret) {
        this.denumire = denumire;
        this.durataZile = durataZile;
        this.pret = pret;
    }

    /** RB06: modificarea pretului nu afecteaza platile trecute (snapshot in Plata.suma). */
    public void modificaPret(Bani pretNou) {
        this.pret = pretNou;
    }

    public void modificaDurata(Integer durataZile) {
        this.durataZile = durataZile;
    }

    public Integer getIdTipAbonament() {
        return idTipAbonament;
    }

    public String getDenumire() {
        return denumire;
    }

    public Integer getDurataZile() {
        return durataZile;
    }

    public Bani getPret() {
        return pret;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof TipAbonament t && idTipAbonament != null
                && idTipAbonament.equals(t.getIdTipAbonament()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idTipAbonament);
    }
}
