package ro.dam.salafitness.service.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.ClasaFitness;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.domain.model.entity.RezervareClasa;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.enums.MetodaPlata;
import ro.dam.salafitness.domain.model.enums.RolAngajat;
import ro.dam.salafitness.domain.model.enums.StatusRezervare;
import ro.dam.salafitness.domain.model.vo.IntervalOrar;
import ro.dam.salafitness.service.computation.view.OcupareClasaView;
import ro.dam.salafitness.support.AbstractIntegrationTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * UC08, UC09 (cu UC07, UC10), UC11, UC15, UC22: RA01-RA04, RA09, RA12, RC09.
 */
class ClasaFitnessWorkflowServiceTest extends AbstractIntegrationTest {

    private static final LocalDateTime MAINE_18 = ACUM.plusDays(1).withHour(18);

    @Autowired
    private ClasaFitnessWorkflowService clasaWorkflow;
    @Autowired
    private ClientWorkflowService clientWorkflow;

    private TipAbonament lunar;
    private ClasaFitness yoga;
    private ProgramClasa sesiune;

    @BeforeEach
    void pregatesteClasa() {
        lunar = tipAbonament("Lunar", 30, "150");
        yoga = clasaWorkflow.definesteClasa("Yoga", antrenor.getIdUtilizator(), 2, "Hatha yoga",
                manager.getIdUtilizator());
        sesiune = clasaWorkflow.programeazaSesiune(yoga.getIdClasa(), IntervalOrar.intre(MAINE_18, MAINE_18.plusHours(1)),
                "Studio 1", manager.getIdUtilizator());
    }

    private Client clientCuAbonament(String email) {
        Client client = client(email);
        clientWorkflow.cumparaAbonament(client.getIdUtilizator(), lunar.getIdTipAbonament(), MetodaPlata.CARD,
                receptioner.getIdUtilizator());
        return client;
    }

    @Test
    void definireClasa_antrenorulTrebuieSaFieAntrenor_RA09() {
        assertNotNull(yoga.getIdClasa());
        assertNotNull(sesiune.getIdProgram());
        assertRegula("RA09", () -> clasaWorkflow.definesteClasa("Box", receptioner.getIdUtilizator(), 10, null,
                manager.getIdUtilizator()));
    }

    @Test
    void definireClasa_doarManagerul_UC22() {
        assertRegula("UC22", () -> clasaWorkflow.definesteClasa("Box", antrenor.getIdUtilizator(), 10, null,
                antrenor.getIdUtilizator()));
    }

    @Test
    void programareSesiune_salaOcupataDeAltaClasa_RA12() {
        ClasaFitness pilates = clasaWorkflow.definesteClasa("Pilates", antrenor.getIdUtilizator(), 8, null,
                manager.getIdUtilizator());

        assertRegula("RA12", () -> clasaWorkflow.programeazaSesiune(pilates.getIdClasa(),
                IntervalOrar.intre(MAINE_18.plusMinutes(30), MAINE_18.plusMinutes(90)), "Studio 1",
                manager.getIdUtilizator()));
        ProgramClasa altaSala = clasaWorkflow.programeazaSesiune(pilates.getIdClasa(),
                IntervalOrar.intre(MAINE_18.plusMinutes(30), MAINE_18.plusMinutes(90)), "Studio 2",
                manager.getIdUtilizator());
        assertNotNull(altaSala.getIdProgram());
    }

    @Test
    void rezervare_UC09_siProgramulAfiseazaLocurileLibere_UC08() {
        Client ana = clientCuAbonament("ana@x.ro");

        RezervareClasa rezervare = clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), ana.getIdUtilizator());

        assertNotNull(rezervare.getIdRezervare());
        assertEquals(StatusRezervare.CONFIRMAT, rezervare.getStatus());
        List<OcupareClasaView> program = clasaWorkflow.programClase(ACUM, ACUM.plusDays(7));
        assertEquals(1, program.size());
        assertEquals(1, program.get(0).locuriLibere());
    }

    @Test
    void rezervare_faraAbonamentValidLaDataSesiunii_RA03() {
        Client ana = client("ana@x.ro");

        assertRegula("RA03", () -> clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), ana.getIdUtilizator()));
    }

    @Test
    void rezervare_dublaLaAceeasiSesiune_RA02() {
        Client ana = clientCuAbonament("ana@x.ro");
        clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), ana.getIdUtilizator());

        assertRegula("RA02", () -> clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), ana.getIdUtilizator()));
    }

    @Test
    void rezervare_clasaCompleta_RA01() {
        clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), clientCuAbonament("ana@x.ro").getIdUtilizator());
        clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), clientCuAbonament("ion@x.ro").getIdUtilizator());
        Client alTreilea = clientCuAbonament("maria@x.ro");

        assertRegula("RA01", () -> clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), alTreilea.getIdUtilizator()));
    }

    @Test
    void rezervare_dupaInceperea_RA03() {
        Client ana = clientCuAbonament("ana@x.ro");
        ceas.seteaza(MAINE_18.plusMinutes(5));

        assertRegula("RA03", () -> clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), ana.getIdUtilizator()));
    }

    @Test
    void anulare_elibereazaLoculSiRezervareaSePoateReface_UC11() {
        Client ana = clientCuAbonament("ana@x.ro");
        Client ion = clientCuAbonament("ion@x.ro");
        clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), ana.getIdUtilizator());
        clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), ion.getIdUtilizator());

        RezervareClasa anulata = clasaWorkflow.anuleazaRezervare(sesiune.getIdProgram(), ana.getIdUtilizator());
        assertEquals(StatusRezervare.ANULAT, anulata.getStatus());
        assertEquals(1, sesiune.locuriDisponibile());

        // UNIQUE(client, program): rezervarea anulata se reconfirma, nu se dubleaza
        RezervareClasa refacuta = clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), ana.getIdUtilizator());
        assertSame(anulata, refacuta);
        assertEquals(StatusRezervare.CONFIRMAT, refacuta.getStatus());
    }

    @Test
    void anulare_dupaInceperea_RA04() {
        Client ana = clientCuAbonament("ana@x.ro");
        clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), ana.getIdUtilizator());
        ceas.seteaza(MAINE_18);

        assertRegula("RA04", () -> clasaWorkflow.anuleazaRezervare(sesiune.getIdProgram(), ana.getIdUtilizator()));
    }

    @Test
    void marcarePrezenta_doarDupaInceputSiDoarDeAntrenorulTitular_UC15_RA04() {
        Client ana = clientCuAbonament("ana@x.ro");
        clasaWorkflow.rezervaLoc(sesiune.getIdProgram(), ana.getIdUtilizator());
        Long idProgram = sesiune.getIdProgram();
        Integer idAna = ana.getIdUtilizator();

        assertRegula("RA04", () -> clasaWorkflow.marcheazaPrezenta(idProgram, idAna, antrenor.getIdUtilizator()));

        ceas.seteaza(MAINE_18);
        ceas.avanseaza(Duration.ofMinutes(10));
        Angajat altAntrenor = angajat(RolAngajat.ANTRENOR, "alt.antrenor@sala.ro");
        assertRegula("RA04", () -> clasaWorkflow.marcheazaPrezenta(idProgram, idAna, altAntrenor.getIdUtilizator()));

        RezervareClasa prezent = clasaWorkflow.marcheazaPrezenta(idProgram, idAna, antrenor.getIdUtilizator());
        assertEquals(StatusRezervare.PARTICIPAT, prezent.getStatus());
    }
}
