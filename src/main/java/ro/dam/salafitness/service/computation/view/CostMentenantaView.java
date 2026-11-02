package ro.dam.salafitness.service.computation.view;

import java.math.BigDecimal;
import java.time.LocalDate;

/** V5 - v_cost_mentenanta_echipament */
public record CostMentenantaView(Integer idEchipament, String denumire, String stareUzura, int nrInterventii,
                                 BigDecimal costTotal, LocalDate ultimaVerificare) {
}
