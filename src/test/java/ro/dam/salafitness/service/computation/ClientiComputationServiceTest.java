package ro.dam.salafitness.service.computation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.service.computation.view.FeedbackLunarView;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Reguli de calcul pentru clienti (V6).
 */
class ClientiComputationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ClientiComputationService clientiComputationService;

    @Test
    void feedbackLunar_V6_medieSiRecenziiNegative() {
        Client ana = client("ana@x.ro");
        ana.adaugaFeedback(5, "Super", ACUM);
        ana.adaugaFeedback(1, "Aglomerat", ACUM);
        ana.adaugaFeedback(4, null, ACUM);
        clientRepository.flush();

        FeedbackLunarView octombrie = clientiComputationService.feedbackLunar().get(0);

        assertEquals(YearMonth.of(2026, 10), octombrie.luna());
        assertEquals(3, octombrie.nrRecenzii());
        assertEquals(new BigDecimal("3.33"), octombrie.ratingMediu());
        assertEquals(1, octombrie.recenziiNegative());
    }
}
