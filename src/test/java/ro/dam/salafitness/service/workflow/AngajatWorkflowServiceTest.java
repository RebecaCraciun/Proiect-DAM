package ro.dam.salafitness.service.workflow;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.factory.DateAngajatNou;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;
import ro.dam.salafitness.domain.model.entity.SalariuIstoric;
import ro.dam.salafitness.domain.model.enums.StatusPlataSalariu;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UC17 Pontaj, UC18 Calcul ore lucrate, UC19 Gestionare angajati, UC20 Plata salarii: RA08, RC03, RC11, RB05.
 */
class AngajatWorkflowServiceTest extends AbstractIntegrationTest {

    private static final YearMonth OCTOMBRIE = YearMonth.of(2026, 10);

    @Autowired
    private AngajatWorkflowService angajatWorkflow;

    private DateAngajatNou dateAntrenorNou(BigDecimal salariu) {
        return new DateAngajatNou("Ionescu", "Mihai", "mihai@sala.ro", "parola", null,
                antrenor.getIdTipAngajat(), "Antrenor fitness", salariu, AZI, "L-V 12-20");
    }

    @Test
    void angajare_UC19() {
        Angajat nou = angajatWorkflow.angajeaza(dateAntrenorNou(new BigDecimal("3500")), manager.getIdUtilizator());

        assertNotNull(nou.getIdUtilizator());
        assertEquals(Bani.of("3500"), nou.getSalariu());
        assertTrue(nou.getParola().startsWith("$2"));
    }

    @Test
    void angajare_doarDeManager_UC19_siCuSalariuNenegativ_RV05() {
        assertRegula("UC19", () -> angajatWorkflow.angajeaza(dateAntrenorNou(BigDecimal.TEN), receptioner.getIdUtilizator()));
        assertThrows(ConstraintViolationException.class,
                () -> angajatWorkflow.angajeaza(dateAntrenorNou(new BigDecimal("-1")), manager.getIdUtilizator()));
    }

    @Test
    void pontaj_checkInSiCheckOut_RC03() {
        Integer id = receptioner.getIdUtilizator();

        PontajAngajat pontaj = angajatWorkflow.checkIn(id);
        assertNotNull(pontaj.getIdPontaj());
        assertNull(pontaj.getOreLucrate());

        ceas.avanseaza(Duration.ofHours(8).plusMinutes(15));
        angajatWorkflow.checkOut(id);

        assertEquals(ACUM.plusHours(8).plusMinutes(15), pontaj.getCheckOut());
        assertEquals(new BigDecimal("8.25"), pontaj.getOreLucrate());
    }

    @Test
    void pontaj_checkInDublu_RA08_siCheckOutFaraCheckIn() {
        Integer id = receptioner.getIdUtilizator();
        assertRegula("RA08", () -> angajatWorkflow.checkOut(id));

        angajatWorkflow.checkIn(id);
        assertRegula("RA08", () -> angajatWorkflow.checkIn(id));
    }

    @Test
    void oreLucrateLunar_UC18() {
        Integer id = femeieDeServiciu.getIdUtilizator();
        angajatWorkflow.checkIn(id);
        ceas.avanseaza(Duration.ofHours(6));
        angajatWorkflow.checkOut(id);
        ceas.avanseaza(Duration.ofDays(1));
        angajatWorkflow.checkIn(id);
        ceas.avanseaza(Duration.ofMinutes(4 * 60 + 30));
        angajatWorkflow.checkOut(id);

        assertEquals(new BigDecimal("10.50"), angajatWorkflow.oreLucrate(id, OCTOMBRIE));
        assertEquals(BigDecimal.ZERO, angajatWorkflow.oreLucrate(id, OCTOMBRIE.plusMonths(1)));
    }

    @Test
    void generareSalarii_snapshotulSalariului_RC11_faraDubluri() {
        List<SalariuIstoric> generate = angajatWorkflow.genereazaSalarii(OCTOMBRIE, manager.getIdUtilizator());
        assertEquals(5, generate.size());
        SalariuIstoric salariuAntrenor = antrenor.getSalarii().get(0);
        assertEquals(StatusPlataSalariu.IN_ASTEPTARE, salariuAntrenor.getStatusPlata());

        angajatWorkflow.modificaSalariu(antrenor.getIdUtilizator(), new BigDecimal("5000"), manager.getIdUtilizator());

        assertEquals(Bani.of("4000"), salariuAntrenor.getSumaPlatita());
        assertEquals(Bani.of("5000"), antrenor.getSalariu());
        assertTrue(angajatWorkflow.genereazaSalarii(OCTOMBRIE, manager.getIdUtilizator()).isEmpty());
    }

    @Test
    void confirmarePlataSalariu_RB05() {
        angajatWorkflow.genereazaSalarii(OCTOMBRIE, manager.getIdUtilizator());
        Long idPlata = antrenor.getSalarii().get(0).getIdPlata();

        SalariuIstoric achitat = angajatWorkflow.confirmaPlataSalariu(antrenor.getIdUtilizator(), idPlata,
                manager.getIdUtilizator());

        assertEquals(StatusPlataSalariu.ACHITAT, achitat.getStatusPlata());
        assertEquals(AZI, achitat.getDataPlatii());
        assertRegula("RB05", () -> angajatWorkflow.confirmaPlataSalariu(antrenor.getIdUtilizator(), idPlata,
                manager.getIdUtilizator()));
    }

    @Test
    void plataSalarii_doarDeManager_UC20() {
        assertRegula("UC20", () -> angajatWorkflow.genereazaSalarii(OCTOMBRIE, receptioner.getIdUtilizator()));
    }
}
