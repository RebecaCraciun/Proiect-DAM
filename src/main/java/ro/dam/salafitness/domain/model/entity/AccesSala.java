package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.model.enums.StatusAcces;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Agregat cu o singura entitate: o incercare de intrare in sala, permisa sau respinsa (UC06).
 */
@Entity
@Table(name = "acces_sala")
public class AccesSala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acces")
    private Long idAcces;

    @NotNull
    @Column(name = "id_utilizator", nullable = false)
    private Integer idUtilizator;

    @NotNull
    @Column(name = "data_ora_intrare", nullable = false)
    private LocalDateTime dataOraIntrare;

    @Column(name = "data_ora_iesire")
    private LocalDateTime dataOraIesire;

    /** RV18 / RA07 */
    @NotNull
    @Column(name = "status_acces", nullable = false, length = 20)
    private StatusAcces statusAcces;

    protected AccesSala() {
        // JPA
    }

    private AccesSala(Integer idUtilizator, LocalDateTime dataOraIntrare, StatusAcces statusAcces) {
        this.idUtilizator = idUtilizator;
        this.dataOraIntrare = dataOraIntrare;
        this.statusAcces = statusAcces;
    }

    public static AccesSala permis(Integer idUtilizator, LocalDateTime moment) {
        return new AccesSala(idUtilizator, moment, StatusAcces.PERMIS);
    }

    public static AccesSala respins(Integer idUtilizator, LocalDateTime moment) {
        return new AccesSala(idUtilizator, moment, StatusAcces.RESPINS);
    }

    /** RV18: iesire &ge; intrare sau necompletata. */
    @AssertTrue(message = "Ora iesirii nu poate fi inaintea orei intrarii")
    public boolean isIntervalValid() {
        return dataOraIesire == null || dataOraIntrare == null || !dataOraIesire.isBefore(dataOraIntrare);
    }

    /** UC06 pas 4 */
    public void inregistreazaIesire(LocalDateTime moment) {
        if (statusAcces != StatusAcces.PERMIS) {
            throw new BusinessRuleException("RV18", "Un acces respins nu are iesire");
        }
        if (dataOraIesire != null) {
            throw new BusinessRuleException("RV18", "Iesirea a fost deja inregistrata");
        }
        if (moment.isBefore(dataOraIntrare)) {
            throw new BusinessRuleException("RV18", "Ora iesirii nu poate fi inaintea orei intrarii");
        }
        this.dataOraIesire = moment;
    }

    public boolean estePermis() {
        return statusAcces == StatusAcces.PERMIS;
    }

    public Long getIdAcces() {
        return idAcces;
    }

    public Integer getIdUtilizator() {
        return idUtilizator;
    }

    public LocalDateTime getDataOraIntrare() {
        return dataOraIntrare;
    }

    public LocalDateTime getDataOraIesire() {
        return dataOraIesire;
    }

    public StatusAcces getStatusAcces() {
        return statusAcces;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof AccesSala a && idAcces != null && idAcces.equals(a.getIdAcces()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idAcces);
    }
}
