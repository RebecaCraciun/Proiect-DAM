package ro.dam.salafitness.domain.factory;

import ro.dam.salafitness.domain.model.entity.Angajat;

/**
 * Fabrica agregatului Angajat: produce un angajat valid (RV01-RV06), fara pontaje si salarii.
 */
public interface AngajatFactory {

    Angajat creeazaAngajat(DateAngajatNou date);
}
