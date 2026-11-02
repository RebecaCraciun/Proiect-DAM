package ro.dam.salafitness.service.computation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.AbonamentClient;
import ro.dam.salafitness.domain.model.entity.ClasaFitness;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.LinieVanzare;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.domain.model.entity.SalariuIstoric;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.entity.Vanzare;
import ro.dam.salafitness.domain.model.enums.StatusAbonament;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.model.vo.IntervalOrar;
import ro.dam.salafitness.domain.model.vo.PerioadaValabilitate;
import ro.dam.salafitness.domain.repository.ClasaFitnessRepository;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Reguli de calcul pentru campurile entitatilor (README 3.3, RC01-RC11).
 */
class FieldComputationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private FieldComputationService computationService;
    @Autowired
    private ClasaFitnessRepository clasaFitnessRepository;

    @Test
    void totalLinieSiTotalVanzare_RC01_RC02() {
        Produs proteina = produs("Proteina", "10.50", 20);
        Produs apa = produs("Apa", "3", 20);
        Vanzare vanzare = new Vanzare(1, receptioner.getIdUtilizator(), ACUM);
        LinieVanzare linieProteina = vanzare.adaugaLinie(proteina, 2);
        vanzare.adaugaLinie(apa, 3);

        assertEquals(Bani.of("21.00"), computationService.calculeazaTotalLinie(linieProteina));
        assertEquals(Bani.of("30.00"), computationService.calculeazaTotalVanzare(vanzare));
    }

    @Test
    void oreLucrate_RC03_rotunjireLaDouaZecimale() {
        manager.checkIn(ACUM);
        PontajAngajat pontaj = manager.checkOut(ACUM.plusHours(7).plusMinutes(50));

        assertEquals(new BigDecimal("7.83"), computationService.calculeazaOreLucrate(pontaj));
    }

    @Test
    void oreLucrate_pontajDeschis_RC03() {
        PontajAngajat pontaj = manager.checkIn(ACUM);

        assertNull(computationService.calculeazaOreLucrate(pontaj));
    }

    @Test
    void perioadaAbonamentului_RC04_RA06() {
        TipAbonament lunar = tipAbonament("Lunar", 30, "150");

        PerioadaValabilitate perioada = computationService.calculeazaPerioada(lunar, LocalDate.of(2026, 11, 1));

        assertEquals(LocalDate.of(2026, 11, 30), perioada.getDataSfarsit());
        assertEquals(30, perioada.durataZile());
    }

    @Test
    void dataInceputAbonament_dupaExpirareaCeluiActiv_UC03() {
        Client client = client("ana@x.ro");
        assertEquals(AZI, computationService.calculeazaDataInceputAbonament(client, AZI));

        AbonamentClient activ = client.cumparaAbonament(tipAbonament("Lunar", 30, "150"), AZI);

        assertEquals(activ.getPerioada().getDataSfarsit().plusDays(1),
                computationService.calculeazaDataInceputAbonament(client, AZI));
    }

    @Test
    void statusAbonament_RC05() {
        Client client = client("ana@x.ro");
        TipAbonament lunar = tipAbonament("Lunar", 30, "150");
        AbonamentClient abonament = client.cumparaAbonament(lunar, AZI.minusDays(29)); // expira azi

        assertEquals(StatusAbonament.ACTIV, computationService.calculeazaStatusAbonament(abonament, AZI));
        assertEquals(StatusAbonament.EXPIRAT, computationService.calculeazaStatusAbonament(abonament, AZI.plusDays(1)));
    }

    @Test
    void statusAbonament_actualizareaPeriodicaFolosesteDataCurenta_RC05() {
        Client client = client("ana@x.ro");
        client.cumparaAbonament(tipAbonament("Saptamanal", 7, "50"), AZI);
        clientRepository.flush();

        assertEquals(0, computationService.actualizeazaStatusAbonamente());
        ceas.seteaza(ACUM.plusDays(7));
        assertEquals(1, computationService.actualizeazaStatusAbonamente());
        assertEquals(StatusAbonament.EXPIRAT, client.getAbonamente().get(0).getStatus());
    }

    @Test
    void sumaPlatiiEstePretulTipului_RC06() {
        assertEquals(Bani.of("150.00"), computationService.calculeazaSumaPlataAbonament(tipAbonament("Lunar", 30, "150")));
    }

    @Test
    void pretulUnitarEsteSnapshot_RC07_RB06() {
        Produs proteina = produs("Proteina", "100", 10);
        LinieVanzare linie = new Vanzare(1, receptioner.getIdUtilizator(), ACUM).adaugaLinie(proteina, 1);
        assertEquals(Bani.of("100"), computationService.calculeazaPretUnitar(proteina));

        proteina.modificaPret(Bani.of("120"));

        assertEquals(Bani.of("120"), computationService.calculeazaPretUnitar(proteina));
        assertEquals(Bani.of("100"), linie.getPretUnitar());
    }

    @Test
    void stocRamas_RC08_siStocInsuficient_RA14() {
        Produs proteina = produs("Proteina", "100", 5);

        assertEquals(2, computationService.calculeazaStocRamas(proteina, 3));
        assertEquals(0, computationService.calculeazaStocRamas(proteina, 5));
        assertRegula("RA14", () -> computationService.calculeazaStocRamas(proteina, 6));
    }

    @Test
    void locuriLibere_RC09_rezervarileAnulateNuOcupaLoc() {
        ClasaFitness clasa = clasaFitnessRepository.save(new ClasaFitness("Pilates", antrenor.getIdUtilizator(), 3, null));
        ProgramClasa sesiune = clasa.programeazaSesiune(IntervalOrar.intre(ACUM.plusDays(1), ACUM.plusDays(1).plusHours(1)), "Studio 1");
        clasaFitnessRepository.flush();
        Long idProgram = sesiune.getIdProgram();
        assertEquals(3, computationService.calculeazaLocuriLibere(sesiune));

        clasa.rezerva(idProgram, 101, ACUM);
        clasa.rezerva(idProgram, 102, ACUM);
        assertEquals(1, computationService.calculeazaLocuriLibere(sesiune));

        clasa.anuleazaRezervare(idProgram, 101, ACUM);
        assertEquals(2, computationService.calculeazaLocuriLibere(sesiune));
    }

    @Test
    void tipulAbonamentuluiClientului_esteAlUltimuluiCumparat_RC10() {
        Client client = client("ana@x.ro");
        client.cumparaAbonament(tipAbonament("Lunar", 30, "150"), AZI);
        client.cumparaAbonament(tipAbonament("Anual", 365, "1200"), AZI);
        client.actualizeazaTipAbonament(null);

        assertEquals("Anual", computationService.calculeazaTipAbonamentClient(client));
        assertEquals("Anual", client.getTipAbonament());
    }

    @Test
    void sumaSalariuluiEsteSnapshotLaGenerare_RC11() {
        assertEquals(Bani.of("4000"), computationService.calculeazaSumaSalariu(antrenor));
        SalariuIstoric octombrie = antrenor.genereazaSalariu(YearMonth.of(2026, 10));

        antrenor.modificaSalariu(Bani.of("4500"));

        assertEquals(Bani.of("4500"), computationService.calculeazaSumaSalariu(antrenor));
        assertEquals(Bani.of("4000"), octombrie.getSumaPlatita());
    }
}
