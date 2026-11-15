package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EntityListeners;
import ro.dam.salafitness.domain.model.listener.AuditEntitateListener;
import ro.dam.salafitness.domain.model.listener.Auditabil;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ro.dam.salafitness.domain.model.enums.MetodaPlata;
import ro.dam.salafitness.domain.model.vo.Bani;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Agregat cu o singura entitate: incasarea unei sume de la un client, inregistrata de un angajat (RB04).
 */
@Entity
@Table(name = "plati")
@EntityListeners(AuditEntitateListener.class)
public class Plata implements Auditabil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plata")
    private Long idPlata;

    @NotNull
    @Column(name = "id_utilizator_client", nullable = false)
    private Integer idClient;

    /** RA11 / RB04 */
    @NotNull
    @Column(name = "id_utilizator_angajat", nullable = false)
    private Integer idAngajat;

    /** RV13 / RC06 */
    @NotNull
    @Valid
    @Embedded
    @AttributeOverride(name = "valoare", column = @Column(name = "suma", nullable = false, precision = 10, scale = 2))
    private Bani suma;

    /** RV13 */
    @NotNull(message = "Metoda de plata este obligatorie")
    @Convert(converter = MetodaPlata.Converter.class)
    @Column(name = "metoda_plata", nullable = false, length = 20)
    private MetodaPlata metodaPlata;

    @NotNull
    @Column(name = "data_plata", nullable = false)
    private LocalDateTime dataPlata;

    protected Plata() {
        // JPA
    }

    public Plata(Integer idClient, Integer idAngajat, Bani suma, MetodaPlata metodaPlata, LocalDateTime dataPlata) {
        this.idClient = idClient;
        this.idAngajat = idAngajat;
        this.suma = suma;
        this.metodaPlata = metodaPlata;
        this.dataPlata = dataPlata;
    }

    public Long getIdPlata() {
        return idPlata;
    }

    public Integer getIdClient() {
        return idClient;
    }

    public Integer getIdAngajat() {
        return idAngajat;
    }

    public Bani getSuma() {
        return suma;
    }

    public MetodaPlata getMetodaPlata() {
        return metodaPlata;
    }

    public LocalDateTime getDataPlata() {
        return dataPlata;
    }

    @Override
    public Object idAudit() {
        return idPlata;
    }

    @Override
    public String detaliiAudit() {
        return suma + " " + (metodaPlata == null ? "" : metodaPlata.getValoare()) + ", client " + idClient
                + ", angajat " + idAngajat;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Plata p && idPlata != null && idPlata.equals(p.getIdPlata()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idPlata);
    }
}
