package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ro.dam.salafitness.domain.model.vo.Bani;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Componenta a agregatului Echipament: o verificare sau reparatie.
 */
@Entity
@Table(name = "mentenanta_echipamente")
public class MentenantaEchipament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mentenanta")
    private Long idMentenanta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_echipament", nullable = false)
    private Echipament echipament;

    @NotNull(message = "Data verificarii este obligatorie")
    @Column(name = "data_verificare", nullable = false)
    private LocalDate dataVerificare;

    @Size(max = 2000)
    @Column(name = "descriere_problema", length = 2000)
    private String descriereProblema;

    /** RV22 */
    @NotNull
    @Valid
    @Embedded
    @AttributeOverride(name = "valoare", column = @Column(name = "cost", nullable = false, precision = 10, scale = 2))
    private Bani cost = Bani.ZERO;

    /** RA10 / RB04 */
    @NotNull
    @Column(name = "id_angajat_responsabil", nullable = false)
    private Integer idAngajatResponsabil;

    protected MentenantaEchipament() {
        // JPA
    }

    MentenantaEchipament(Echipament echipament, LocalDate dataVerificare, String descriereProblema, Bani cost,
                         Integer idAngajatResponsabil) {
        this.echipament = echipament;
        this.dataVerificare = dataVerificare;
        this.descriereProblema = descriereProblema;
        this.cost = cost;
        this.idAngajatResponsabil = idAngajatResponsabil;
    }

    public Long getIdMentenanta() {
        return idMentenanta;
    }

    public Echipament getEchipament() {
        return echipament;
    }

    public LocalDate getDataVerificare() {
        return dataVerificare;
    }

    public String getDescriereProblema() {
        return descriereProblema;
    }

    public Bani getCost() {
        return cost;
    }

    public Integer getIdAngajatResponsabil() {
        return idAngajatResponsabil;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof MentenantaEchipament m && idMentenanta != null
                && idMentenanta.equals(m.getIdMentenanta()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idMentenanta);
    }
}
