package ro.dam.salafitness.service.computation.view;

import java.math.BigDecimal;
import java.time.YearMonth;

/** V6 - v_feedback_lunar */
public record FeedbackLunarView(YearMonth luna, long nrRecenzii, BigDecimal ratingMediu, long recenziiNegative) {
}
