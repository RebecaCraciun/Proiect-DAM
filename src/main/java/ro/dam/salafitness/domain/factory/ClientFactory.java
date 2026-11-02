package ro.dam.salafitness.domain.factory;

import ro.dam.salafitness.domain.model.entity.Client;

/**
 * Fabrica agregatului Client: produce un client valid (RV01-RV04), fara abonamente.
 */
public interface ClientFactory {

    Client creeazaClient(DateClientNou date);
}
