package ro.dam.salafitness.service.workflow;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * UC21 Gestionare tipuri abonament, UC23 Gestionare produse si stoc: RV07, RV08, RV19, RB06.
 */
class NomenclatoareWorkflowServiceTest extends AbstractIntegrationTest {

    @Autowired
    private NomenclatoareWorkflowService nomenclatoare;

    @Test
    void definireTipAbonament_UC21() {
        TipAbonament tip = nomenclatoare.definesteTipAbonament("Trimestrial", 90, new BigDecimal("400"),
                manager.getIdUtilizator());

        assertNotNull(tip.getIdTipAbonament());
        assertEquals(Bani.of("400"), tip.getPret());
    }

    @Test
    void definireTipAbonament_denumireDuplicata_RV07() {
        nomenclatoare.definesteTipAbonament("Lunar", 30, new BigDecimal("150"), manager.getIdUtilizator());

        assertRegula("RV07", () -> nomenclatoare.definesteTipAbonament("LUNAR", 31, new BigDecimal("160"),
                manager.getIdUtilizator()));
    }

    @Test
    void definireTipAbonament_durataSauPretInvalid_RV08() {
        Integer idManager = manager.getIdUtilizator();

        assertThrows(ConstraintViolationException.class,
                () -> nomenclatoare.definesteTipAbonament("Zero", 0, new BigDecimal("10"), idManager));
        assertThrows(ConstraintViolationException.class,
                () -> nomenclatoare.definesteTipAbonament("Negativ", 30, new BigDecimal("-10"), idManager));
    }

    @Test
    void modificareTipAbonament_doarDeManager_UC21() {
        TipAbonament tip = nomenclatoare.definesteTipAbonament("Lunar", 30, new BigDecimal("150"),
                manager.getIdUtilizator());

        nomenclatoare.modificaTipAbonament(tip.getIdTipAbonament(), null, new BigDecimal("170"), manager.getIdUtilizator());

        assertEquals(Bani.of("170"), tip.getPret());
        assertEquals(30, tip.getDurataZile());
        assertRegula("UC21", () -> nomenclatoare.modificaTipAbonament(tip.getIdTipAbonament(), 31, null,
                receptioner.getIdUtilizator()));
    }

    @Test
    void produse_adaugareSiReceptieStoc_deReceptie_UC23() {
        Produs baton = nomenclatoare.adaugaProdus("Baton proteic", "Snacks", new BigDecimal("9.90"), 24,
                receptioner.getIdUtilizator());

        nomenclatoare.receptioneazaStoc(baton.getIdProdus(), 12, receptioner.getIdUtilizator());

        assertNotNull(baton.getIdProdus());
        assertEquals(36, baton.getStocCurent());
        assertRegula("RV19", () -> nomenclatoare.receptioneazaStoc(baton.getIdProdus(), 0, receptioner.getIdUtilizator()));
        assertRegula("UC23", () -> nomenclatoare.adaugaProdus("Apa", null, BigDecimal.ONE, 1, antrenor.getIdUtilizator()));
    }

    @Test
    void produse_modificarePret_doarDeManager_siNenegativ_UC23_RV19() {
        Produs baton = nomenclatoare.adaugaProdus("Baton proteic", "Snacks", new BigDecimal("9.90"), 24,
                manager.getIdUtilizator());

        nomenclatoare.modificaPretProdus(baton.getIdProdus(), new BigDecimal("10.50"), manager.getIdUtilizator());

        assertEquals(Bani.of("10.50"), baton.getPret());
        assertRegula("UC23", () -> nomenclatoare.modificaPretProdus(baton.getIdProdus(), BigDecimal.ONE,
                receptioner.getIdUtilizator()));
        assertThrows(ConstraintViolationException.class, () -> nomenclatoare.modificaPretProdus(baton.getIdProdus(),
                new BigDecimal("-1"), manager.getIdUtilizator()));
    }
}
