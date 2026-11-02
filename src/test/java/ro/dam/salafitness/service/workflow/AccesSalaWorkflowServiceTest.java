package ro.dam.salafitness.service.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.AccesSala;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.enums.MetodaPlata;
import ro.dam.salafitness.domain.model.enums.StatusAcces;
import ro.dam.salafitness.domain.repository.AccesSalaRepository;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UC06 Acces in sala (cu UC07 inclus): RA07, RV18.
 */
class AccesSalaWorkflowServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AccesSalaWorkflowService accesWorkflow;
    @Autowired
    private ClientWorkflowService clientWorkflow;
    @Autowired
    private AccesSalaRepository accesSalaRepository;

    private Client client;

    @BeforeEach
    void pregatesteClient() {
        client = client("ana@x.ro");
    }

    private void cumparaAbonamentLunar() {
        clientWorkflow.cumparaAbonament(client.getIdUtilizator(), tipAbonament("Lunar", 30, "150").getIdTipAbonament(),
                MetodaPlata.CARD, receptioner.getIdUtilizator());
    }

    @Test
    void clientCuAbonamentValid_intraSiIese_UC06() {
        cumparaAbonamentLunar();

        AccesSala acces = accesWorkflow.inregistreazaIntrare(client.getIdUtilizator());
        ceas.avanseaza(Duration.ofMinutes(90));
        accesWorkflow.inregistreazaIesire(acces.getIdAcces());

        assertEquals(StatusAcces.PERMIS, acces.getStatusAcces());
        assertEquals(ACUM, acces.getDataOraIntrare());
        assertEquals(ACUM.plusMinutes(90), acces.getDataOraIesire());
    }

    @Test
    void clientFaraAbonament_esteRespinsDarIncercareaSeInregistreaza_RA07() {
        AccesSala acces = accesWorkflow.inregistreazaIntrare(client.getIdUtilizator());

        assertEquals(StatusAcces.RESPINS, acces.getStatusAcces());
        assertNotNull(acces.getIdAcces());
        assertEquals(1, accesSalaRepository.count());
    }

    @Test
    void abonamentExpirat_accesRespins_RA07() {
        cumparaAbonamentLunar();
        ceas.avanseaza(Duration.ofDays(30));

        assertEquals(StatusAcces.RESPINS, accesWorkflow.inregistreazaIntrare(client.getIdUtilizator()).getStatusAcces());
    }

    @Test
    void angajatulIntraFaraAbonament_UC06_2a() {
        assertEquals(StatusAcces.PERMIS,
                accesWorkflow.inregistreazaIntrare(femeieDeServiciu.getIdUtilizator()).getStatusAcces());
    }

    @Test
    void accesulRespinsNuAreIesire_iarIesireaNuSeInregistreazaDeDouaOri_RV18() {
        AccesSala respins = accesWorkflow.inregistreazaIntrare(client.getIdUtilizator());
        assertRegula("RV18", () -> accesWorkflow.inregistreazaIesire(respins.getIdAcces()));

        AccesSala permis = accesWorkflow.inregistreazaIntrare(manager.getIdUtilizator());
        accesWorkflow.inregistreazaIesire(permis.getIdAcces());
        assertRegula("RV18", () -> accesWorkflow.inregistreazaIesire(permis.getIdAcces()));
    }
}
