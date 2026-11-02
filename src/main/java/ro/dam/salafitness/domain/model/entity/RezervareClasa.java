package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import ro.dam.salafitness.domain.model.enums.StatusRezervare;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Componenta a agregatului Clasa Fitness. Clientul (alt agregat) este referit prin id.
 * RA02: UNIQUE(id_utilizator_client, id_program).
 */
@Entity
@Table(name = "rezervari_clase", uniqueConstraints = @UniqueConstraint(
        name = "uq_rezervare_client_program", columnNames = {"id_utilizator_client", "id_program"}))
public class RezervareClasa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rezervare")
    private Long idRezervare;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_program", nullable = false)
    private ProgramClasa program;

    @NotNull
    @Column(name = "id_utilizator_client", nullable = false)
    private Integer idClient;

    /** RV16 */
    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    private StatusRezervare status = StatusRezervare.CONFIRMAT;

    @NotNull
    @Column(name = "data_rezervare", nullable = false)
    private LocalDateTime dataRezervare;

    protected RezervareClasa() {
        // JPA
    }

    RezervareClasa(ProgramClasa program, Integer idClient, LocalDateTime dataRezervare) {
        this.program = program;
        this.idClient = idClient;
        this.dataRezervare = dataRezervare;
        this.status = StatusRezervare.CONFIRMAT;
    }

    /** RA01 / RC09: rezervarile cu status diferit de "Anulat" ocupa un loc. */
    public boolean ocupaLoc() {
        return status != StatusRezervare.ANULAT;
    }

    void anuleaza() {
        status = StatusRezervare.ANULAT;
    }

    void marcheazaParticipare() {
        status = StatusRezervare.PARTICIPAT;
    }

    void reconfirma(LocalDateTime moment) {
        status = StatusRezervare.CONFIRMAT;
        dataRezervare = moment;
    }

    public Long getIdRezervare() {
        return idRezervare;
    }

    public ProgramClasa getProgram() {
        return program;
    }

    public Integer getIdClient() {
        return idClient;
    }

    public StatusRezervare getStatus() {
        return status;
    }

    public LocalDateTime getDataRezervare() {
        return dataRezervare;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof RezervareClasa r && idRezervare != null && idRezervare.equals(r.getIdRezervare()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idRezervare);
    }
}
