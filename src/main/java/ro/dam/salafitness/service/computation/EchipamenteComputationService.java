package ro.dam.salafitness.service.computation;

import ro.dam.salafitness.service.computation.view.CostMentenantaView;

import java.util.List;

/**
 * Reguli de calcul pentru echipamente (V5).
 */
public interface EchipamenteComputationService {

    /** V5 */
    List<CostMentenantaView> costMentenantaEchipamente();
}
