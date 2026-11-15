package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.model.vo.Bani;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Radacina agregatului Vanzare. Componenta: LinieVanzare (compozitie 1..*, ON DELETE CASCADE - RB02).
 * Invarianti protejati: RA14, RC01, RC02, RC07. Clientul, angajatul si produsul sunt referiti prin id.
 */
@Entity
@Table(name = "vanzari")
public class Vanzare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vanzare")
    private Long idVanzare;

    @NotNull
    @Column(name = "id_utilizator_client", nullable = false)
    private Integer idClient;

    /** RA11 / RB04: angajatul (receptioner) care a efectuat vanzarea */
    @NotNull
    @Column(name = "id_utilizator_angajat", nullable = false)
    private Integer idAngajat;

    @NotNull
    @Column(name = "data_vanzare", nullable = false)
    private LocalDateTime dataVanzare;

    /** RV21 / RC02 */
    @NotNull
    @Valid
    @Embedded
    @AttributeOverride(name = "valoare", column = @Column(name = "total_plata", nullable = false, precision = 10, scale = 2))
    private Bani totalPlata = Bani.ZERO;

    // RB02: liniile se sterg odata cu vanzarea
    @OneToMany(mappedBy = "vanzare", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LinieVanzare> linii = new ArrayList<>();

    protected Vanzare() {
        // JPA
    }

    public Vanzare(Integer idClient, Integer idAngajat, LocalDateTime dataVanzare) {
        this.idClient = idClient;
        this.idAngajat = idAngajat;
        this.dataVanzare = dataVanzare;
    }

    /**
     * UC14 pas 2-3: adauga produsul in vanzare cu pretul curent (RC07) si recalculeaza totalurile (RC01, RC02).
     * RA14: cantitatea cumulata a produsului in vanzare nu poate depasi stocul.
     */
    public LinieVanzare adaugaLinie(Produs produs, int cantitate) {
        if (cantitate <= 0) {
            throw new BusinessRuleException("RV20", "Cantitatea trebuie sa fie > 0");
        }
        int cantitateTotala = cantitateProdus(produs.getIdProdus()) + cantitate;
        if (!produs.areStoc(cantitateTotala)) {
            throw new BusinessRuleException("RA14", "Stoc insuficient pentru " + produs.getDenumire()
                    + " (disponibil " + produs.getStocCurent() + ", cerut " + cantitateTotala + ")");
        }
        Optional<LinieVanzare> existenta = linie(produs.getIdProdus());
        LinieVanzare linie;
        if (existenta.isPresent()) {
            linie = existenta.get();
            linie.majoreazaCantitatea(cantitate);
        } else {
            linie = new LinieVanzare(this, produs.getIdProdus(), cantitate, produs.getPret());
            linii.add(linie);
        }
        calculeazaTotal();
        return linie;
    }

    public void eliminaLinie(Integer idProdus) {
        linii.removeIf(l -> l.getIdProdus().equals(idProdus));
        calculeazaTotal();
    }

    /** RC02: total_plata = suma total_linie. */
    public Bani calculeazaTotal() {
        totalPlata = linii.stream().map(LinieVanzare::recalculeazaTotal).reduce(Bani.ZERO, Bani::plus);
        return totalPlata;
    }

    /** UC14 pas 4 / RA14: o vanzare fara linii nu poate fi finalizata. */
    public Bani finalizeaza() {
        if (linii.isEmpty()) {
            throw new BusinessRuleException("RA14", "Vanzarea nu are nicio linie si nu poate fi finalizata");
        }
        return calculeazaTotal();
    }

    public int cantitateProdus(Integer idProdus) {
        return linie(idProdus).map(LinieVanzare::getCantitate).orElse(0);
    }

    private Optional<LinieVanzare> linie(Integer idProdus) {
        return linii.stream().filter(l -> l.getIdProdus().equals(idProdus)).findFirst();
    }

    public Long getIdVanzare() {
        return idVanzare;
    }

    public Integer getIdClient() {
        return idClient;
    }

    public Integer getIdAngajat() {
        return idAngajat;
    }

    public LocalDateTime getDataVanzare() {
        return dataVanzare;
    }

    public Bani getTotalPlata() {
        return totalPlata;
    }

    public List<LinieVanzare> getLinii() {
        return Collections.unmodifiableList(linii);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Vanzare v && idVanzare != null && idVanzare.equals(v.getIdVanzare()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idVanzare);
    }
}
