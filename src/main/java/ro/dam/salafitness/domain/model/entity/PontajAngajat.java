package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EntityListeners;
import ro.dam.salafitness.domain.model.listener.CampuriCalculateListener;
import ro.dam.salafitness.domain.model.listener.CuCampuriCalculate;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import ro.dam.salafitness.domain.exception.BusinessRuleException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Componenta a agregatului Angajat: o tura (check-in / check-out).
 */
@Entity
@Table(name = "pontaj_angajati")
@EntityListeners(CampuriCalculateListener.class)
public class PontajAngajat implements CuCampuriCalculate {

    private static final BigDecimal SECUNDE_PE_ORA = BigDecimal.valueOf(3600);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pontaj")
    private Long idPontaj;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilizator_angajat", nullable = false)
    private Angajat angajat;

    @NotNull
    @Column(name = "data_ora_check_in", nullable = false)
    private LocalDateTime checkIn;

    @Column(name = "data_ora_check_out")
    private LocalDateTime checkOut;

    /** RV11 / RC03 */
    @DecimalMin(value = "0.00", message = "Orele lucrate trebuie sa fie >= 0")
    @Column(name = "ore_lucrate", precision = 5, scale = 2)
    private BigDecimal oreLucrate;

    protected PontajAngajat() {
        // JPA
    }

    PontajAngajat(Angajat angajat, LocalDateTime checkIn) {
        this.angajat = angajat;
        this.checkIn = checkIn;
    }

    /** RV11: check-out &ge; check-in sau necompletat. */
    @AssertTrue(message = "Check-out-ul nu poate fi inaintea check-in-ului")
    public boolean isIntervalValid() {
        return checkOut == null || checkIn == null || !checkOut.isBefore(checkIn);
    }

    public boolean esteDeschis() {
        return checkOut == null;
    }

    void inchide(LocalDateTime momentCheckOut) {
        if (momentCheckOut.isBefore(checkIn)) {
            throw new BusinessRuleException("RV11", "Check-out-ul nu poate fi inaintea check-in-ului");
        }
        this.checkOut = momentCheckOut;
        recalculeazaOreLucrate();
    }

    /** RC03: (check_out - check_in) in ore, rotunjit la 2 zecimale. */
    public BigDecimal recalculeazaOreLucrate() {
        oreLucrate = checkOut == null ? null
                : BigDecimal.valueOf(Duration.between(checkIn, checkOut).toSeconds())
                        .divide(SECUNDE_PE_ORA, 2, RoundingMode.HALF_UP);
        return oreLucrate;
    }

    public Long getIdPontaj() {
        return idPontaj;
    }

    public Angajat getAngajat() {
        return angajat;
    }

    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    public LocalDateTime getCheckOut() {
        return checkOut;
    }

    public BigDecimal getOreLucrate() {
        return oreLucrate;
    }

    // RC03
    @Override
    public void recalculeazaCampuriCalculate() {
        recalculeazaOreLucrate();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof PontajAngajat p && idPontaj != null && idPontaj.equals(p.getIdPontaj()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idPontaj);
    }
}
