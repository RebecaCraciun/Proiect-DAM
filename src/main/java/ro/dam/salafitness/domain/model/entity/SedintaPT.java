package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.model.enums.StatusSedinta;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Agregat cu o singura entitate: clasa de asociere antrenor - client (sedinta individuala).
 */
@Entity
@Table(name = "sedinte_pt")
public class SedintaPT {

    /** Durata standard a unei sedinte PT, folosita la verificarea suprapunerilor (RA13). */
    public static final int DURATA_MINUTE = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sedinta")
    private Long idSedinta;

    /** RA09 */
    @NotNull
    @Column(name = "id_utilizator_antrenor", nullable = false)
    private Integer idAntrenor;

    @NotNull
    @Column(name = "id_utilizator_client", nullable = false)
    private Integer idClient;

    @NotNull(message = "Data si ora sedintei sunt obligatorii")
    @Column(name = "data_ora", nullable = false)
    private LocalDateTime dataOra;

    /** RV17 */
    @NotNull
    @Convert(converter = StatusSedinta.Converter.class)
    @Column(name = "status", nullable = false, length = 20)
    private StatusSedinta status = StatusSedinta.PROGRAMAT;

    @Size(max = 2000)
    @Column(name = "note_antrenor", length = 2000)
    private String noteAntrenor;

    protected SedintaPT() {
        // JPA
    }

    public SedintaPT(Integer idAntrenor, Integer idClient, LocalDateTime dataOra) {
        this.idAntrenor = idAntrenor;
        this.idClient = idClient;
        this.dataOra = dataOra;
        this.status = StatusSedinta.PROGRAMAT;
    }

    /** RA13: sedintele anulate nu blocheaza antrenorul. */
    public boolean esteActiva() {
        return status != StatusSedinta.ANULAT;
    }

    /** UC16: statusul final este "Finalizat" sau "Absent", doar dupa ora programata. */
    public void finalizeaza(StatusSedinta statusFinal, String note, LocalDateTime acum) {
        if (statusFinal != StatusSedinta.FINALIZAT && statusFinal != StatusSedinta.ABSENT) {
            throw new BusinessRuleException("RV17", "Statusul final poate fi doar Finalizat sau Absent");
        }
        verificaProgramata();
        if (acum.isBefore(dataOra)) {
            throw new BusinessRuleException("RV17", "Sedinta nu poate fi finalizata inainte de ora programata");
        }
        this.status = statusFinal;
        this.noteAntrenor = note;
    }

    public void anuleaza() {
        verificaProgramata();
        this.status = StatusSedinta.ANULAT;
    }

    private void verificaProgramata() {
        if (status != StatusSedinta.PROGRAMAT) {
            throw new BusinessRuleException("RV17", "Sedinta are deja statusul " + status.getValoare());
        }
    }

    public Long getIdSedinta() {
        return idSedinta;
    }

    public Integer getIdAntrenor() {
        return idAntrenor;
    }

    public Integer getIdClient() {
        return idClient;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public StatusSedinta getStatus() {
        return status;
    }

    public String getNoteAntrenor() {
        return noteAntrenor;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof SedintaPT s && idSedinta != null && idSedinta.equals(s.getIdSedinta()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idSedinta);
    }
}
