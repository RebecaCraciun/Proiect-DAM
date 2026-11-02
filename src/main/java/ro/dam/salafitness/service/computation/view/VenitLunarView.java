package ro.dam.salafitness.service.computation.view;

import java.math.BigDecimal;
import java.time.YearMonth;

/** V3 - v_venituri_lunare */
public record VenitLunarView(YearMonth luna, BigDecimal venitAbonamente, BigDecimal venitProduse,
                             BigDecimal venitTotal) {
}
