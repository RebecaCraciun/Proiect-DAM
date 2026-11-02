package ro.dam.salafitness.service.workflow;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.Echipament;
import ro.dam.salafitness.domain.model.entity.MentenantaEchipament;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.service.audit.AuditService;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * UC24 Gestionare echipamente, UC25 Inregistrare mentenanta: RA10, RV22, RB04.
 */
class EchipamentWorkflowServiceTest extends AbstractIntegrationTest {

    @Autowired
    private EchipamentWorkflowService echipamentWorkflow;
    @Autowired
    private AuditService auditService;

    private Echipament banda() {
        return echipamentWorkflow.inregistreazaEchipament("Banda de alergare", AZI.minusYears(2), "Bun",
                "Zona cardio", manager.getIdUtilizator());
    }

    @Test
    void inregistrareSiMutare_deManagerSauTehnician_UC24() {
        Echipament banda = banda();
        assertNotNull(banda.getIdEchipament());

        echipamentWorkflow.mutaEchipament(banda.getIdEchipament(), "Etaj 1", tehnician.getIdUtilizator());

        assertEquals("Etaj 1", banda.getLocatieInSala());
        assertRegula("UC24", () -> echipamentWorkflow.mutaEchipament(banda.getIdEchipament(), "Hol",
                receptioner.getIdUtilizator()));
    }

    @Test
    void mentenanta_UC25_actualizeazaStareaSiRetineTehnicianul() {
        Echipament banda = banda();

        MentenantaEchipament interventie = echipamentWorkflow.inregistreazaMentenanta(banda.getIdEchipament(),
                tehnician.getIdUtilizator(), "Curea uzata", new BigDecimal("250"), "Reparat");

        assertNotNull(interventie.getIdMentenanta());
        assertEquals(AZI, interventie.getDataVerificare());
        assertEquals(Bani.of("250"), interventie.getCost());
        assertEquals(tehnician.getIdUtilizator(), interventie.getIdAngajatResponsabil()); // RB04
        assertEquals("Reparat", banda.getStareUzura());
        assertEquals(1, auditService.istoricUtilizator(tehnician.getIdUtilizator()).size());
    }

    @Test
    void mentenanta_faraCostEsteZero_RV22() {
        MentenantaEchipament verificare = echipamentWorkflow.inregistreazaMentenanta(banda().getIdEchipament(),
                tehnician.getIdUtilizator(), "Verificare periodica", null, null);

        assertEquals(Bani.ZERO, verificare.getCost());
    }

    @Test
    void mentenanta_responsabilulTrebuieSaFieTehnician_RA10() {
        Integer idBanda = banda().getIdEchipament();

        assertRegula("RA10", () -> echipamentWorkflow.inregistreazaMentenanta(idBanda, antrenor.getIdUtilizator(),
                "Scartaie", BigDecimal.ZERO, null));
    }

    @Test
    void mentenanta_costNegativ_RV22() {
        Integer idBanda = banda().getIdEchipament();

        assertThrows(ConstraintViolationException.class, () -> echipamentWorkflow.inregistreazaMentenanta(idBanda,
                tehnician.getIdUtilizator(), "?", new BigDecimal("-10"), null));
    }
}
