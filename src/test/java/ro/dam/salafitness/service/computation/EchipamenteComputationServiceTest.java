package ro.dam.salafitness.service.computation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.Echipament;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.repository.EchipamentRepository;
import ro.dam.salafitness.service.computation.view.CostMentenantaView;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Reguli de calcul pentru echipamente (V5).
 */
class EchipamenteComputationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private EchipamenteComputationService echipamenteComputationService;
    @Autowired
    private EchipamentRepository echipamentRepository;

    @Test
    void costMentenanta_V5_ordonatDescrescator() {
        Echipament banda = echipamentRepository.save(new Echipament("Banda", AZI.minusYears(2), "Uzat", "Zona cardio"));
        Echipament gantere = echipamentRepository.save(new Echipament("Gantere", AZI.minusYears(1), "Nou", "Zona libera"));
        banda.inregistreazaMentenanta(AZI.minusDays(10), "Curea", Bani.of("100"), tehnician.getIdUtilizator(), null);
        banda.inregistreazaMentenanta(AZI, "Motor", Bani.of("50.50"), tehnician.getIdUtilizator(), "Reparat");
        echipamentRepository.flush();

        List<CostMentenantaView> costuri = echipamenteComputationService.costMentenantaEchipamente();

        assertEquals(List.of("Banda", "Gantere"), costuri.stream().map(CostMentenantaView::denumire).toList());
        assertEquals(new BigDecimal("150.50"), costuri.get(0).costTotal());
        assertEquals(2, costuri.get(0).nrInterventii());
        assertEquals(AZI, costuri.get(0).ultimaVerificare());
        assertEquals("Reparat", costuri.get(0).stareUzura());
        assertEquals(0, costuri.get(1).nrInterventii());
        assertEquals(gantere.getIdEchipament(), costuri.get(1).idEchipament());
    }
}
