package ro.dam.salafitness.domain.model.enums;

/**
 * Enumerare persistata ca VARCHAR cu valoarea exacta din constrangerea CHECK a bazei de date
 * (de exemplu 'În așteptare'), nu cu numele constantei Java.
 */
public interface ValoareBD {

    String getValoare();
}
