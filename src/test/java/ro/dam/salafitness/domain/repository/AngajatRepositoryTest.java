package ro.dam.salafitness.domain.repository;

import org.junit.jupiter.api.Test;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;
import ro.dam.salafitness.domain.model.entity.SalariuIstoric;
import ro.dam.salafitness.domain.model.enums.StatusPlataSalariu;
import ro.dam.salafitness.support.AbstractRepositoryTest;

import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Sprint 3: depozitul agregatului Angajat
class AngajatRepositoryTest extends AbstractRepositoryTest {

    @Test
    void pontajele_seSalveazaPrinCascada_iarFiltrulIntoarceDoarPontajeleInchise_RC03() {
        antrenor.checkIn(ACUM.withHour(7));
        antrenor.checkOut(ACUM.withHour(15).withMinute(30));
        receptioner.checkIn(ACUM);
        sincronizeaza();

        List<PontajAngajat> inchise = angajatRepository.findPontajeInchise();
        assertEquals(1, inchise.size());
        assertEquals(antrenor.getIdUtilizator(), inchise.get(0).getAngajat().getIdUtilizator());
        assertTrue(egal("8.50", inchise.get(0).getOreLucrate()));
    }

    @Test
    void findByRol_dupaDenumireaDinNomenclator_faraMajuscule() {
        assertEquals(List.of(antrenor.getIdUtilizator()),
                angajatRepository.findByRol("antrenor").stream().map(Angajat::getIdUtilizator).toList());
        assertEquals(1, angajatRepository.findByRol("Recepționer").size());
        assertTrue(angajatRepository.findByRol("Inexistent").isEmpty());
    }

    @Test
    void findByIdTipAngajat_operatieDerivataDinNume() {
        assertEquals(List.of(tehnician.getIdUtilizator()),
                angajatRepository.findByIdTipAngajat(tehnician.getIdTipAngajat()).stream()
                        .map(Angajat::getIdUtilizator).toList());
    }

    @Test
    void salariul_seSalveazaPrinCascada_iarStatusulCuDiacriticeSeConverteste_RV12() {
        SalariuIstoric plata = manager.genereazaSalariu(YearMonth.of(2026, 10));
        sincronizeaza();

        assertNotNull(plata.getIdPlata());
        assertEquals("În așteptare", valoareNativa(
                "select status_plata from salarii_istoric where id_plata = :id", plata.getIdPlata()));
        Angajat citit = angajatRepository.findById(manager.getIdUtilizator()).orElseThrow();
        assertEquals(StatusPlataSalariu.IN_ASTEPTARE, citit.getSalarii().get(0).getStatusPlata());
    }
}