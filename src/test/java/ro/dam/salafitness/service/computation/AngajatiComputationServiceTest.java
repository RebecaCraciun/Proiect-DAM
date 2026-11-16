package ro.dam.salafitness.service.computation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;
import ro.dam.salafitness.domain.model.entity.SalariuIstoric;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.service.computation.view.OreLucrateLunarView;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Reguli de calcul pentru angajati (RC03, RC11, V4).
 */
class AngajatiComputationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private AngajatiComputationService angajatiComputationService;

    @Test
    void oreLucrate_RC03_rotunjireLaDouaZecimale() {
        manager.checkIn(ACUM);
        PontajAngajat pontaj = manager.checkOut(ACUM.plusHours(7).plusMinutes(50));

        assertEquals(new BigDecimal("7.83"), angajatiComputationService.calculeazaOreLucrate(pontaj));
    }

    @Test
    void oreLucrate_pontajDeschis_RC03() {
        PontajAngajat pontaj = manager.checkIn(ACUM);

        assertNull(angajatiComputationService.calculeazaOreLucrate(pontaj));
    }

    @Test
    void sumaSalariuluiEsteSnapshotLaGenerare_RC11() {
        assertEquals(Bani.of("4000"), angajatiComputationService.calculeazaSumaSalariu(antrenor));
        SalariuIstoric octombrie = antrenor.genereazaSalariu(YearMonth.of(2026, 10));

        antrenor.modificaSalariu(Bani.of("4500"));

        assertEquals(Bani.of("4500"), angajatiComputationService.calculeazaSumaSalariu(antrenor));
        assertEquals(Bani.of("4000"), octombrie.getSumaPlatita());
    }

    @Test
    void oreLucrateLunar_V4_doarPontajeleInchise() {
        receptioner.checkIn(ACUM.minusDays(1));
        receptioner.checkOut(ACUM.minusDays(1).plusHours(8));
        receptioner.checkIn(ACUM);
        receptioner.checkOut(ACUM.plusHours(4).plusMinutes(30));
        antrenor.checkIn(ACUM); // deschis, nu intra in raport
        angajatRepository.flush();

        List<OreLucrateLunarView> raport = angajatiComputationService.oreLucrateLunar();

        assertEquals(1, raport.size());
        assertEquals(2, raport.get(0).zilePontate());
        assertEquals(new BigDecimal("12.50"), raport.get(0).totalOre());
        assertEquals("Recepționer", raport.get(0).tipAngajat());
    }
}
