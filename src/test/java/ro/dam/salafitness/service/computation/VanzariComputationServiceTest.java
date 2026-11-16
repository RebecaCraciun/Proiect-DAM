package ro.dam.salafitness.service.computation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.LinieVanzare;
import ro.dam.salafitness.domain.model.entity.Plata;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.Vanzare;
import ro.dam.salafitness.domain.model.enums.MetodaPlata;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.repository.PlataRepository;
import ro.dam.salafitness.domain.repository.VanzareRepository;
import ro.dam.salafitness.service.computation.view.VenitLunarView;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Reguli de calcul pentru vanzari si venituri (RC01, RC02, RC07, RC08, V3).
 */
class VanzariComputationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private VanzariComputationService vanzariComputationService;
    @Autowired
    private PlataRepository plataRepository;
    @Autowired
    private VanzareRepository vanzareRepository;

    @Test
    void totalLinieSiTotalVanzare_RC01_RC02() {
        Produs proteina = produs("Proteina", "10.50", 20);
        Produs apa = produs("Apa", "3", 20);
        Vanzare vanzare = new Vanzare(1, receptioner.getIdUtilizator(), ACUM);
        LinieVanzare linieProteina = vanzare.adaugaLinie(proteina, 2);
        vanzare.adaugaLinie(apa, 3);

        assertEquals(Bani.of("21.00"), vanzariComputationService.calculeazaTotalLinie(linieProteina));
        assertEquals(Bani.of("30.00"), vanzariComputationService.calculeazaTotalVanzare(vanzare));
    }

    @Test
    void pretulUnitarEsteSnapshot_RC07_RB06() {
        Produs proteina = produs("Proteina", "100", 10);
        LinieVanzare linie = new Vanzare(1, receptioner.getIdUtilizator(), ACUM).adaugaLinie(proteina, 1);
        assertEquals(Bani.of("100"), vanzariComputationService.calculeazaPretUnitar(proteina));

        proteina.modificaPret(Bani.of("120"));

        assertEquals(Bani.of("120"), vanzariComputationService.calculeazaPretUnitar(proteina));
        assertEquals(Bani.of("100"), linie.getPretUnitar());
    }

    @Test
    void stocRamas_RC08_siStocInsuficient_RA14() {
        Produs proteina = produs("Proteina", "100", 5);

        assertEquals(2, vanzariComputationService.calculeazaStocRamas(proteina, 3));
        assertEquals(0, vanzariComputationService.calculeazaStocRamas(proteina, 5));
        assertRegula("RA14", () -> vanzariComputationService.calculeazaStocRamas(proteina, 6));
    }

    @Test
    void venituriLunare_V3_abonamentePlusProduse() {
        Integer idReceptioner = receptioner.getIdUtilizator();
        plataRepository.save(new Plata(1, idReceptioner, Bani.of("150"), MetodaPlata.CARD, ACUM));
        plataRepository.save(new Plata(2, idReceptioner, Bani.of("50"), MetodaPlata.NUMERAR, ACUM.minusMonths(1)));
        Vanzare vanzare = new Vanzare(1, idReceptioner, ACUM);
        vanzare.adaugaLinie(produs("Apa", "5", 10), 6);
        vanzareRepository.save(vanzare);

        List<VenitLunarView> venituri = vanzariComputationService.venituriLunare();

        assertEquals(2, venituri.size());
        assertEquals(new VenitLunarView(YearMonth.of(2026, 9), new BigDecimal("50.00"), BigDecimal.ZERO,
                new BigDecimal("50.00")), venituri.get(0));
        VenitLunarView octombrie = venituri.get(1);
        assertEquals(new BigDecimal("150.00"), octombrie.venitAbonamente());
        assertEquals(new BigDecimal("30.00"), octombrie.venitProduse());
        assertEquals(new BigDecimal("180.00"), octombrie.venitTotal());
    }
}
