package ro.dam.salafitness.domain.repository.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.Vanzare;
import ro.dam.salafitness.support.AbstractRepositoryTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Bonus: depozitul IVanzareRepository scris cu EntityManager
class VanzareRepositoryJPATest extends AbstractRepositoryTest {

    @Autowired
    private IVanzareRepository vanzareRepositoryJPA;

    private Client ana;
    private Produs proteina;
    private Produs apa;

    @BeforeEach
    void pregatesteProduse() {
        ana = client("ana@sala.ro");
        proteina = produs("Proteina whey", "149.90", 10);
        apa = produs("Apa plata", "4.50", 50);
    }

    private Vanzare vanzareNoua(LocalDateTime moment) {
        Vanzare vanzare = new Vanzare(ana.getIdUtilizator(), receptioner.getIdUtilizator(), moment);
        vanzare.adaugaLinie(proteina, 2);
        return vanzare;
    }

    @Test
    void crud_persistCitireNumarareStergere() {
        Vanzare vanzare = vanzareNoua(ACUM);

        // entitate noua -> persist
        Vanzare salvata = vanzareRepositoryJPA.save(vanzare);
        assertSame(vanzare, salvata);
        assertNotNull(salvata.getIdVanzare());
        Long id = salvata.getIdVanzare();
        sincronizeaza();

        assertEquals(1, vanzareRepositoryJPA.count());
        assertTrue(vanzareRepositoryJPA.existsById(id));
        assertEquals(1, vanzareRepositoryJPA.findById(id).orElseThrow().getLinii().size());
        assertEquals(List.of(id), vanzareRepositoryJPA.findAll().stream().map(Vanzare::getIdVanzare).toList());

        vanzareRepositoryJPA.deleteById(id);
        sincronizeaza();

        assertFalse(vanzareRepositoryJPA.existsById(id));
        assertEquals(0, vanzareRepositoryJPA.count());
        assertEquals(0, numarNativ("select count(*) from linie_vanzare where id_vanzare = :id", id));
    }

    @Test
    void save_peOEntitateDetasata_faceMerge() {
        Vanzare vanzare = vanzareRepositoryJPA.save(vanzareNoua(ACUM));
        // dupa clear(), vanzarea e detasata
        sincronizeaza();

        vanzare.adaugaLinie(apa, 3);
        // entitate cu id -> merge
        vanzareRepositoryJPA.save(vanzare);
        sincronizeaza();

        Vanzare citita = vanzareRepositoryJPA.findById(vanzare.getIdVanzare()).orElseThrow();
        assertEquals(2, citita.getLinii().size());
        assertTrue(egal("313.30", citita.getTotalPlata().getValoare()));
    }

    @Test
    void operatiiCustom_dupaClientSiPerioada() {
        vanzareRepositoryJPA.save(vanzareNoua(ACUM.minusDays(40)));
        Vanzare octombrie = vanzareRepositoryJPA.save(vanzareNoua(ACUM));
        sincronizeaza();
        LocalDateTime inceputOctombrie = ACUM.withDayOfMonth(1).toLocalDate().atStartOfDay();

        List<Vanzare> aleClientului = vanzareRepositoryJPA.findByClient(ana.getIdUtilizator());
        assertEquals(2, aleClientului.size());
        assertEquals(octombrie.getIdVanzare(), aleClientului.get(0).getIdVanzare());
        assertEquals(1, vanzareRepositoryJPA.findInPerioada(inceputOctombrie, ACUM.plusDays(1)).size());
        assertTrue(egal("299.80", vanzareRepositoryJPA.totalInPerioada(inceputOctombrie, ACUM.plusDays(1))));
        assertTrue(egal("0", vanzareRepositoryJPA.totalInPerioada(ACUM.plusDays(10), ACUM.plusDays(20))));
    }
}