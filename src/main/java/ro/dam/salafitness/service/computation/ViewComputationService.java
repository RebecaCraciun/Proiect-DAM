package ro.dam.salafitness.service.computation;

import ro.dam.salafitness.service.computation.view.AbonamentActivView;
import ro.dam.salafitness.service.computation.view.CostMentenantaView;
import ro.dam.salafitness.service.computation.view.FeedbackLunarView;
import ro.dam.salafitness.service.computation.view.OcupareClasaView;
import ro.dam.salafitness.service.computation.view.OreLucrateLunarView;
import ro.dam.salafitness.service.computation.view.VenitLunarView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Reguli de calcul pentru view-urile calculate (README Anexa A, V1-V6), implementate in aplicatie
 * pentru a fi independente de dialectul SQL.
 */
public interface ViewComputationService {

    /** V1 */
    List<AbonamentActivView> abonamenteActive(LocalDate data);

    /** V2 pentru toate sesiunile */
    List<OcupareClasaView> ocupareClase();

    /** V2 restrans la sesiunile care incep in [de, pana) - UC08 */
    List<OcupareClasaView> ocupareClase(LocalDateTime de, LocalDateTime pana);

    /** V3 */
    List<VenitLunarView> venituriLunare();

    /** V4 */
    List<OreLucrateLunarView> oreLucrateLunar();

    /** V5 */
    List<CostMentenantaView> costMentenantaEchipamente();

    /** V6 */
    List<FeedbackLunarView> feedbackLunar();
}
