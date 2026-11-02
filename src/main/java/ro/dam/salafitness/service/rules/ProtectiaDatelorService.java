package ro.dam.salafitness.service.rules;

/**
 * RB07: datele personale (telefon, contact de urgenta) sunt accesibile doar personalului autorizat
 * (Receptioner, Manager) si titularului; celorlalti li se afiseaza mascate.
 */
public interface ProtectiaDatelorService {

    record DateContact(Integer idClient, String numeComplet, String telefon, String contactUrgenta, boolean mascat) {
    }

    boolean esteAutorizat(Integer idSolicitant, Integer idClient);

    DateContact dateContactClient(Integer idClient, Integer idSolicitant);
}
