package ro.dam.salafitness.service.workflow;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.factory.DateClientNou;
import ro.dam.salafitness.domain.model.entity.AuditLog;
import ro.dam.salafitness.domain.model.listener.AuditEntitateListener;
import java.util.List;
import ro.dam.salafitness.domain.model.entity.AbonamentClient;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.FeedbackClient;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.entity.Utilizator;
import ro.dam.salafitness.domain.model.enums.MetodaPlata;
import ro.dam.salafitness.domain.model.enums.StatusAbonament;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.service.audit.AuditService;
import ro.dam.salafitness.service.workflow.ClientWorkflowService.AchizitieAbonament;
import ro.dam.salafitness.service.workflow.ClientWorkflowService.SituatieClient;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UC01, UC02, UC03 (+UC04), UC05, UC13.
 */
class ClientWorkflowServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ClientWorkflowService clientWorkflow;
    @Autowired
    private AutentificareWorkflowService autentificare;
    @Autowired
    private AuditService auditService;

    private DateClientNou dateAna() {
        return new DateClientNou("Popescu", "Ana", "Ana.Popescu@Mail.ro", "secret!", "0722 123 456", "yoga", null);
    }

    @Test
    void inregistrareClient_UC01() {
        Client client = clientWorkflow.inregistreazaClient(dateAna(), receptioner.getIdUtilizator());

        assertNotNull(client.getIdUtilizator());
        assertEquals("ana.popescu@mail.ro", client.getEmail().getAdresa());
        assertEquals("0722123456", client.getTelefon().getNumar());
        // RB04: auditul de business + auditul din listener
        List<AuditLog> istoric = auditService.istoricEntitate("Client", client.getIdUtilizator());
        assertEquals(1, istoric.stream().filter(a -> a.getActiune().equals("INREGISTRARE")).count());
        assertEquals(1, istoric.stream().filter(a -> a.getActiune().equals(AuditEntitateListener.CREARE)).count());
    }

    @Test
    void inregistrareClient_emailExistent_RV01() {
        clientWorkflow.inregistreazaClient(dateAna(), receptioner.getIdUtilizator());

        assertRegula("RV01", () -> clientWorkflow.inregistreazaClient(dateAna(), receptioner.getIdUtilizator()));
    }

    @Test
    void inregistrareClient_doarDeReceptie_UC01() {
        assertRegula("UC01", () -> clientWorkflow.inregistreazaClient(dateAna(), antrenor.getIdUtilizator()));
    }

    @Test
    void autentificare_UC02() {
        Client client = clientWorkflow.inregistreazaClient(dateAna(), receptioner.getIdUtilizator());

        Utilizator autentificat = autentificare.autentifica("ana.popescu@mail.ro", "secret!");

        assertEquals(client.getIdUtilizator(), autentificat.getIdUtilizator());
        assertEquals(manager, autentificare.autentifica("manager@sala.ro", "parola123"));
        assertRegula("UC02", () -> autentificare.autentifica("ana.popescu@mail.ro", "gresita"));
        assertRegula("UC02", () -> autentificare.autentifica("nimeni@mail.ro", "secret!"));
    }

    @Test
    void achizitieAbonament_UC03_creeazaAbonamentulSiPlata() {
        Client client = client("ana@x.ro");
        TipAbonament lunar = tipAbonament("Lunar", 30, "150");

        AchizitieAbonament achizitie = clientWorkflow.cumparaAbonament(client.getIdUtilizator(),
                lunar.getIdTipAbonament(), MetodaPlata.CARD, receptioner.getIdUtilizator());

        AbonamentClient abonament = achizitie.abonament();
        assertNotNull(abonament.getIdAbonamentClient());
        assertEquals(AZI, abonament.getPerioada().getDataInceput());                  // UC03 pas 3
        assertEquals(AZI.plusDays(29), abonament.getPerioada().getDataSfarsit());     // RC04
        assertEquals(StatusAbonament.ACTIV, abonament.getStatus());                   // RC05
        assertEquals("Lunar", client.getTipAbonament());                              // RC10
        assertNotNull(achizitie.plata().getIdPlata());
        assertEquals(Bani.of("150"), achizitie.plata().getSuma());                    // RC06
        assertEquals(receptioner.getIdUtilizator(), achizitie.plata().getIdAngajat()); // RB04
        assertEquals(1, auditService.istoricUtilizator(receptioner.getIdUtilizator()).size());
    }

    @Test
    void achizitieAbonament_alDoileaIncepeDupaExpirareaPrimului_RA05() {
        Client client = client("ana@x.ro");
        TipAbonament lunar = tipAbonament("Lunar", 30, "150");
        TipAbonament anual = tipAbonament("Anual", 365, "1200");
        Integer idReceptioner = receptioner.getIdUtilizator();
        clientWorkflow.cumparaAbonament(client.getIdUtilizator(), lunar.getIdTipAbonament(), MetodaPlata.CARD, idReceptioner);

        AbonamentClient alDoilea = clientWorkflow.cumparaAbonament(client.getIdUtilizator(), anual.getIdTipAbonament(),
                MetodaPlata.NUMERAR, idReceptioner).abonament();

        assertEquals(AZI.plusDays(30), alDoilea.getPerioada().getDataInceput());
        assertEquals("Anual", client.getTipAbonament());
        assertEquals(2, client.getAbonamente().size());
    }

    @Test
    void achizitieAbonament_platileVechiPastreazaPretul_RB06() {
        Client client = client("ana@x.ro");
        TipAbonament lunar = tipAbonament("Lunar", 30, "150");
        AchizitieAbonament prima = clientWorkflow.cumparaAbonament(client.getIdUtilizator(), lunar.getIdTipAbonament(),
                MetodaPlata.CARD, receptioner.getIdUtilizator());

        lunar.modificaPret(Bani.of("180"));
        AchizitieAbonament aDoua = clientWorkflow.cumparaAbonament(client.getIdUtilizator(), lunar.getIdTipAbonament(),
                MetodaPlata.CARD, receptioner.getIdUtilizator());

        assertEquals(Bani.of("150"), prima.plata().getSuma());
        assertEquals(Bani.of("180"), aDoua.plata().getSuma());
    }

    @Test
    void achizitieAbonament_doarDeReceptioner_RA11() {
        Client client = client("ana@x.ro");
        TipAbonament lunar = tipAbonament("Lunar", 30, "150");

        assertRegula("RA11", () -> clientWorkflow.cumparaAbonament(client.getIdUtilizator(),
                lunar.getIdTipAbonament(), MetodaPlata.CARD, antrenor.getIdUtilizator()));
        assertTrue(client.getAbonamente().isEmpty());
    }

    @Test
    void achizitieAbonament_plataInvalidaNuCreeazaAbonament_UC03_5a() {
        Client client = client("ana@x.ro");
        TipAbonament lunar = tipAbonament("Lunar", 30, "150");

        assertThrows(ConstraintViolationException.class, () -> clientWorkflow.cumparaAbonament(
                client.getIdUtilizator(), lunar.getIdTipAbonament(), null, receptioner.getIdUtilizator()));
        assertTrue(client.getAbonamente().isEmpty());
        assertNull(client.getTipAbonament());
    }

    @Test
    void achizitieAbonament_clientNou_UC01extindeUC03() {
        TipAbonament lunar = tipAbonament("Lunar", 30, "150");

        AchizitieAbonament achizitie = clientWorkflow.inregistreazaSiCumparaAbonament(dateAna(),
                lunar.getIdTipAbonament(), MetodaPlata.TRANSFER, receptioner.getIdUtilizator());

        Client client = achizitie.abonament().getClient();
        assertNotNull(client.getIdUtilizator());
        assertEquals("Lunar", client.getTipAbonament());
        assertEquals(MetodaPlata.TRANSFER, achizitie.plata().getMetodaPlata());
    }

    @Test
    void consultareAbonamentSiPlati_UC05() {
        Client client = client("ana@x.ro");
        assertNull(clientWorkflow.consultaSituatie(client.getIdUtilizator()).abonamentCurent());

        clientWorkflow.cumparaAbonament(client.getIdUtilizator(), tipAbonament("Lunar", 30, "150").getIdTipAbonament(),
                MetodaPlata.CARD, receptioner.getIdUtilizator());
        ceas.seteaza(ACUM.plusDays(10));

        SituatieClient situatie = clientWorkflow.consultaSituatie(client.getIdUtilizator());
        assertEquals("Lunar", situatie.tipAbonament());
        assertNotNull(situatie.abonamentCurent());
        assertEquals(19, situatie.zileRamase());
        assertEquals(1, situatie.plati().size());
    }

    @Test
    void acordareFeedback_UC13_RV23() {
        Client client = client("ana@x.ro");

        FeedbackClient feedback = clientWorkflow.acordaFeedback(client.getIdUtilizator(), 5, "Antrenori foarte buni");

        assertNotNull(feedback.getIdFeedback());
        assertEquals(ACUM, feedback.getDataFeedback());
        assertThrows(ConstraintViolationException.class,
                () -> clientWorkflow.acordaFeedback(client.getIdUtilizator(), 0, "?"));
    }
}
