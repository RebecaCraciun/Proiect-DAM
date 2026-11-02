package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.model.enums.StatusRezervare;
import ro.dam.salafitness.domain.model.vo.IntervalOrar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Componenta a agregatului Clasa Fitness: o sesiune concreta a clasei.
 * Modificarile se fac prin radacina (ClasaFitness), de aceea metodele de business sunt package-private.
 */
@Entity
@Table(name = "program_clase")
public class ProgramClasa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_program")
    private Long idProgram;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_clasa", nullable = false)
    private ClasaFitness clasa;

    /** RV15 */
    @NotNull
    @Valid
    @Embedded
    private IntervalOrar interval;

    /** RV15 */
    @NotBlank(message = "Sala / studioul este obligatoriu")
    @Size(max = 50)
    @Column(name = "sala_studio", nullable = false, length = 50)
    private String salaStudio;

    @OneToMany(mappedBy = "program", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<RezervareClasa> rezervari = new ArrayList<>();

    protected ProgramClasa() {
        // JPA
    }

    ProgramClasa(ClasaFitness clasa, IntervalOrar interval, String salaStudio) {
        this.clasa = clasa;
        this.interval = interval;
        this.salaStudio = salaStudio;
    }

    /** Numarul rezervarilor neanulate. */
    public int locuriOcupate() {
        return (int) rezervari.stream().filter(RezervareClasa::ocupaLoc).count();
    }

    /** RC09: capacitate_maxima - nr. rezervari neanulate. */
    public int locuriDisponibile() {
        return clasa.getCapacitateMaxima() - locuriOcupate();
    }

    public Optional<RezervareClasa> rezervareClient(Integer idClient) {
        return rezervari.stream().filter(r -> r.getIdClient().equals(idClient)).findFirst();
    }

    RezervareClasa rezerva(Integer idClient, LocalDateTime acum) {
        if (interval.aInceputLa(acum)) {
            throw new BusinessRuleException("RA03", "Sesiunea a inceput sau s-a terminat; nu se mai pot face rezervari");
        }
        Optional<RezervareClasa> existenta = rezervareClient(idClient);
        if (existenta.isPresent() && existenta.get().ocupaLoc()) {
            throw new BusinessRuleException("RA02", "Clientul are deja o rezervare la aceasta sesiune");
        }
        if (locuriDisponibile() <= 0) {
            throw new BusinessRuleException("RA01", "Clasa este completa (" + locuriOcupate() + " / "
                    + clasa.getCapacitateMaxima() + " locuri)");
        }
        // RA02 + UNIQUE(client, program): o rezervare anulata anterior se reactiveaza, nu se dubleaza
        if (existenta.isPresent()) {
            existenta.get().reconfirma(acum);
            return existenta.get();
        }
        RezervareClasa rezervare = new RezervareClasa(this, idClient, acum);
        rezervari.add(rezervare);
        return rezervare;
    }

    RezervareClasa anuleaza(Integer idClient, LocalDateTime acum) {
        if (interval.aInceputLa(acum)) {
            throw new BusinessRuleException("RA04", "Anularea este permisa doar inainte de inceperea sesiunii");
        }
        RezervareClasa rezervare = rezervareActiva(idClient);
        rezervare.anuleaza();
        return rezervare;
    }

    RezervareClasa marcheazaPrezenta(Integer idClient, LocalDateTime acum) {
        if (!interval.aInceputLa(acum)) {
            throw new BusinessRuleException("RA04", "Prezenta se marcheaza doar dupa inceperea sesiunii");
        }
        RezervareClasa rezervare = rezervareActiva(idClient);
        rezervare.marcheazaParticipare();
        return rezervare;
    }

    private RezervareClasa rezervareActiva(Integer idClient) {
        return rezervareClient(idClient)
                .filter(r -> r.getStatus() == StatusRezervare.CONFIRMAT)
                .orElseThrow(() -> new BusinessRuleException("RA04",
                        "Clientul " + idClient + " nu are o rezervare confirmata la aceasta sesiune"));
    }

    public Long getIdProgram() {
        return idProgram;
    }

    public ClasaFitness getClasa() {
        return clasa;
    }

    public IntervalOrar getInterval() {
        return interval;
    }

    public String getSalaStudio() {
        return salaStudio;
    }

    public List<RezervareClasa> getRezervari() {
        return Collections.unmodifiableList(rezervari);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ProgramClasa p && idProgram != null && idProgram.equals(p.getIdProgram()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idProgram);
    }
}
