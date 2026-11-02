package ro.dam.salafitness.service.computation.view;

import java.math.BigDecimal;
import java.time.YearMonth;

/** V4 - v_ore_lucrate_lunar */
public record OreLucrateLunarView(Integer idUtilizator, String angajat, String tipAngajat, YearMonth luna,
                                  long zilePontate, BigDecimal totalOre) {
}
