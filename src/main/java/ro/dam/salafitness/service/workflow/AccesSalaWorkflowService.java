package ro.dam.salafitness.service.workflow;

import ro.dam.salafitness.domain.model.entity.AccesSala;

/**
 * UC06 Acces in sala, cu UC07 Validare abonament activ inclus.
 */
public interface AccesSalaWorkflowService {

    /** UC06 pasii 1-3: RA07 (angajatii intra fara abonament), RV18. Si incercarile respinse se inregistreaza. */
    AccesSala inregistreazaIntrare(Integer idUtilizator);

    /** UC06 pasul 4: RV18 */
    AccesSala inregistreazaIesire(Long idAcces);
}
