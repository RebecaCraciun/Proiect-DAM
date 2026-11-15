package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EntityListeners;
import ro.dam.salafitness.domain.model.listener.AuditEntitateListener;
import ro.dam.salafitness.domain.model.listener.Auditabil;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.model.vo.Bani;

import java.util.Objects;

/**
 * Agregat cu o singura entitate: articol vandut la receptie.
 */
@Entity
@Table(name = "produse")
@EntityListeners(AuditEntitateListener.class)
public class Produs implements Auditabil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produs")
    private Integer idProdus;

    @NotBlank(message = "Denumirea produsului este obligatorie")
    @Size(max = 100)
    @Column(name = "denumire", nullable = false, length = 100)
    private String denumire;

    @Size(max = 50)
    @Column(name = "categorie", length = 50)
    private String categorie;

    /** RV19 */
    @NotNull
    @Valid
    @Embedded
    @AttributeOverride(name = "valoare", column = @Column(name = "pret", nullable = false, precision = 10, scale = 2))
    private Bani pret;

    /** RV19 */
    @NotNull
    @PositiveOrZero(message = "Stocul nu poate fi negativ")
    @Column(name = "stoc_curent", nullable = false)
    private Integer stocCurent;

    protected Produs() {
        // JPA
    }

    public Produs(String denumire, String categorie, Bani pret, Integer stocCurent) {
        this.denumire = denumire;
        this.categorie = categorie;
        this.pret = pret;
        this.stocCurent = stocCurent;
    }

    public boolean areStoc(int cantitate) {
        return stocCurent >= cantitate;
    }

    /** RC08: stocul scade cu cantitatea vanduta; RA14 / RV19: nu poate deveni negativ. */
    public void scadeStoc(int cantitate) {
        if (cantitate <= 0) {
            throw new BusinessRuleException("RV20", "Cantitatea trebuie sa fie > 0");
        }
        if (!areStoc(cantitate)) {
            throw new BusinessRuleException("RA14", "Stoc insuficient pentru " + denumire);
        }
        stocCurent -= cantitate;
    }

    /** UC23 */
    public void adaugaStoc(int cantitate) {
        if (cantitate <= 0) {
            throw new BusinessRuleException("RV19", "Cantitatea receptionata trebuie sa fie > 0");
        }
        stocCurent += cantitate;
    }

    /** RB06: vanzarile trecute pastreaza pretul din linie (snapshot). */
    public void modificaPret(Bani pretNou) {
        this.pret = pretNou;
    }

    public Integer getIdProdus() {
        return idProdus;
    }

    public String getDenumire() {
        return denumire;
    }

    public String getCategorie() {
        return categorie;
    }

    public Bani getPret() {
        return pret;
    }

    public Integer getStocCurent() {
        return stocCurent;
    }

    @Override
    public Object idAudit() {
        return idProdus;
    }

    @Override
    public String detaliiAudit() {
        return denumire + ", pret " + pret + ", stoc " + stocCurent;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Produs p && idProdus != null && idProdus.equals(p.getIdProdus()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idProdus);
    }
}
