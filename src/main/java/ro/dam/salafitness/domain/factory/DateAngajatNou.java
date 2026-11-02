package ro.dam.salafitness.domain.factory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datele unui angajat nou (UC19). Parola este in clar si se hash-uieste in fabrica.
 */
public record DateAngajatNou(String nume, String prenume, String email, String parola, String telefon,
                             Integer idTipAngajat, String functie, BigDecimal salariu, LocalDate dataAngajarii,
                             String programLucru) {
}
