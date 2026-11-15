package ro.dam.salafitness.service.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.LinieVanzare;
import ro.dam.salafitness.domain.model.entity.AuditLog;
import ro.dam.salafitness.domain.model.listener.AuditEntitateListener;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.Vanzare;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.repository.VanzareRepository;
import ro.dam.salafitness.service.audit.AuditService;
import ro.dam.salafitness.service.workflow.VanzareWorkflowService.LinieComanda;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UC14 Vanzare produse: RV19-RV21, RA11, RA14, RC01, RC02, RC07, RC08.
 */
class VanzareWorkflowServiceTest extends AbstractIntegrationTest {

    @Autowired
    private VanzareWorkflowService vanzareWorkflow;
    @Autowired
    private VanzareRepository vanzareRepository;
    @Autowired
    private AuditService auditService;

    private Integer idClient;
    private Integer idReceptioner;
    private Produs proteina;
    private Produs apa;

    @BeforeEach
    void pregateste() {
        idClient = client("ana@x.ro").getIdUtilizator();
        idReceptioner = receptioner.getIdUtilizator();
        proteina = produs("Proteina whey", "149.90", 10);
        apa = produs("Apa plata", "4.50", 50);
    }

    @Test
    void vanzare_UC14_calculeazaTotaluriSiScadeStocul() {
        Vanzare vanzare = vanzareWorkflow.vindeProduse(idClient, idReceptioner, List.of(
                new LinieComanda(proteina.getIdProdus(), 2),
                new LinieComanda(apa.getIdProdus(), 3)));

        assertNotNull(vanzare.getIdVanzare());
        assertEquals(2, vanzare.getLinii().size());
        assertEquals(Bani.of("299.80"), vanzare.getLinii().get(0).getTotalLinie());   // RC01
        assertEquals(Bani.of("313.30"), vanzare.getTotalPlata());                     // RC02
        assertEquals(8, proteina.getStocCurent());                                    // RC08
        assertEquals(47, apa.getStocCurent());
        // RB04: auditul de business (cu actorul) + auditul din listener
        List<AuditLog> istoric = auditService.istoricEntitate("Vanzare", vanzare.getIdVanzare());
        assertEquals(1, istoric.stream().filter(a -> a.getActiune().equals("VANZARE")
                && idReceptioner.equals(a.getIdUtilizator())).count());
        assertEquals(1, istoric.stream().filter(a -> a.getActiune().equals(AuditEntitateListener.CREARE)).count());
    }

    @Test
    void vanzare_acelasiProdusDeDouaOri_seCumuleazaIntr_oLinie() {
        Vanzare vanzare = vanzareWorkflow.vindeProduse(idClient, idReceptioner, List.of(
                new LinieComanda(apa.getIdProdus(), 2),
                new LinieComanda(apa.getIdProdus(), 3)));

        assertEquals(1, vanzare.getLinii().size());
        assertEquals(5, vanzare.getLinii().get(0).getCantitate());
        assertEquals(45, apa.getStocCurent());
    }

    @Test
    void vanzare_pretulLinieiRamaneCelDinMomentulVanzarii_RC07_RB06() {
        Vanzare vanzare = vanzareWorkflow.vindeProduse(idClient, idReceptioner,
                List.of(new LinieComanda(proteina.getIdProdus(), 1)));

        proteina.modificaPret(Bani.of("169.90"));

        LinieVanzare linie = vanzare.getLinii().get(0);
        assertEquals(Bani.of("149.90"), linie.getPretUnitar());
        assertEquals(Bani.of("149.90"), vanzare.getTotalPlata());
    }

    @Test
    void vanzare_stocInsuficient_nimicNuSeSalveaza_RA14() {
        assertRegula("RA14", () -> vanzareWorkflow.vindeProduse(idClient, idReceptioner, List.of(
                new LinieComanda(apa.getIdProdus(), 1),
                new LinieComanda(proteina.getIdProdus(), 11))));

        assertEquals(10, proteina.getStocCurent());
        assertEquals(50, apa.getStocCurent());
        assertEquals(0, vanzareRepository.count());
    }

    @Test
    void vanzare_cumulataPesteStoc_RA14() {
        assertRegula("RA14", () -> vanzareWorkflow.vindeProduse(idClient, idReceptioner, List.of(
                new LinieComanda(proteina.getIdProdus(), 6),
                new LinieComanda(proteina.getIdProdus(), 5))));
    }

    @Test
    void vanzare_faraLinii_RA14() {
        assertRegula("RA14", () -> vanzareWorkflow.vindeProduse(idClient, idReceptioner, List.of()));
    }

    @Test
    void vanzare_cantitateZero_RV20() {
        assertRegula("RV20", () -> vanzareWorkflow.vindeProduse(idClient, idReceptioner,
                List.of(new LinieComanda(apa.getIdProdus(), 0))));
    }

    @Test
    void vanzare_doarDeReceptioner_RA11() {
        assertRegula("RA11", () -> vanzareWorkflow.vindeProduse(idClient, manager.getIdUtilizator(),
                List.of(new LinieComanda(apa.getIdProdus(), 1))));
    }
}
