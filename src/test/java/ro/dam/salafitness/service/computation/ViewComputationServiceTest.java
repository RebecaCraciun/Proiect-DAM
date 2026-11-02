package ro.dam.salafitness.service.computation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.ClasaFitness;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.Echipament;
import ro.dam.salafitness.domain.model.entity.Plata;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.domain.model.entity.Vanzare;
import ro.dam.salafitness.domain.model.enums.MetodaPlata;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.model.vo.IntervalOrar;
import ro.dam.salafitness.domain.repository.ClasaFitnessRepository;
import ro.dam.salafitness.domain.repository.EchipamentRepository;
import ro.dam.salafitness.domain.repository.PlataRepository;
import ro.dam.salafitness.domain.repository.VanzareRepository;
import ro.dam.salafitness.service.computation.view.AbonamentActivView;
import ro.dam.salafitness.service.computation.view.CostMentenantaView;
import ro.dam.salafitness.service.computation.view.FeedbackLunarView;
import ro.dam.salafitness.service.computation.view.OcupareClasaView;
import ro.dam.salafitness.service.computation.view.OreLucrateLunarView;
import ro.dam.salafitness.service.computation.view.VenitLunarView;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reguli de calcul pentru view-urile calculate (README Anexa A, V1-V6).
 */
class ViewComputationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ViewComputationService viewService;
    @Autowired
    private ClasaFitnessRepository clasaFitnessRepository;
    @Autowired
    private PlataRepository plataRepository;
    @Autowired
    private VanzareRepository vanzareRepository;
    @Autowired
    private EchipamentRepository echipamentRepository;

    @Test
    void abonamenteActive_V1_doarCeleValabile_ordonateDupaZileleRamase() {
        Client ana = client("ana@x.ro");
        Client ion = client("ion@x.ro");
        Client expirat = client("vechi@x.ro");
        ana.cumparaAbonament(tipAbonament("Anual", 365, "1200"), AZI);
        ion.cumparaAbonament(tipAbonament("Lunar", 30, "150"), AZI.minusDays(20));
        expirat.cumparaAbonament(tipAbonament("Saptamanal", 7, "50"), AZI.minusDays(30));
        clientRepository.flush();

        List<AbonamentActivView> active = viewService.abonamenteActive(AZI);

        assertEquals(2, active.size());
        assertEquals("Lunar", active.get(0).tipAbonament());
        assertEquals(9, active.get(0).zileRamase());
        assertEquals("Anual", active.get(1).tipAbonament());
        assertEquals(364, active.get(1).zileRamase());
    }

    @Test
    void ocupareClase_V2_gradDeOcupare() {
        ClasaFitness clasa = clasaFitnessRepository.save(new ClasaFitness("Spinning", antrenor.getIdUtilizator(), 4, null));
        ProgramClasa sesiune = clasa.programeazaSesiune(IntervalOrar.intre(ACUM.plusDays(1), ACUM.plusDays(1).plusHours(1)), "Sala 2");
        clasaFitnessRepository.flush();
        clasa.rezerva(sesiune.getIdProgram(), 101, ACUM);
        clasaFitnessRepository.flush();

        OcupareClasaView ocupare = viewService.ocupareClase().get(0);

        assertEquals("Spinning", ocupare.clasa());
        assertEquals(1, ocupare.locuriOcupate());
        assertEquals(3, ocupare.locuriLibere());
        assertEquals(new BigDecimal("25.0"), ocupare.gradOcuparePct());
        assertTrue(viewService.ocupareClase(ACUM.plusDays(2), ACUM.plusDays(3)).isEmpty());
    }

    @Test
    void venituriLunare_V3_abonamentePlusProduse() {
        Integer idReceptioner = receptioner.getIdUtilizator();
        plataRepository.save(new Plata(1, idReceptioner, Bani.of("150"), MetodaPlata.CARD, ACUM));
        plataRepository.save(new Plata(2, idReceptioner, Bani.of("50"), MetodaPlata.NUMERAR, ACUM.minusMonths(1)));
        Vanzare vanzare = new Vanzare(1, idReceptioner, ACUM);
        vanzare.adaugaLinie(produs("Apa", "5", 10), 6);
        vanzareRepository.save(vanzare);

        List<VenitLunarView> venituri = viewService.venituriLunare();

        assertEquals(2, venituri.size());
        assertEquals(new VenitLunarView(YearMonth.of(2026, 9), new BigDecimal("50.00"), BigDecimal.ZERO,
                new BigDecimal("50.00")), venituri.get(0));
        VenitLunarView octombrie = venituri.get(1);
        assertEquals(new BigDecimal("150.00"), octombrie.venitAbonamente());
        assertEquals(new BigDecimal("30.00"), octombrie.venitProduse());
        assertEquals(new BigDecimal("180.00"), octombrie.venitTotal());
    }

    @Test
    void oreLucrateLunar_V4_doarPontajeleInchise() {
        receptioner.checkIn(ACUM.minusDays(1));
        receptioner.checkOut(ACUM.minusDays(1).plusHours(8));
        receptioner.checkIn(ACUM);
        receptioner.checkOut(ACUM.plusHours(4).plusMinutes(30));
        antrenor.checkIn(ACUM); // deschis, nu intra in raport
        angajatRepository.flush();

        List<OreLucrateLunarView> raport = viewService.oreLucrateLunar();

        assertEquals(1, raport.size());
        assertEquals(2, raport.get(0).zilePontate());
        assertEquals(new BigDecimal("12.50"), raport.get(0).totalOre());
        assertEquals("Recepționer", raport.get(0).tipAngajat());
    }

    @Test
    void costMentenanta_V5_ordonatDescrescator() {
        Echipament banda = echipamentRepository.save(new Echipament("Banda", AZI.minusYears(2), "Uzat", "Zona cardio"));
        Echipament gantere = echipamentRepository.save(new Echipament("Gantere", AZI.minusYears(1), "Nou", "Zona libera"));
        banda.inregistreazaMentenanta(AZI.minusDays(10), "Curea", Bani.of("100"), tehnician.getIdUtilizator(), null);
        banda.inregistreazaMentenanta(AZI, "Motor", Bani.of("50.50"), tehnician.getIdUtilizator(), "Reparat");
        echipamentRepository.flush();

        List<CostMentenantaView> costuri = viewService.costMentenantaEchipamente();

        assertEquals(List.of("Banda", "Gantere"), costuri.stream().map(CostMentenantaView::denumire).toList());
        assertEquals(new BigDecimal("150.50"), costuri.get(0).costTotal());
        assertEquals(2, costuri.get(0).nrInterventii());
        assertEquals(AZI, costuri.get(0).ultimaVerificare());
        assertEquals("Reparat", costuri.get(0).stareUzura());
        assertEquals(0, costuri.get(1).nrInterventii());
        assertEquals(gantere.getIdEchipament(), costuri.get(1).idEchipament());
    }

    @Test
    void feedbackLunar_V6_medieSiRecenziiNegative() {
        Client ana = client("ana@x.ro");
        ana.adaugaFeedback(5, "Super", ACUM);
        ana.adaugaFeedback(1, "Aglomerat", ACUM);
        ana.adaugaFeedback(4, null, ACUM);
        clientRepository.flush();

        FeedbackLunarView octombrie = viewService.feedbackLunar().get(0);

        assertEquals(YearMonth.of(2026, 10), octombrie.luna());
        assertEquals(3, octombrie.nrRecenzii());
        assertEquals(new BigDecimal("3.33"), octombrie.ratingMediu());
        assertEquals(1, octombrie.recenziiNegative());
    }
}
