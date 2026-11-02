package ro.dam.salafitness.service.workflow;

import ro.dam.salafitness.domain.model.entity.Vanzare;

import java.util.List;

/**
 * UC14 Vanzare produse.
 */
public interface VanzareWorkflowService {

    /** Un produs cerut de client si cantitatea lui. */
    record LinieComanda(Integer idProdus, int cantitate) {
    }

    /**
     * UC14: RV19-RV21, RA11, RA14, RC01, RC02, RC07, RC08. Vanzarea, liniile si scaderea stocurilor se fac
     * intr-o singura tranzactie: daca un produs nu are stoc (2a), nicio linie nu se salveaza.
     */
    Vanzare vindeProduse(Integer idClient, Integer idReceptioner, List<LinieComanda> linii);
}
