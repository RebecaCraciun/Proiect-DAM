package ro.dam.salafitness.domain.factory;

import ro.dam.salafitness.domain.model.entity.Echipament;

import java.time.LocalDate;

/**
 * Fabrica agregatului Echipament (UC24).
 */
public interface EchipamentFactory {

    Echipament creeazaEchipament(String denumire, LocalDate dataAchizitie, String stareUzura, String locatieInSala);
}
