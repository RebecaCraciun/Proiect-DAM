package ro.dam.salafitness.service.computation;

import ro.dam.salafitness.service.computation.view.FeedbackLunarView;

import java.util.List;

/**
 * Reguli de calcul pentru clienti (V6).
 */
public interface ClientiComputationService {

    /** V6 */
    List<FeedbackLunarView> feedbackLunar();
}
