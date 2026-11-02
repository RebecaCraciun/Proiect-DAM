package ro.dam.salafitness.domain.factory;

import ro.dam.salafitness.domain.model.entity.ClasaFitness;

/**
 * Fabrica agregatului Clasa Fitness (RV14). Rolul antrenorului (RA09) il verifica serviciul de workflow.
 */
public interface ClasaFitnessFactory {

    ClasaFitness creeazaClasa(String denumire, Integer idAntrenor, int capacitateMaxima, String descriere);
}
