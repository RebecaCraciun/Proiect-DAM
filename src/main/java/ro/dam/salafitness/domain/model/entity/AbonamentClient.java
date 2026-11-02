package ro.dam.salafitness.domain.model.entity;

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
import ro.dam.salafitness.domain.model.enums.StatusAbonament;
import ro.dam.salafitness.domain.model.vo.PerioadaValabilitate;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Componenta a agregatului Client. Tipul de abonament (alt agregat) este referit doar prin id.
 */
@Entity
@Table(name = "abonamente_clienti")
public class AbonamentClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_abonament_client")
    private Long idAbonamentClient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilizator_client", nullable = false)
    private Client client;

    @NotNull(message = "Tipul abonamentului este obligatoriu")
    @Column(name = "id_tip_abonament", nullable = false)
    private Integer idTipAbonament;

    /** RV09 */
    @NotNull
    @Valid
    @Embedded
    private PerioadaValabilitate perioada;

    /** RV10 */
    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    private StatusAbonament status;

    protected AbonamentClient() {
        // JPA
    }

    AbonamentClient(Client client, Integer idTipAbonament, PerioadaValabilitate perioada) {
        this.client = client;
        this.idTipAbonament = idTipAbonament;
        this.perioada = perioada;
        this.status = StatusAbonament.ACTIV;
    }

    /** RA07 / UC07: data_inceput &le; data &le; data_sfarsit. */
    public boolean esteValid(LocalDate data) {
        return perioada.contine(data);
    }

    /** RC05: "Activ" daca data curenta &le; data_sfarsit, altfel "Expirat". Intoarce true daca s-a schimbat. */
    public boolean actualizeazaStatus(LocalDate azi) {
        StatusAbonament nou = azi.isAfter(perioada.getDataSfarsit()) ? StatusAbonament.EXPIRAT : StatusAbonament.ACTIV;
        boolean schimbat = nou != status;
        status = nou;
        return schimbat;
    }

    public Long getIdAbonamentClient() {
        return idAbonamentClient;
    }

    public Client getClient() {
        return client;
    }

    public Integer getIdTipAbonament() {
        return idTipAbonament;
    }

    public PerioadaValabilitate getPerioada() {
        return perioada;
    }

    public StatusAbonament getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof AbonamentClient a && idAbonamentClient != null
                && idAbonamentClient.equals(a.getIdAbonamentClient()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idAbonamentClient);
    }
}
