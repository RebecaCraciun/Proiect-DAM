package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ro.dam.salafitness.domain.model.vo.Email;
import ro.dam.salafitness.domain.model.vo.Telefon;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Superclasa abstracta a actorilor umani. Mostenire Class Table Inheritance (JOINED):
 * utilizator &lt;- client, angajat (RB08: specializari disjuncte).
 */
@Entity
@Table(name = "utilizator")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Utilizator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilizator")
    private Integer idUtilizator;

    /** RV02 */
    @NotBlank(message = "Numele este obligatoriu")
    @Size(max = 100, message = "Numele are maximum 100 de caractere")
    @Column(name = "nume", nullable = false, length = 100)
    private String nume;

    /** RV02 */
    @NotBlank(message = "Prenumele este obligatoriu")
    @Size(max = 100, message = "Prenumele are maximum 100 de caractere")
    @Column(name = "prenume", nullable = false, length = 100)
    private String prenume;

    /** RV01 (unicitatea este verificata si de DomainValidationService) */
    @NotNull(message = "Emailul este obligatoriu")
    @Valid
    @Embedded
    @AttributeOverride(name = "adresa", column = @Column(name = "email", nullable = false, unique = true, length = 150))
    private Email email;

    /** RV03: se stocheaza doar hash-ul parolei */
    @NotBlank(message = "Parola este obligatorie")
    @Column(name = "parola", nullable = false)
    private String parola;

    @Valid
    @Embedded
    @AttributeOverride(name = "numar", column = @Column(name = "telefon", length = 20))
    private Telefon telefon;

    /** RV04 */
    @NotNull
    @Column(name = "data_inregistrare", nullable = false, updatable = false)
    private LocalDateTime dataInregistrare;

    protected Utilizator() {
        // JPA
    }

    protected Utilizator(String nume, String prenume, Email email, String parolaHash, Telefon telefon,
                         LocalDateTime dataInregistrare) {
        this.nume = nume;
        this.prenume = prenume;
        this.email = email;
        this.parola = parolaHash;
        this.telefon = telefon;
        this.dataInregistrare = dataInregistrare;
    }

    /** RA07: angajatii au acces in sala fara verificarea abonamentului. */
    public abstract boolean esteAngajat();

    public void actualizeazaDateContact(String nume, String prenume, Telefon telefon) {
        this.nume = nume;
        this.prenume = prenume;
        this.telefon = telefon;
    }

    /** RV03: primeste exclusiv hash-ul noii parole. */
    public void schimbaParola(String parolaHash) {
        this.parola = parolaHash;
    }

    public String getNumeComplet() {
        return nume + " " + prenume;
    }

    public Integer getIdUtilizator() {
        return idUtilizator;
    }

    public String getNume() {
        return nume;
    }

    public String getPrenume() {
        return prenume;
    }

    public Email getEmail() {
        return email;
    }

    public String getParola() {
        return parola;
    }

    public Telefon getTelefon() {
        return telefon;
    }

    public LocalDateTime getDataInregistrare() {
        return dataInregistrare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Utilizator u && idUtilizator != null && idUtilizator.equals(u.getIdUtilizator());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idUtilizator);
    }
}
