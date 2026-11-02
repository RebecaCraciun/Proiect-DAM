package ro.dam.salafitness.service.workflow;

import ro.dam.salafitness.domain.factory.DateAngajatNou;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;
import ro.dam.salafitness.domain.model.entity.SalariuIstoric;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Workflow-urile agregatului Angajat: UC17, UC18, UC19, UC20.
 */
public interface AngajatWorkflowService {

    /** UC19: RV01-RV06, RB08; doar managerul angajeaza. */
    Angajat angajeaza(DateAngajatNou date, Integer idManager);

    /** UC19 / RC11: salariul nou nu modifica platile salariale deja generate. */
    Angajat modificaSalariu(Integer idAngajat, BigDecimal salariuNou, Integer idManager);

    /** UC17 / RA08: nu se poate face check-in cu un pontaj deschis. */
    PontajAngajat checkIn(Integer idAngajat);

    /** UC17 / RV11 / RC03 */
    PontajAngajat checkOut(Integer idAngajat);

    /** UC18: totalul orelor lucrate in luna indicata. */
    BigDecimal oreLucrate(Integer idAngajat, YearMonth luna);

    /** UC20 / RC11: genereaza plata lunara pentru toti angajatii care nu o au inca. */
    List<SalariuIstoric> genereazaSalarii(YearMonth luna, Integer idManager);

    /** UC20 / RB05: plata devine "Achitat" cu data de azi. */
    SalariuIstoric confirmaPlataSalariu(Integer idAngajat, Long idPlata, Integer idManager);
}
