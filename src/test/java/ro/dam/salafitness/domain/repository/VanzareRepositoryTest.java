package ro.dam.salafitness.domain.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.Vanzare;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.support.AbstractRepositoryTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Sprint 3: depozitul agregatului Vanzare
class VanzareRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private VanzareRepository vanzareRepository;

    private Client ana;
    private Produs proteina;
    private Produs apa;

    @BeforeEach
    void pregatesteProduse() {
        ana = client("ana@sala.ro");
        proteina = produs("Proteina whey", "149.90", 10);
        apa = produs("Apa plata", "4.50", 50);
    }

    private Vanzare vanzare(LocalDateTime moment) {
        Vanzare vanzare = new Vanzare(ana.getIdUtilizator(), receptioner.getIdUtilizator(), moment);
        vanzare.adaugaLinie(proteina, 2);
        vanzare.adaugaLinie(apa, 3);
        return vanzareRepository.save(vanzare);
    }

    @Test
    void liniile_seSalveazaPrinCascadaAll() {
        Vanzare vanzare = vanzare(ACUM);
        sincronizeaza();

        Vanzare citita = vanzareRepository.findById(vanzare.getIdVanzare()).orElseThrow();
        assertEquals(2, citita.getLinii().size());
        assertEquals(Bani.of("313.30"), citita.getTotalPlata());
    }

    @Test
    void liniaScoasaDinVanzare_seStergeDinBD_orphanRemoval() {
        Vanzare vanzare = vanzare(ACUM);
        sincronizeaza();

        vanzareRepository.findById(vanzare.getIdVanzare()).orElseThrow().eliminaLinie(apa.getIdProdus());
        sincronizeaza();

        assertEquals(1, numarNativ("select count(*) from linie_vanzare where id_vanzare = :id", vanzare.getIdVanzare()));
        assertEquals(Bani.of("299.80"), vanzareRepository.findById(vanzare.getIdVanzare()).orElseThrow().getTotalPlata());
    }

    @Test
    void stergereaVanzarii_ii_stergeLiniile_RB02() {
        Vanzare vanzare = vanzare(ACUM);
        sincronizeaza();

        vanzareRepository.deleteById(vanzare.getIdVanzare());
        sincronizeaza();

        assertEquals(0, numarNativ("select count(*) from linie_vanzare where id_vanzare = :id", vanzare.getIdVanzare()));
    }

    @Test
    void existaLinieCuProdus_RB01() {
        Produs prosop = produs("Prosop", "35.00", 30);
        vanzare(ACUM);
        sincronizeaza();

        assertTrue(vanzareRepository.existaLinieCuProdus(proteina.getIdProdus()));
        assertFalse(vanzareRepository.existaLinieCuProdus(prosop.getIdProdus()));
    }

    @Test
    void filtreCustom_dupaPerioadaSiDupaClient() {
        vanzare(ACUM.minusDays(40));
        Vanzare octombrie = vanzare(ACUM);
        sincronizeaza();

        List<Vanzare> dinOctombrie = vanzareRepository.findInPerioadaCuLinii(
                ACUM.withDayOfMonth(1).toLocalDate().atStartOfDay(), ACUM.plusDays(1));
        assertEquals(1, dinOctombrie.size());
        assertEquals(2, dinOctombrie.get(0).getLinii().size());
        List<Vanzare> aleClientului = vanzareRepository.findByIdClientOrderByDataVanzareDesc(ana.getIdUtilizator());
        assertEquals(2, aleClientului.size());
        assertEquals(octombrie.getIdVanzare(), aleClientului.get(0).getIdVanzare());
    }
}