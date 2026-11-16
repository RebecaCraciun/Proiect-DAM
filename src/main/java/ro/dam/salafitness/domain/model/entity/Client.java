package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.EntityListeners;
import ro.dam.salafitness.domain.model.listener.AuditEntitateListener;
import ro.dam.salafitness.domain.model.listener.Auditabil;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.Size;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.model.vo.ContactUrgenta;
import ro.dam.salafitness.domain.model.vo.Email;
import ro.dam.salafitness.domain.model.vo.PerioadaValabilitate;
import ro.dam.salafitness.domain.model.vo.Telefon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Radacina agregatului Client. Componente: AbonamentClient, FeedbackClient.
 * Invarianti protejati: RA05, RA06, RC04, RC05, RC10.
 */
@Entity
@Table(name = "client")
@PrimaryKeyJoinColumn(name = "id_utilizator")
@EntityListeners(AuditEntitateListener.class)
public class Client extends Utilizator implements Auditabil {

    /** RC10: denumirea tipului ultimului abonament cumparat */
    @Size(max = 100)
    @Column(name = "tip_abonament", length = 100)
    private String tipAbonament;

    @Size(max = 2000)
    @Column(name = "preferinte_fitness", length = 2000)
    private String preferinteFitness;

    @Valid
    @Embedded
    private ContactUrgenta contactUrgenta;

    // RB01: istoricul nu se sterge
    @OneToMany(mappedBy = "client", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @OrderBy("perioada.dataInceput ASC")
    private List<AbonamentClient> abonamente = new ArrayList<>();

    /** RB02: stergerea clientului ii sterge recenziile */
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FeedbackClient> feedbackuri = new ArrayList<>();

    protected Client() {
        // JPA
    }

    public Client(String nume, String prenume, Email email, String parolaHash, Telefon telefon,
                  LocalDateTime dataInregistrare, String preferinteFitness, ContactUrgenta contactUrgenta) {
        super(nume, prenume, email, parolaHash, telefon, dataInregistrare);
        this.preferinteFitness = preferinteFitness;
        this.contactUrgenta = contactUrgenta;
    }

    @Override
    public boolean esteAngajat() {
        return false;
    }

    /** UC07: abonamentul valabil la data indicata, daca exista. */
    public Optional<AbonamentClient> abonamentActiv(LocalDate data) {
        return abonamente.stream().filter(a -> a.esteValid(data)).findFirst();
    }

    /** RA07 */
    public boolean poateIntra(LocalDate data) {
        return abonamentActiv(data).isPresent();
    }

    /**
     * UC03 pas 3: data curenta sau ziua de dupa expirarea ultimului abonament care inca nu a expirat.
     */
    public LocalDate dataInceputUrmatorAbonament(LocalDate azi) {
        return abonamente.stream()
                .map(a -> a.getPerioada().getDataSfarsit())
                .filter(sfarsit -> !sfarsit.isBefore(azi))
                .max(Comparator.naturalOrder())
                .map(sfarsit -> sfarsit.plusDays(1))
                .orElse(azi);
    }

    /**
     * UC03: creeaza un abonament nou al clientului.
     * RC04/RA06 perioada din durata tipului, RA05 fara suprapuneri, RC05 status, RC10 tipul curent.
     */
    public AbonamentClient cumparaAbonament(TipAbonament tip, LocalDate azi) {
        PerioadaValabilitate perioada = PerioadaValabilitate.dinDurata(dataInceputUrmatorAbonament(azi), tip.getDurataZile());
        verificaSuprapunere(perioada);

        AbonamentClient abonament = new AbonamentClient(this, tip.getIdTipAbonament(), perioada);
        abonament.actualizeazaStatus(azi);
        abonamente.add(abonament);
        this.tipAbonament = tip.getDenumire();
        return abonament;
    }

    /** RA05: perioadele abonamentelor aceluiasi client nu se suprapun. */
    public void verificaSuprapunere(PerioadaValabilitate perioada) {
        abonamente.stream()
                .filter(a -> a.getPerioada().seSuprapuneCu(perioada))
                .findAny()
                .ifPresent(a -> {
                    throw new BusinessRuleException("RA05",
                            "Perioada " + perioada + " se suprapune cu abonamentul existent " + a.getPerioada());
                });
    }

    /** RC05 pentru toate abonamentele clientului; intoarce cate statusuri s-au schimbat. */
    public int actualizeazaStatusAbonamente(LocalDate azi) {
        int modificate = 0;
        for (AbonamentClient abonament : abonamente) {
            if (abonament.actualizeazaStatus(azi)) {
                modificate++;
            }
        }
        return modificate;
    }

    /** RC10: recalculeaza tipul curent din denumirea primita pentru ultimul abonament cumparat. */
    public void actualizeazaTipAbonament(String denumireTip) {
        this.tipAbonament = denumireTip;
    }

    /** UC13 */
    public FeedbackClient adaugaFeedback(Integer rating, String comentariu, LocalDateTime moment) {
        FeedbackClient feedback = new FeedbackClient(this, rating, comentariu, moment);
        feedbackuri.add(feedback);
        return feedback;
    }

    public void actualizeazaProfil(String preferinteFitness, ContactUrgenta contactUrgenta) {
        this.preferinteFitness = preferinteFitness;
        this.contactUrgenta = contactUrgenta;
    }

    /** Ultimul abonament cumparat (dupa data de inceput). */
    public Optional<AbonamentClient> ultimulAbonament() {
        return abonamente.stream().max(Comparator.comparing(a -> a.getPerioada().getDataInceput()));
    }

    @Override
    public Object idAudit() {
        return getIdUtilizator();
    }

    // fara email sau telefon (RB07)
    @Override
    public String detaliiAudit() {
        return tipAbonament == null ? null : "abonament " + tipAbonament;
    }

    public String getTipAbonament() {
        return tipAbonament;
    }

    public String getPreferinteFitness() {
        return preferinteFitness;
    }

    public ContactUrgenta getContactUrgenta() {
        return contactUrgenta;
    }

    public List<AbonamentClient> getAbonamente() {
        return Collections.unmodifiableList(abonamente);
    }

    public List<FeedbackClient> getFeedbackuri() {
        return Collections.unmodifiableList(feedbackuri);
    }
}
