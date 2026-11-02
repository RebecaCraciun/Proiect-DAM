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
import jakarta.validation.constraints.Size;
import ro.dam.salafitness.domain.model.vo.Bani;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Radacina agregatului Echipament. Componenta: MentenantaEchipament (istoric pastrat - RB01).
 * Tehnicianul responsabil (agregatul Angajat) este referit prin id; rolul (RA10) il verifica DomainValidationService.
 */
@Entity
@Table(name = "echipamente_sala")
public class Echipament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_echipament")
    private Integer idEchipament;

    @NotBlank(message = "Denumirea echipamentului este obligatorie")
    @Size(max = 100)
    @Column(name = "denumire", nullable = false, length = 100)
    private String denumire;

    @Column(name = "data_achizitie")
    private LocalDate dataAchizitie;

    @Size(max = 50)
    @Column(name = "stare_uzura", length = 50)
    private String stareUzura;

    @Size(max = 100)
    @Column(name = "locatie_in_sala", length = 100)
    private String locatieInSala;

    @OneToMany(mappedBy = "echipament", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<MentenantaEchipament> interventii = new ArrayList<>();

    protected Echipament() {
        // JPA
    }

    public Echipament(String denumire, LocalDate dataAchizitie, String stareUzura, String locatieInSala) {
        this.denumire = denumire;
        this.dataAchizitie = dataAchizitie;
        this.stareUzura = stareUzura;
        this.locatieInSala = locatieInSala;
    }

    /** UC25: inregistreaza o interventie; daca se indica, actualizeaza si starea de uzura. */
    public MentenantaEchipament inregistreazaMentenanta(LocalDate dataVerificare, String descriereProblema, Bani cost,
                                                        Integer idTehnician, String stareNoua) {
        MentenantaEchipament interventie = new MentenantaEchipament(this, dataVerificare, descriereProblema,
                cost == null ? Bani.ZERO : cost, idTehnician);
        interventii.add(interventie);
        if (stareNoua != null && !stareNoua.isBlank()) {
            this.stareUzura = stareNoua;
        }
        return interventie;
    }

    /** v_cost_mentenanta_echipament.cost_total */
    public Bani costTotalMentenanta() {
        return interventii.stream().map(MentenantaEchipament::getCost).reduce(Bani.ZERO, Bani::plus);
    }

    /** v_cost_mentenanta_echipament.ultima_verificare */
    public Optional<LocalDate> ultimaVerificare() {
        return interventii.stream().map(MentenantaEchipament::getDataVerificare).max(Comparator.naturalOrder());
    }

    /** UC24 */
    public void muta(String locatieNoua) {
        this.locatieInSala = locatieNoua;
    }

    public void actualizeazaStare(String stareNoua) {
        this.stareUzura = stareNoua;
    }

    public Integer getIdEchipament() {
        return idEchipament;
    }

    public String getDenumire() {
        return denumire;
    }

    public LocalDate getDataAchizitie() {
        return dataAchizitie;
    }

    public String getStareUzura() {
        return stareUzura;
    }

    public String getLocatieInSala() {
        return locatieInSala;
    }

    public List<MentenantaEchipament> getInterventii() {
        return Collections.unmodifiableList(interventii);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Echipament e && idEchipament != null && idEchipament.equals(e.getIdEchipament()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idEchipament);
    }
}
