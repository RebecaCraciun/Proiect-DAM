package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EntityListeners;
import ro.dam.salafitness.domain.model.listener.CampuriCalculateListener;
import ro.dam.salafitness.domain.model.listener.CuCampuriCalculate;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ro.dam.salafitness.domain.model.vo.Bani;

import java.util.Objects;

/**
 * Componenta a agregatului Vanzare.
 */
@Entity
@Table(name = "linie_vanzare")
@EntityListeners(CampuriCalculateListener.class)
public class LinieVanzare implements CuCampuriCalculate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_linie_vanzare")
    private Long idLinieVanzare;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vanzare", nullable = false)
    private Vanzare vanzare;

    @NotNull
    @Column(name = "id_produs", nullable = false)
    private Integer idProdus;

    /** RV20 */
    @NotNull
    @Positive(message = "Cantitatea trebuie sa fie > 0")
    @Column(name = "cantitate", nullable = false)
    private Integer cantitate;

    /** RV20 / RC07: snapshot al pretului produsului la momentul vanzarii (RB06) */
    @NotNull
    @Valid
    @Embedded
    @AttributeOverride(name = "valoare", column = @Column(name = "pret_unitar", nullable = false, precision = 10, scale = 2))
    private Bani pretUnitar;

    /** RV20 / RC01 */
    @NotNull
    @Valid
    @Embedded
    @AttributeOverride(name = "valoare", column = @Column(name = "total_linie", nullable = false, precision = 10, scale = 2))
    private Bani totalLinie;

    protected LinieVanzare() {
        // JPA
    }

    LinieVanzare(Vanzare vanzare, Integer idProdus, Integer cantitate, Bani pretUnitar) {
        this.vanzare = vanzare;
        this.idProdus = idProdus;
        this.cantitate = cantitate;
        this.pretUnitar = pretUnitar;
        recalculeazaTotal();
    }

    /** RC01: total_linie = cantitate x pret_unitar. */
    public Bani recalculeazaTotal() {
        totalLinie = pretUnitar.inmultitCu(cantitate);
        return totalLinie;
    }

    void majoreazaCantitatea(int cantitateSuplimentara) {
        cantitate += cantitateSuplimentara;
        recalculeazaTotal();
    }

    public Long getIdLinieVanzare() {
        return idLinieVanzare;
    }

    public Vanzare getVanzare() {
        return vanzare;
    }

    public Integer getIdProdus() {
        return idProdus;
    }

    public Integer getCantitate() {
        return cantitate;
    }

    public Bani getPretUnitar() {
        return pretUnitar;
    }

    public Bani getTotalLinie() {
        return totalLinie;
    }

    // RC01
    @Override
    public void recalculeazaCampuriCalculate() {
        recalculeazaTotal();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof LinieVanzare l && idLinieVanzare != null
                && idLinieVanzare.equals(l.getIdLinieVanzare()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idLinieVanzare);
    }
}
