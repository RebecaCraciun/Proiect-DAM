package ro.dam.salafitness.service.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.SedintaPT;
import ro.dam.salafitness.domain.model.enums.StatusSedinta;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UC12 Programare sedinta PT, UC16 Finalizare sedinta PT: RA09, RA13, RV17.
 */
class SedintaPTWorkflowServiceTest extends AbstractIntegrationTest {

    private static final LocalDateTime MAINE_15 = ACUM.plusDays(1).withHour(15);

    @Autowired
    private SedintaPTWorkflowService sedintaWorkflow;

    private Integer idClient;
    private Integer idAntrenor;

    @BeforeEach
    void pregateste() {
        idClient = client("ana@x.ro").getIdUtilizator();
        idAntrenor = antrenor.getIdUtilizator();
    }

    @Test
    void programare_UC12() {
        SedintaPT sedinta = sedintaWorkflow.programeazaSedinta(idClient, idAntrenor, MAINE_15);

        assertNotNull(sedinta.getIdSedinta());
        assertEquals(StatusSedinta.PROGRAMAT, sedinta.getStatus());
    }

    @Test
    void programare_cuUnAngajatCareNuEsteAntrenor_RA09() {
        assertRegula("RA09", () -> sedintaWorkflow.programeazaSedinta(idClient, tehnician.getIdUtilizator(), MAINE_15));
    }

    @Test
    void programare_inTrecut_UC12() {
        assertRegula("UC12", () -> sedintaWorkflow.programeazaSedinta(idClient, idAntrenor, ACUM.minusHours(1)));
    }

    @Test
    void programare_antrenorOcupat_RA13_iarAnulareaElibereazaOra() {
        SedintaPT prima = sedintaWorkflow.programeazaSedinta(idClient, idAntrenor, MAINE_15);
        Integer altClient = client("ion@x.ro").getIdUtilizator();

        assertRegula("RA13", () -> sedintaWorkflow.programeazaSedinta(altClient, idAntrenor, MAINE_15.plusMinutes(30)));

        sedintaWorkflow.anuleazaSedinta(prima.getIdSedinta());
        assertNotNull(sedintaWorkflow.programeazaSedinta(altClient, idAntrenor, MAINE_15.plusMinutes(30)).getIdSedinta());
    }

    @Test
    void finalizare_inainteaOreiProgramate_RV17() {
        SedintaPT sedinta = sedintaWorkflow.programeazaSedinta(idClient, idAntrenor, MAINE_15);

        assertRegula("RV17", () -> sedintaWorkflow.finalizeazaSedinta(sedinta.getIdSedinta(), idAntrenor,
                StatusSedinta.FINALIZAT, "ok"));
    }

    @Test
    void finalizare_UC16_doarDeAntrenorulSedintei_cuStatusFinalValid() {
        SedintaPT sedinta = sedintaWorkflow.programeazaSedinta(idClient, idAntrenor, MAINE_15);
        ceas.seteaza(MAINE_15.plusHours(1));
        Long id = sedinta.getIdSedinta();

        assertRegula("UC16", () -> sedintaWorkflow.finalizeazaSedinta(id, manager.getIdUtilizator(),
                StatusSedinta.FINALIZAT, null));
        assertRegula("RV17", () -> sedintaWorkflow.finalizeazaSedinta(id, idAntrenor, StatusSedinta.ANULAT, null));

        sedintaWorkflow.finalizeazaSedinta(id, idAntrenor, StatusSedinta.FINALIZAT, "Progres bun la genuflexiuni");

        assertEquals(StatusSedinta.FINALIZAT, sedinta.getStatus());
        assertEquals("Progres bun la genuflexiuni", sedinta.getNoteAntrenor());
        assertRegula("RV17", () -> sedintaWorkflow.anuleazaSedinta(id));
    }

    @Test
    void finalizare_clientAbsent_UC16() {
        SedintaPT sedinta = sedintaWorkflow.programeazaSedinta(idClient, idAntrenor, MAINE_15);
        ceas.seteaza(MAINE_15.plusMinutes(15));

        sedintaWorkflow.finalizeazaSedinta(sedinta.getIdSedinta(), idAntrenor, StatusSedinta.ABSENT, null);

        assertEquals(StatusSedinta.ABSENT, sedinta.getStatus());
    }
}
