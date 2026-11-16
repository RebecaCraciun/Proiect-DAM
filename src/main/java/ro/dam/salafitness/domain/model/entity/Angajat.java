package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.EntityListeners;
import ro.dam.salafitness.domain.model.listener.AuditEntitateListener;
import ro.dam.salafitness.domain.model.listener.Auditabil;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.model.vo.Email;
import ro.dam.salafitness.domain.model.vo.Telefon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Radacina agregatului Angajat. Componente: PontajAngajat, SalariuIstoric (istoric pastrat - RB01).
 * Invarianti protejati: RA08, RC03, RC11, RB05.
 */
@Entity
@Table(name = "angajat")
@PrimaryKeyJoinColumn(name = "id_utilizator")
@EntityListeners(AuditEntitateListener.class)
public class Angajat extends Utilizator implements Auditabil {

    /** RV06; tipul de angajat (nomenclator) este referit prin id */
    @NotNull(message = "Tipul de angajat este obligatoriu")
    @Column(name = "id_tip_angajat", nullable = false)
    private Integer idTipAngajat;

    /** RV06 */
    @NotBlank(message = "Functia este obligatorie")
    @Size(max = 100)
    @Column(name = "functie", nullable = false, length = 100)
    private String functie;

    /** RV05 */
    @NotNull
    @Valid
    @Embedded
    @AttributeOverride(name = "valoare", column = @Column(name = "salariu", nullable = false, precision = 10, scale = 2))
    private Bani salariu = Bani.ZERO;

    /** RV06 */
    @NotNull(message = "Data angajarii este obligatorie")
    @Column(name = "data_angajarii", nullable = false)
    private LocalDate dataAngajarii;

    @Size(max = 100)
    @Column(name = "program_lucru", length = 100)
    private String programLucru;

    // RB01: istoricul nu se sterge
    @OneToMany(mappedBy = "angajat", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<PontajAngajat> pontaje = new ArrayList<>();

    // RB01: istoricul nu se sterge
    @OneToMany(mappedBy = "angajat", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<SalariuIstoric> salarii = new ArrayList<>();

    protected Angajat() {
        // JPA
    }

    public Angajat(String nume, String prenume, Email email, String parolaHash, Telefon telefon,
                   LocalDateTime dataInregistrare, Integer idTipAngajat, String functie, Bani salariu,
                   LocalDate dataAngajarii, String programLucru) {
        super(nume, prenume, email, parolaHash, telefon, dataInregistrare);
        this.idTipAngajat = idTipAngajat;
        this.functie = functie;
        this.salariu = salariu == null ? Bani.ZERO : salariu;
        this.dataAngajarii = dataAngajarii;
        this.programLucru = programLucru;
    }

    @Override
    public boolean esteAngajat() {
        return true;
    }

    public Optional<PontajAngajat> pontajDeschis() {
        return pontaje.stream().filter(PontajAngajat::esteDeschis).findFirst();
    }

    public boolean existaPontajDeschis() {
        return pontajDeschis().isPresent();
    }

    /** UC17 / RA08: nu se poate face check-in cat timp exista un pontaj deschis. */
    public PontajAngajat checkIn(LocalDateTime moment) {
        if (existaPontajDeschis()) {
            throw new BusinessRuleException("RA08", "Check-out lipsa: angajatul are deja un pontaj deschis");
        }
        PontajAngajat pontaj = new PontajAngajat(this, moment);
        pontaje.add(pontaj);
        return pontaj;
    }

    /** UC17 / RC03: inchide pontajul deschis si calculeaza orele lucrate. */
    public PontajAngajat checkOut(LocalDateTime moment) {
        PontajAngajat pontaj = pontajDeschis()
                .orElseThrow(() -> new BusinessRuleException("RA08", "Nu exista un pontaj deschis pentru check-out"));
        pontaj.inchide(moment);
        return pontaj;
    }

    /** UC18: totalul orelor din pontajele inchise cu check-in in luna data. */
    public BigDecimal oreLucrate(YearMonth luna) {
        return pontaje.stream()
                .filter(p -> !p.esteDeschis() && YearMonth.from(p.getCheckIn()).equals(luna))
                .map(PontajAngajat::getOreLucrate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean areSalariuGenerat(YearMonth luna) {
        return salarii.stream().anyMatch(s -> YearMonth.from(s.getLunaAn()).equals(luna));
    }

    /** UC20 / RC11: suma platita este salariul valabil la generare (snapshot). */
    public SalariuIstoric genereazaSalariu(YearMonth luna) {
        if (areSalariuGenerat(luna)) {
            throw new BusinessRuleException("RB05", "Salariul pentru " + luna + " a fost deja generat");
        }
        SalariuIstoric plata = new SalariuIstoric(this, salariu, luna.atDay(1));
        salarii.add(plata);
        return plata;
    }

    /** UC20 / RB05 */
    public SalariuIstoric confirmaPlataSalariu(Long idPlata, LocalDate dataPlatii) {
        SalariuIstoric plata = salarii.stream()
                .filter(s -> idPlata.equals(s.getIdPlata()))
                .findFirst()
                .orElseThrow(() -> new EntitateNegasitaException("SalariuIstoric", idPlata));
        plata.marcheazaAchitat(dataPlatii);
        return plata;
    }

    /** UC19 / RC11: modificarea salariului nu recalculeaza platile deja generate. */
    public void modificaSalariu(Bani salariuNou) {
        this.salariu = salariuNou;
    }

    public void schimbaFunctia(Integer idTipAngajat, String functie, String programLucru) {
        this.idTipAngajat = idTipAngajat;
        this.functie = functie;
        this.programLucru = programLucru;
    }

    @Override
    public Object idAudit() {
        return getIdUtilizator();
    }

    @Override
    public String detaliiAudit() {
        return functie + ", tip " + idTipAngajat + ", salariu " + salariu;
    }

    public Integer getIdTipAngajat() {
        return idTipAngajat;
    }

    public String getFunctie() {
        return functie;
    }

    public Bani getSalariu() {
        return salariu;
    }

    public LocalDate getDataAngajarii() {
        return dataAngajarii;
    }

    public String getProgramLucru() {
        return programLucru;
    }

    public List<PontajAngajat> getPontaje() {
        return Collections.unmodifiableList(pontaje);
    }

    public List<SalariuIstoric> getSalarii() {
        return Collections.unmodifiableList(salarii);
    }
}
