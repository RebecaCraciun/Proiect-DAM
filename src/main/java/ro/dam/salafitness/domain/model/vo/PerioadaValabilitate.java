package ro.dam.salafitness.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value object pentru perioada unui abonament: coloanele data_inceput si data_sfarsit.
 */
@Embeddable
public class PerioadaValabilitate {

    @NotNull(message = "Data de inceput este obligatorie")
    @Column(name = "data_inceput", nullable = false)
    private LocalDate dataInceput;

    @NotNull(message = "Data de sfarsit este obligatorie")
    @Column(name = "data_sfarsit", nullable = false)
    private LocalDate dataSfarsit;

    protected PerioadaValabilitate() {
        // JPA
    }

    private PerioadaValabilitate(LocalDate dataInceput, LocalDate dataSfarsit) {
        this.dataInceput = dataInceput;
        this.dataSfarsit = dataSfarsit;
    }

    public static PerioadaValabilitate intre(LocalDate dataInceput, LocalDate dataSfarsit) {
        return new PerioadaValabilitate(dataInceput, dataSfarsit);
    }

    /** RC04 / RA06: data_sfarsit = data_inceput + durata_zile - 1. */
    public static PerioadaValabilitate dinDurata(LocalDate dataInceput, int durataZile) {
        if (durataZile <= 0) {
            throw new IllegalArgumentException("Durata abonamentului trebuie sa fie > 0 zile");
        }
        return new PerioadaValabilitate(dataInceput, dataInceput.plusDays(durataZile - 1L));
    }

    /** RV09: data de sfarsit nu poate fi inaintea datei de inceput. */
    @AssertTrue(message = "Data de sfarsit nu poate fi inaintea datei de inceput")
    public boolean isIntervalValid() {
        return dataInceput == null || dataSfarsit == null || !dataSfarsit.isBefore(dataInceput);
    }

    public boolean contine(LocalDate data) {
        return !data.isBefore(dataInceput) && !data.isAfter(dataSfarsit);
    }

    /** RA05: doua perioade se suprapun daca au cel putin o zi comuna. */
    public boolean seSuprapuneCu(PerioadaValabilitate alta) {
        return !dataInceput.isAfter(alta.dataSfarsit) && !alta.dataInceput.isAfter(dataSfarsit);
    }

    /** Zilele ramase incepand cu data indicata (v_abonamente_active.zile_ramase). */
    public long zileRamase(LocalDate data) {
        return ChronoUnit.DAYS.between(data, dataSfarsit);
    }

    public long durataZile() {
        return ChronoUnit.DAYS.between(dataInceput, dataSfarsit) + 1;
    }

    public LocalDate getDataInceput() {
        return dataInceput;
    }

    public LocalDate getDataSfarsit() {
        return dataSfarsit;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof PerioadaValabilitate p
                && Objects.equals(dataInceput, p.dataInceput) && Objects.equals(dataSfarsit, p.dataSfarsit));
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataInceput, dataSfarsit);
    }

    @Override
    public String toString() {
        return dataInceput + " .. " + dataSfarsit;
    }
}
