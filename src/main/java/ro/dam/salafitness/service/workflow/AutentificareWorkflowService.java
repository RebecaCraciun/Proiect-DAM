package ro.dam.salafitness.service.workflow;

import ro.dam.salafitness.domain.model.entity.Utilizator;

/**
 * UC02: autentificarea oricarui utilizator (client sau angajat) cu email si parola.
 */
public interface AutentificareWorkflowService {

    /** RV03: parola primita se compara cu hash-ul stocat; acelasi mesaj pentru email sau parola gresite. */
    Utilizator autentifica(String email, String parola);
}
