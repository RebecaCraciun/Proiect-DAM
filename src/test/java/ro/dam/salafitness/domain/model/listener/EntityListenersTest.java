package ro.dam.salafitness.domain.model.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import ro.dam.salafitness.domain.model.entity.AuditLog;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.Echipament;
import ro.dam.salafitness.domain.model.entity.LinieVanzare;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.Vanzare;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.repository.EchipamentRepository;
import ro.dam.salafitness.domain.repository.VanzareRepository;
import ro.dam.salafitness.service.audit.AuditService;
import ro.dam.salafitness.support.AbstractRepositoryTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ro.dam.salafitness.domain.model.listener.AuditEntitateListener.CREARE;
import static ro.dam.salafitness.domain.model.listener.AuditEntitateListener.MODIFICARE;
import static ro.dam.salafitness.domain.model.listener.AuditEntitateListener.STERGERE;

// Sprint 3: regulile de calcul si audit din @EntityListeners
class EntityListenersTest extends AbstractRepositoryTest {

    @Autowired
    private VanzareRepository vanzareRepository;
    @Autowired
    private EchipamentRepository echipamentRepository;
    @Autowired
    private AuditService auditService;

    private Client ana;
    private Produs proteina;

    @BeforeEach
    void pregatesteClientSiProdus() {
        ana = client("ana@sala.ro");
        proteina = produs("Proteina whey", "149.90", 10);
    }

    // ---------- calcul (CampuriCalculateListener)

    @Test
    void prePersist_recalculeazaOreleLucrate_RC03() {
        antrenor.checkIn(ACUM.withHour(8));
        PontajAngajat pontaj = antrenor.checkOut(ACUM.withHour(10).withMinute(15));
        // stricam valoarea, ocolind metodele entitatii
        ReflectionTestUtils.setField(pontaj, "oreLucrate", new BigDecimal("99.00"));

        angajatRepository.flush();

        assertTrue(egal("2.25", zecimalNativ(
                "select ore_lucrate from pontaj_angajati where id_pontaj = :id", pontaj.getIdPontaj())));
    }

    @Test
    void prePersist_recalculeazaTotalulVanzarii_RC02() {
        Vanzare vanzare = new Vanzare(ana.getIdUtilizator(), receptioner.getIdUtilizator(), ACUM);
        vanzare.adaugaLinie(proteina, 2);
        ReflectionTestUtils.setField(vanzare, "totalPlata", Bani.of("1.00"));

        vanzareRepository.saveAndFlush(vanzare);

        assertTrue(egal("299.80", zecimalNativ(
                "select total_plata from vanzari where id_vanzare = :id", vanzare.getIdVanzare())));
    }

    @Test
    void preUpdate_recalculeazaTotalulLiniei_RC01() {
        Vanzare vanzare = new Vanzare(ana.getIdUtilizator(), receptioner.getIdUtilizator(), ACUM);
        LinieVanzare linie = vanzare.adaugaLinie(proteina, 2);
        vanzareRepository.saveAndFlush(vanzare);

        ReflectionTestUtils.setField(linie, "totalLinie", Bani.of("1.00"));
        vanzareRepository.flush();

        assertTrue(egal("299.80", zecimalNativ(
                "select total_linie from linie_vanzare where id_linie_vanzare = :id", linie.getIdLinieVanzare())));
    }

    // ---------- audit (AuditEntitateListener)

    @Test
    void audit_creareModificareStergere_RB04() {
        Produs baton = produs("Baton proteic", "9.90", 100);
        Integer idBaton = baton.getIdProdus();
        baton.modificaPret(Bani.of("10.90"));
        produsRepository.flush();
        produsRepository.delete(baton);
        produsRepository.flush();

        List<AuditLog> istoric = auditService.istoricEntitate("Produs", idBaton);
        assertEquals(Set.of(CREARE, MODIFICARE, STERGERE),
                istoric.stream().map(AuditLog::getActiune).collect(Collectors.toSet()));
        assertEquals(3, istoric.size());
        AuditLog modificare = istoric.stream().filter(a -> a.getActiune().equals(MODIFICARE)).findFirst().orElseThrow();
        assertTrue(modificare.getDetalii().contains("10.90"), modificare.getDetalii());
        assertTrue(istoric.stream().allMatch(a -> a.getIdUtilizator() == null));
    }

    @Test
    void audit_faraDatePersonale_RB07() {
        clientRepository.flush();

        List<AuditLog> istoric = auditService.istoricEntitate("Client", ana.getIdUtilizator());
        assertEquals(1, istoric.size());
        assertEquals(CREARE, istoric.get(0).getActiune());
        assertNull(istoric.get(0).getDetalii());
    }

    @Test
    void audit_entitateleNeauditabileNuProducIntrari() {
        Echipament banda = echipamentRepository.saveAndFlush(new Echipament("Banda", null, "Bun", "Zona cardio"));

        assertTrue(auditService.istoricEntitate("Echipament", banda.getIdEchipament()).isEmpty());
    }
}