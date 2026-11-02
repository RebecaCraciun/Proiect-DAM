package ro.dam.salafitness.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value object pentru intervalul orar al unei sesiuni: data_ora_inceput si data_ora_sfarsit.
 */
@Embeddable
public class IntervalOrar {

    @NotNull(message = "Ora de inceput este obligatorie")
    @Column(name = "data_ora_inceput", nullable = false)
    private LocalDateTime inceput;

    @NotNull(message = "Ora de sfarsit este obligatorie")
    @Column(name = "data_ora_sfarsit", nullable = false)
    private LocalDateTime sfarsit;

    protected IntervalOrar() {
        // JPA
    }

    private IntervalOrar(LocalDateTime inceput, LocalDateTime sfarsit) {
        this.inceput = inceput;
        this.sfarsit = sfarsit;
    }

    public static IntervalOrar intre(LocalDateTime inceput, LocalDateTime sfarsit) {
        return new IntervalOrar(inceput, sfarsit);
    }

    /** RV15: ora de sfarsit este strict dupa ora de inceput. */
    @AssertTrue(message = "Ora de sfarsit trebuie sa fie dupa ora de inceput")
    public boolean isIntervalValid() {
        return inceput == null || sfarsit == null || sfarsit.isAfter(inceput);
    }

    /** RA12: intervalele semi-deschise [inceput, sfarsit) se suprapun. */
    public boolean seSuprapuneCu(IntervalOrar altul) {
        return inceput.isBefore(altul.sfarsit) && altul.inceput.isBefore(sfarsit);
    }

    /** RA03 / RA04: sesiunea a inceput la momentul indicat. */
    public boolean aInceputLa(LocalDateTime moment) {
        return !moment.isBefore(inceput);
    }

    public LocalDateTime getInceput() {
        return inceput;
    }

    public LocalDateTime getSfarsit() {
        return sfarsit;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof IntervalOrar i
                && Objects.equals(inceput, i.inceput) && Objects.equals(sfarsit, i.sfarsit));
    }

    @Override
    public int hashCode() {
        return Objects.hash(inceput, sfarsit);
    }

    @Override
    public String toString() {
        return inceput + " - " + sfarsit;
    }
}
