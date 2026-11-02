package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.model.vo.IntervalOrar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Radacina agregatului Clasa Fitness. Componente: ProgramClasa -&gt; RezervareClasa.
 * Invarianti protejati: RA01, RA02, RA03 (sesiune viitoare), RA04, RA12 (in cadrul clasei), RC09.
 * Antrenorul (agregatul Angajat) este referit prin id.
 */
@Entity
@Table(name = "clase_fitness")
public class ClasaFitness {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clasa")
    private Integer idClasa;

    @NotBlank(message = "Denumirea clasei este obligatorie")
    @Size(max = 100)
    @Column(name = "denumire", nullable = false, length = 100)
    private String denumire;

    /** RA09: trebuie sa fie un angajat cu tipul "Antrenor" (verificat de DomainValidationService) */
    @NotNull(message = "Antrenorul este obligatoriu")
    @Column(name = "id_utilizator_antrenor", nullable = false)
    private Integer idAntrenor;

    /** RV14 */
    @NotNull
    @Positive(message = "Capacitatea maxima trebuie sa fie > 0")
    @Column(name = "capacitate_maxima", nullable = false)
    private Integer capacitateMaxima;

    @Size(max = 2000)
    @Column(name = "descriere", length = 2000)
    private String descriere;

    @OneToMany(mappedBy = "clasa", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<ProgramClasa> sesiuni = new ArrayList<>();

    protected ClasaFitness() {
        // JPA
    }

    public ClasaFitness(String denumire, Integer idAntrenor, Integer capacitateMaxima, String descriere) {
        this.denumire = denumire;
        this.idAntrenor = idAntrenor;
        this.capacitateMaxima = capacitateMaxima;
        this.descriere = descriere;
    }

    /** UC22 / RA12 (sesiunile aceleiasi clase); suprapunerea cu alte clase o verifica DomainValidationService. */
    public ProgramClasa programeazaSesiune(IntervalOrar interval, String salaStudio) {
        sesiuni.stream()
                .filter(s -> s.getSalaStudio().equalsIgnoreCase(salaStudio) && s.getInterval().seSuprapuneCu(interval))
                .findAny()
                .ifPresent(s -> {
                    throw new BusinessRuleException("RA12", "Sala " + salaStudio + " este ocupata in intervalul " + s.getInterval());
                });
        ProgramClasa sesiune = new ProgramClasa(this, interval, salaStudio);
        sesiuni.add(sesiune);
        return sesiune;
    }

    public ProgramClasa sesiune(Long idProgram) {
        return sesiuni.stream()
                .filter(s -> idProgram.equals(s.getIdProgram()))
                .findFirst()
                .orElseThrow(() -> new EntitateNegasitaException("ProgramClasa", idProgram));
    }

    /** UC09: RA01, RA02, RA03 (sesiune viitoare). Abonamentul valid se verifica in serviciul de workflow. */
    public RezervareClasa rezerva(Long idProgram, Integer idClient, LocalDateTime acum) {
        return sesiune(idProgram).rezerva(idClient, acum);
    }

    /** UC11 / RA04 */
    public RezervareClasa anuleazaRezervare(Long idProgram, Integer idClient, LocalDateTime acum) {
        return sesiune(idProgram).anuleaza(idClient, acum);
    }

    /** UC15 / RA04: prezenta o marcheaza doar antrenorul clasei, dupa inceperea sesiunii. */
    public RezervareClasa marcheazaPrezenta(Long idProgram, Integer idClient, Integer idAntrenorSolicitant,
                                            LocalDateTime acum) {
        if (!idAntrenor.equals(idAntrenorSolicitant)) {
            throw new BusinessRuleException("RA04", "Doar antrenorul titular poate marca prezenta la clasa " + denumire);
        }
        return sesiune(idProgram).marcheazaPrezenta(idClient, acum);
    }

    /** RA01: capacitatea nu poate scadea sub numarul de locuri deja ocupate la o sesiune viitoare. */
    public void modificaCapacitate(int capacitateNoua, LocalDateTime acum) {
        sesiuni.stream()
                .filter(s -> !s.getInterval().aInceputLa(acum) && s.locuriOcupate() > capacitateNoua)
                .findAny()
                .ifPresent(s -> {
                    throw new BusinessRuleException("RA01", "Sesiunea " + s.getIdProgram() + " are deja "
                            + s.locuriOcupate() + " rezervari");
                });
        this.capacitateMaxima = capacitateNoua;
    }

    /** RA09: schimbarea antrenorului titular (rolul se verifica in DomainValidationService). */
    public void schimbaAntrenor(Integer idAntrenorNou) {
        this.idAntrenor = idAntrenorNou;
    }

    public Integer getIdClasa() {
        return idClasa;
    }

    public String getDenumire() {
        return denumire;
    }

    public Integer getIdAntrenor() {
        return idAntrenor;
    }

    public Integer getCapacitateMaxima() {
        return capacitateMaxima;
    }

    public String getDescriere() {
        return descriere;
    }

    public List<ProgramClasa> getSesiuni() {
        return Collections.unmodifiableList(sesiuni);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ClasaFitness c && idClasa != null && idClasa.equals(c.getIdClasa()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idClasa);
    }
}
