package ro.dam.salafitness.service.rules;

/**
 * RB01 / RB02: stergerea entitatilor este permisa doar daca nu sunt referite de istoric financiar sau
 * operational (echivalentul ON DELETE RESTRICT); componentele fara sens in afara parintelui se sterg in cascada.
 */
public interface RetentieIstoricService {

    /** RB02: sterge clientul impreuna cu recenziile; RB01: refuza daca are plati, vanzari, rezervari etc. */
    void stergeClient(Integer idClient, Integer idManager);

    /** RB01: refuza daca angajatul are pontaje, salarii, plati, vanzari, clase, sedinte sau interventii. */
    void stergeAngajat(Integer idAngajat, Integer idManager);

    /** RB01: refuza daca produsul apare in linii de vanzare. */
    void stergeProdus(Integer idProdus, Integer idManager);

    /** RB01: refuza daca exista abonamente cumparate de acest tip. */
    void stergeTipAbonament(Integer idTipAbonament, Integer idManager);
}
