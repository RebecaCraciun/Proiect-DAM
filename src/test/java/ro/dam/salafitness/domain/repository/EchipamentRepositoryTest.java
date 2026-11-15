package ro.dam.salafitness.domain.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.Echipament;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.support.AbstractRepositoryTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Sprint 3: depozitul agregatului Echipament
class EchipamentRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private EchipamentRepository echipamentRepository;

    @Test
    void interventiile_seSalveazaPrinCascada_siFiltrele_RB01() {
        Echipament banda = echipamentRepository.save(
                new Echipament("Banda de alergare", LocalDate.of(2023, 9, 1), "Bun", "Zona cardio"));
        banda.inregistreazaMentenanta(AZI, "Curea uzata", Bani.of("120.00"), tehnician.getIdUtilizator(), "Reparat");
        sincronizeaza();

        Echipament citit = echipamentRepository.findById(banda.getIdEchipament()).orElseThrow();
        assertEquals(1, citit.getInterventii().size());
        assertEquals(Bani.of("120.00"), citit.costTotalMentenanta());
        assertTrue(echipamentRepository.existaInterventieCuResponsabil(tehnician.getIdUtilizator()));
        assertFalse(echipamentRepository.existaInterventieCuResponsabil(manager.getIdUtilizator()));
        assertEquals(1, echipamentRepository.findByStareUzuraIgnoreCase("REPARAT").size());
        assertEquals(1, echipamentRepository.findByLocatieInSalaContainingIgnoreCase("cardio").size());
    }
}