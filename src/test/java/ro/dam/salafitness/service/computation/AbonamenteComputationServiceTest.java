package ro.dam.salafitness.service.computation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.AbonamentClient;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.enums.StatusAbonament;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.model.vo.PerioadaValabilitate;
import ro.dam.salafitness.service.computation.view.AbonamentActivView;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Reguli de calcul pentru abonamente (RC04, RC05, RC06, RC10, V1).
 */
class AbonamenteComputationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AbonamenteComputationService abonamenteComputationService;

    @Test
    void perioadaAbonamentului_RC04_RA06() {
        TipAbonament lunar = tipAbonament("Lunar", 30, "150");

        PerioadaValabilitate perioada = abonamenteComputationService.calculeazaPerioada(lunar, LocalDate.of(2026, 11, 1));

        assertEquals(LocalDate.of(2026, 11, 30), perioada.getDataSfarsit());
        assertEquals(30, perioada.durataZile());
    }

    @Test
    void dataInceputAbonament_dupaExpirareaCeluiActiv_UC03() {
        Client client = client("ana@x.ro");
        assertEquals(AZI, abonamenteComputationService.calculeazaDataInceputAbonament(client, AZI));

        AbonamentClient activ = client.cumparaAbonament(tipAbonament("Lunar", 30, "150"), AZI);

        assertEquals(activ.getPerioada().getDataSfarsit().plusDays(1),
                abonamenteComputationService.calculeazaDataInceputAbonament(client, AZI));
    }

    @Test
    void statusAbonament_RC05() {
        Client client = client("ana@x.ro");
        TipAbonament lunar = tipAbonament("Lunar", 30, "150");
        AbonamentClient abonament = client.cumparaAbonament(lunar, AZI.minusDays(29)); // expira azi

        assertEquals(StatusAbonament.ACTIV, abonamenteComputationService.calculeazaStatusAbonament(abonament, AZI));
        assertEquals(StatusAbonament.EXPIRAT, abonamenteComputationService.calculeazaStatusAbonament(abonament, AZI.plusDays(1)));
    }

    @Test
    void statusAbonament_actualizareaPeriodicaFolosesteDataCurenta_RC05() {
        Client client = client("ana@x.ro");
        client.cumparaAbonament(tipAbonament("Saptamanal", 7, "50"), AZI);
        clientRepository.flush();

        assertEquals(0, abonamenteComputationService.actualizeazaStatusAbonamente());
        ceas.seteaza(ACUM.plusDays(7));
        assertEquals(1, abonamenteComputationService.actualizeazaStatusAbonamente());
        assertEquals(StatusAbonament.EXPIRAT, client.getAbonamente().get(0).getStatus());
    }

    @Test
    void sumaPlatiiEstePretulTipului_RC06() {
        assertEquals(Bani.of("150.00"), abonamenteComputationService.calculeazaSumaPlataAbonament(tipAbonament("Lunar", 30, "150")));
    }

    @Test
    void tipulAbonamentuluiClientului_esteAlUltimuluiCumparat_RC10() {
        Client client = client("ana@x.ro");
        client.cumparaAbonament(tipAbonament("Lunar", 30, "150"), AZI);
        client.cumparaAbonament(tipAbonament("Anual", 365, "1200"), AZI);
        client.actualizeazaTipAbonament(null);

        assertEquals("Anual", abonamenteComputationService.calculeazaTipAbonamentClient(client));
        assertEquals("Anual", client.getTipAbonament());
    }

    @Test
    void abonamenteActive_V1_doarCeleValabile_ordonateDupaZileleRamase() {
        Client ana = client("ana@x.ro");
        Client ion = client("ion@x.ro");
        Client expirat = client("vechi@x.ro");
        ana.cumparaAbonament(tipAbonament("Anual", 365, "1200"), AZI);
        ion.cumparaAbonament(tipAbonament("Lunar", 30, "150"), AZI.minusDays(20));
        expirat.cumparaAbonament(tipAbonament("Saptamanal", 7, "50"), AZI.minusDays(30));
        clientRepository.flush();

        List<AbonamentActivView> active = abonamenteComputationService.abonamenteActive(AZI);

        assertEquals(2, active.size());
        assertEquals("Lunar", active.get(0).tipAbonament());
        assertEquals(9, active.get(0).zileRamase());
        assertEquals("Anual", active.get(1).tipAbonament());
        assertEquals(364, active.get(1).zileRamase());
    }
}
