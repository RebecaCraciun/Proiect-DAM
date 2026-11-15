package ro.dam.salafitness.domain.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.dam.salafitness.domain.model.entity.AbonamentClient;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.enums.StatusAbonament;
import ro.dam.salafitness.support.AbstractRepositoryTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Sprint 3: depozitul agregatului Client
class ClientRepositoryTest extends AbstractRepositoryTest {

    private TipAbonament lunar;

    @BeforeEach
    void pregatesteTipAbonament() {
        lunar = tipAbonament("Lunar", 30, "150.00");
    }

    @Test
    void findByEmailAdresa_cautaDupaCampulValueObjectului() {
        Client ana = client("ana@sala.ro");
        sincronizeaza();

        assertEquals(ana.getIdUtilizator(),
                clientRepository.findByEmailAdresa("ana@sala.ro").orElseThrow().getIdUtilizator());
        assertTrue(clientRepository.findByEmailAdresa("alt@sala.ro").isEmpty());
    }

    @Test
    void cautareDupaNume_ignoraMajusculele() {
        client("ana@sala.ro");
        sincronizeaza();

        assertEquals(1, clientRepository.findByNumeContainingIgnoreCaseOrPrenumeContainingIgnoreCase("POP", "POP").size());
        assertTrue(clientRepository.findByNumeContainingIgnoreCaseOrPrenumeContainingIgnoreCase("Ion", "Ion").isEmpty());
    }

    @Test
    void abonamentul_seSalveazaPrinCascadaDinRadacina_iarStatusulSeScrieCaValoareaDinCheck() {
        Client ana = client("ana@sala.ro");
        AbonamentClient abonament = ana.cumparaAbonament(lunar, AZI);
        sincronizeaza();

        assertNotNull(abonament.getIdAbonamentClient());
        Client citit = clientRepository.findById(ana.getIdUtilizator()).orElseThrow();
        assertEquals(1, citit.getAbonamente().size());
        assertEquals(StatusAbonament.ACTIV, citit.getAbonamente().get(0).getStatus());
        assertEquals("Lunar", citit.getTipAbonament());
        // @Convert: in coloana ajunge 'Activ', nu ACTIV
        assertEquals("Activ", valoareNativa(
                "select status from abonamente_clienti where id_abonament_client = :id", abonament.getIdAbonamentClient()));
    }

    @Test
    void filtreCustom_abonamenteValabileLaData_UC07() {
        Client ana = client("ana@sala.ro");
        ana.cumparaAbonament(lunar, AZI);
        client("ion@sala.ro");
        sincronizeaza();

        assertEquals(1, clientRepository.findAbonamenteValabileLa(AZI).size());
        assertEquals(1, clientRepository.findAbonamenteValabileLa(AZI.plusDays(29)).size());
        assertTrue(clientRepository.findAbonamenteValabileLa(AZI.plusDays(30)).isEmpty());
        assertEquals(List.of(ana.getIdUtilizator()),
                clientRepository.findCuAbonamentValabilLa(AZI).stream().map(Client::getIdUtilizator).toList());
    }

    @Test
    void existaAbonamentDeTip_RB01() {
        TipAbonament anual = tipAbonament("Anual", 365, "1400.00");
        client("ana@sala.ro").cumparaAbonament(lunar, AZI);
        sincronizeaza();

        assertTrue(clientRepository.existaAbonamentDeTip(lunar.getIdTipAbonament()));
        assertFalse(clientRepository.existaAbonamentDeTip(anual.getIdTipAbonament()));
    }

    @Test
    void feedbackul_cascadaAll_seStergeOdataCuClientul_RB02() {
        Client ana = client("ana@sala.ro");
        ana.adaugaFeedback(5, "Foarte curat", ACUM);
        sincronizeaza();
        assertEquals(1, clientRepository.findToateFeedbackurile().size());

        clientRepository.delete(clientRepository.findById(ana.getIdUtilizator()).orElseThrow());
        sincronizeaza();

        assertTrue(clientRepository.findById(ana.getIdUtilizator()).isEmpty());
        assertTrue(clientRepository.findToateFeedbackurile().isEmpty());
    }
}