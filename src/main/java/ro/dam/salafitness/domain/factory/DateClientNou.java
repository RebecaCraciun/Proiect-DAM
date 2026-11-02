package ro.dam.salafitness.domain.factory;

/**
 * Datele introduse la receptie pentru inregistrarea unui client (UC01). Parola este in clar si se hash-uieste in fabrica.
 */
public record DateClientNou(String nume, String prenume, String email, String parola, String telefon,
                            String preferinteFitness, String contactUrgenta) {
}
