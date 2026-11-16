package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EntityListeners;
import ro.dam.salafitness.domain.model.listener.AuditEntitateListener;
import ro.dam.salafitness.domain.model.listener.Auditabil;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Convert;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.model.enums.StatusPlataSalariu;
import ro.dam.salafitness.domain.model.vo.Bani;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Componenta a agregatului Angajat: plata salariala lunara.
 */
@Entity
@Table(name = "salarii_istoric")
@EntityListeners(AuditEntitateListener.class)
public class SalariuIstoric implements Auditabil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plata")
    private Long idPlata;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilizator_angajat", nullable = false)
    private Angajat angajat;

    /** RV12 / RC11 */
    @NotNull
    @Valid
    @Embedded
    @AttributeOverride(name = "valoare", column = @Column(name = "suma_platita", nullable = false, precision = 10, scale = 2))
    private Bani sumaPlatita;

    /** Prima zi a lunii platite */
    @NotNull
    @Column(name = "luna_an", nullable = false)
    private LocalDate lunaAn;

    /** RV12 */
    @NotNull
    @Convert(converter = StatusPlataSalariu.Converter.class)
    @Column(name = "status_plata", nullable = false, length = 20)
    private StatusPlataSalariu statusPlata = StatusPlataSalariu.IN_ASTEPTARE;

    @Column(name = "data_platii")
    private LocalDate dataPlatii;

    protected SalariuIstoric() {
        // JPA
    }

    SalariuIstoric(Angajat angajat, Bani sumaPlatita, LocalDate lunaAn) {
        this.angajat = angajat;
        this.sumaPlatita = sumaPlatita;
        this.lunaAn = lunaAn;
        this.statusPlata = StatusPlataSalariu.IN_ASTEPTARE;
    }

    /** RB05: o plata "Achitat" are obligatoriu data platii. */
    @AssertTrue(message = "O plata achitata trebuie sa aiba data platii")
    public boolean isDataPlatiiConsistenta() {
        return statusPlata != StatusPlataSalariu.ACHITAT || dataPlatii != null;
    }

    /** RB05: istoricul salarial nu se modifica retroactiv. */
    void marcheazaAchitat(LocalDate data) {
        if (statusPlata == StatusPlataSalariu.ACHITAT) {
            throw new BusinessRuleException("RB05", "Plata salariala " + idPlata + " este deja achitata");
        }
        if (data == null) {
            throw new BusinessRuleException("RB05", "Data platii este obligatorie");
        }
        this.statusPlata = StatusPlataSalariu.ACHITAT;
        this.dataPlatii = data;
    }

    public Long getIdPlata() {
        return idPlata;
    }

    public Angajat getAngajat() {
        return angajat;
    }

    public Bani getSumaPlatita() {
        return sumaPlatita;
    }

    public LocalDate getLunaAn() {
        return lunaAn;
    }

    public StatusPlataSalariu getStatusPlata() {
        return statusPlata;
    }

    public LocalDate getDataPlatii() {
        return dataPlatii;
    }

    @Override
    public Object idAudit() {
        return idPlata;
    }

    @Override
    public String detaliiAudit() {
        return sumaPlatita + " pentru " + lunaAn + ", " + (statusPlata == null ? "" : statusPlata.getValoare());
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof SalariuIstoric s && idPlata != null && idPlata.equals(s.getIdPlata()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idPlata);
    }
}
