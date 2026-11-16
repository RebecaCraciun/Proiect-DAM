package ro.dam.salafitness.domain.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.ClasaFitness;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.domain.model.enums.StatusRezervare;
import ro.dam.salafitness.domain.model.vo.IntervalOrar;
import ro.dam.salafitness.support.AbstractRepositoryTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Sprint 3: depozitul agregatului Clasa Fitness
class ClasaFitnessRepositoryTest extends AbstractRepositoryTest {

    private static final LocalDateTime MAINE_10 = ACUM.plusDays(1);

    @Autowired
    private ClasaFitnessRepository clasaFitnessRepository;

    private ClasaFitness yoga;
    private ProgramClasa sesiune;

    @BeforeEach
    void pregatesteClasaSiSesiunea() {
        yoga = clasaFitnessRepository.save(new ClasaFitness("Yoga", antrenor.getIdUtilizator(), 2, "Hatha"));
        sesiune = yoga.programeazaSesiune(IntervalOrar.intre(MAINE_10, MAINE_10.plusHours(1)), "Studio 1");
        clasaFitnessRepository.flush();
    }

    @Test
    void findSesiuniSuprapuse_intervalSemiDeschis_RA12() {
        assertEquals(1, clasaFitnessRepository.findSesiuniSuprapuse("Studio 1",
                MAINE_10.plusMinutes(30), MAINE_10.plusMinutes(90)).size());
        assertEquals(1, clasaFitnessRepository.findSesiuniSuprapuse("studio 1",
                MAINE_10.minusMinutes(30), MAINE_10.plusMinutes(10)).size());
        // sesiunile care doar se ating nu se suprapun
        assertTrue(clasaFitnessRepository.findSesiuniSuprapuse("Studio 1",
                MAINE_10.plusHours(1), MAINE_10.plusHours(2)).isEmpty());
        assertTrue(clasaFitnessRepository.findSesiuniSuprapuse("Studio 2",
                MAINE_10, MAINE_10.plusHours(1)).isEmpty());
    }

    @Test
    void findSesiuniInPerioada_UC08_siFindToateSesiunile() {
        sincronizeaza();

        assertEquals(1, clasaFitnessRepository.findSesiuniInPerioada(ACUM, ACUM.plusDays(2)).size());
        assertTrue(clasaFitnessRepository.findSesiuniInPerioada(ACUM.plusDays(2), ACUM.plusDays(3)).isEmpty());
        assertEquals("Yoga", clasaFitnessRepository.findToateSesiunile().get(0).getClasa().getDenumire());
    }

    @Test
    void rezervarea_seSalveazaPrinCascadaPeDouaNiveluri_RA02() {
        Client ana = client("ana@sala.ro");
        yoga.rezerva(sesiune.getIdProgram(), ana.getIdUtilizator(), ACUM);
        sincronizeaza();

        assertTrue(clasaFitnessRepository.existaRezervareClient(ana.getIdUtilizator()));
        assertFalse(clasaFitnessRepository.existaRezervareClient(-1));
        assertEquals("Confirmat", valoareNativa(
                "select status from rezervari_clase where id_utilizator_client = :id", ana.getIdUtilizator()));

        // RA01: citire cu PESSIMISTIC_WRITE
        ProgramClasa blocata = clasaFitnessRepository.findSesiunePentruRezervare(sesiune.getIdProgram()).orElseThrow();
        assertEquals(1, blocata.getRezervari().size());
        assertEquals(StatusRezervare.CONFIRMAT, blocata.getRezervari().get(0).getStatus());
        assertEquals(1, blocata.locuriDisponibile());
    }

    @Test
    void findByIdAntrenor_siExistsByIdAntrenor() {
        assertEquals(1, clasaFitnessRepository.findByIdAntrenor(antrenor.getIdUtilizator()).size());
        assertTrue(clasaFitnessRepository.existsByIdAntrenor(antrenor.getIdUtilizator()));
        assertFalse(clasaFitnessRepository.existsByIdAntrenor(tehnician.getIdUtilizator()));
    }
}