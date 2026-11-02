package ro.dam.salafitness.service.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.factory.DateClientNou;
import ro.dam.salafitness.domain.model.entity.AccesSala;
import ro.dam.salafitness.domain.model.entity.ClasaFitness;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.SedintaPT;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.model.vo.IntervalOrar;
import ro.dam.salafitness.domain.model.vo.PerioadaValabilitate;
import ro.dam.salafitness.domain.repository.ClasaFitnessRepository;
import ro.dam.salafitness.domain.repository.SedintaPTRepository;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reguli de validare: JavaBean Validation pe entitati (RV01-RV23) si validari de agregat din DomainValidationService.
 */
class DomainValidationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private DomainValidationService validationService;
    @Autowired
    private ClasaFitnessRepository clasaFitnessRepository;
    @Autowired
    private SedintaPTRepository sedintaPTRepository;

    // ---------- JavaBean Validation (adnotari in entitati si value objects) ----------

    @Test
    void produsCuPretSiStocNegative_incalcaRV19() {
        Produs produs = new Produs("Proteina", "Suplimente", Bani.of("-1"), -5);

        Set<String> campuri = validationService.verifica(produs).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("pret.valoare", "stocCurent"), campuri);
    }

    @Test
    void tipAbonamentCuDurataZero_incalcaRV08() {
        TipAbonament tip = new TipAbonament("Gratuit", 0, Bani.ZERO);

        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> validationService.valideaza(tip));
        assertEquals("durataZile", ex.getConstraintViolations().iterator().next().getPropertyPath().toString());
    }

    @Test
    void emailInvalidSauNumePreaLung_suntRespinseDeFabrica_RV01_RV02() {
        DateClientNou date = new DateClientNou("x".repeat(101), "Ana", "nu-este-email", "parola", null, null, null);

        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> clientFactory.creeazaClient(date));

        Set<String> campuri = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
        assertEquals(Set.of("nume", "email.adresa"), campuri);
    }

    @Test
    void parolaLipsa_esteRespinsa_RV03() {
        DateClientNou date = new DateClientNou("Pop", "Ana", "ana@x.ro", " ", null, null, null);

        assertThrows(ConstraintViolationException.class, () -> clientFactory.creeazaClient(date));
    }

    @Test
    void parolaSeStocheazaDoarCaHash_RV03_siDataInregistrariiEsteCompletata_RV04() {
        Client client = client("ana@x.ro");

        assertNotEquals("parola123", client.getParola());
        assertTrue(client.getParola().startsWith("$2"), "hash BCrypt");
        assertEquals(ACUM, client.getDataInregistrare());
    }

    @Test
    void ratingInAfaraIntervalului_incalcaRV23() {
        Client client = client("ana@x.ro");

        assertThrows(ConstraintViolationException.class,
                () -> validationService.valideaza(client.adaugaFeedback(6, "excelent", ACUM)));
        assertTrue(validationService.verifica(client.adaugaFeedback(5, "excelent", ACUM)).isEmpty());
    }

    @Test
    void intervalOrarInversat_incalcaRV15() {
        IntervalOrar interval = IntervalOrar.intre(ACUM.plusHours(2), ACUM.plusHours(1));

        Set<ConstraintViolation<IntervalOrar>> violari = validationService.verifica(interval);

        assertEquals(1, violari.size());
        assertEquals("intervalValid", violari.iterator().next().getPropertyPath().toString());
    }

    @Test
    void perioadaInversata_incalcaRV09() {
        assertFalse(validationService.verifica(PerioadaValabilitate.intre(AZI, AZI.minusDays(1))).isEmpty());
        assertTrue(validationService.verifica(PerioadaValabilitate.intre(AZI, AZI)).isEmpty());
    }

    @Test
    void iesireInainteaIntrarii_incalcaRV18() {
        AccesSala acces = AccesSala.permis(1, ACUM);

        assertRegula("RV18", () -> acces.inregistreazaIesire(ACUM.minusMinutes(1)));
    }

    // ---------- Validari de agregat ----------

    @Test
    void emailFolosit_incalcaRV01_indiferentDeMajuscule() {
        client("ana@x.ro");

        assertRegula("RV01", () -> validationService.verificaEmailUnic("  ANA@x.ro "));
        assertDoesNotThrow(() -> validationService.verificaEmailUnic("ion@x.ro"));
    }

    @Test
    void denumireTipAbonamentDuplicata_incalcaRV07() {
        tipAbonament("Lunar", 30, "150");

        assertRegula("RV07", () -> validationService.verificaDenumireTipAbonamentUnica("lunar"));
    }

    @Test
    void rolurileAngajatilor_RA09_RA10_RA11() {
        assertEquals(antrenor, validationService.verificaAntrenor(antrenor.getIdUtilizator()));
        assertEquals(tehnician, validationService.verificaTehnician(tehnician.getIdUtilizator()));
        assertEquals(receptioner, validationService.verificaReceptioner(receptioner.getIdUtilizator()));

        assertRegula("RA09", () -> validationService.verificaAntrenor(receptioner.getIdUtilizator()));
        assertRegula("RA10", () -> validationService.verificaTehnician(antrenor.getIdUtilizator()));
        assertRegula("RA11", () -> validationService.verificaReceptioner(manager.getIdUtilizator()));
    }

    @Test
    void unClientNuEsteAngajat_RA11() {
        Client client = client("ana@x.ro");

        assertRegula("RA11", () -> validationService.verificaReceptioner(client.getIdUtilizator()));
    }

    @Test
    void abonamentValidLaData_UC07_RA03() {
        Client client = client("ana@x.ro");
        assertFalse(validationService.areAbonamentValid(client.getIdUtilizator(), AZI));
        assertRegula("RA03", () -> validationService.verificaAbonamentValid(client.getIdUtilizator(), AZI, "RA03"));

        client.cumparaAbonament(tipAbonament("Lunar", 30, "150"), AZI);
        clientRepository.flush();

        assertTrue(validationService.areAbonamentValid(client.getIdUtilizator(), AZI));
        assertTrue(validationService.areAbonamentValid(client.getIdUtilizator(), AZI.plusDays(29)));
        assertFalse(validationService.areAbonamentValid(client.getIdUtilizator(), AZI.plusDays(30)));
    }

    @Test
    void salaOcupataDeAltaClasa_incalcaRA12() {
        ClasaFitness yoga = clasaFitnessRepository.save(new ClasaFitness("Yoga", antrenor.getIdUtilizator(), 10, null));
        yoga.programeazaSesiune(IntervalOrar.intre(ACUM.plusDays(1), ACUM.plusDays(1).plusHours(1)), "Studio 1");
        clasaFitnessRepository.flush();

        assertRegula("RA12", () -> validationService.verificaSalaLibera("studio 1",
                IntervalOrar.intre(ACUM.plusDays(1).plusMinutes(30), ACUM.plusDays(1).plusMinutes(90)), null));
        // intervalele semi-deschise care doar se ating nu se suprapun; alta sala este libera
        assertDoesNotThrow(() -> validationService.verificaSalaLibera("Studio 1",
                IntervalOrar.intre(ACUM.plusDays(1).plusHours(1), ACUM.plusDays(1).plusHours(2)), null));
        assertDoesNotThrow(() -> validationService.verificaSalaLibera("Studio 2",
                IntervalOrar.intre(ACUM.plusDays(1), ACUM.plusDays(1).plusHours(1)), null));
    }

    @Test
    void antrenorCuSedintaLaAceeasiOra_incalcaRA13() {
        Integer idAntrenor = antrenor.getIdUtilizator();
        SedintaPT sedinta = sedintaPTRepository.save(new SedintaPT(idAntrenor, 999, ACUM.plusDays(1)));

        assertRegula("RA13", () -> validationService.verificaDisponibilitateAntrenor(idAntrenor,
                ACUM.plusDays(1).plusMinutes(30)));
        assertDoesNotThrow(() -> validationService.verificaDisponibilitateAntrenor(idAntrenor,
                ACUM.plusDays(1).plusHours(1)));

        // o sedinta anulata nu mai blocheaza antrenorul
        sedinta.anuleaza();
        sedintaPTRepository.flush();
        assertDoesNotThrow(() -> validationService.verificaDisponibilitateAntrenor(idAntrenor,
                ACUM.plusDays(1).plusMinutes(30)));
    }

    @Test
    void clientulSiAngajatulAuProfiluriDisjuncte_RB08() {
        assertDoesNotThrow(() -> validationService.verificaProfilUnic(client("ana@x.ro").getIdUtilizator()));
        assertDoesNotThrow(() -> validationService.verificaProfilUnic(manager.getIdUtilizator()));
    }
}
