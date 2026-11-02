package ro.dam.salafitness.domain.factory;

import ro.dam.salafitness.domain.model.entity.Vanzare;

/**
 * Fabrica agregatului Vanzare: deschide o vanzare goala pentru client, la momentul curent (UC14 pas 1).
 */
public interface VanzareFactory {

    Vanzare deschideVanzare(Integer idClient, Integer idAngajat);
}
