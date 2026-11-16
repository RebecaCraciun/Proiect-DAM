package ro.dam.salafitness.service.computation;

import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.service.computation.view.OreLucrateLunarView;

import java.math.BigDecimal;
import java.util.List;

/**
 * Reguli de calcul pentru angajati (RC03, RC11, V4).
 */
public interface AngajatiComputationService {

    /** RC03: (check_out - check_in) in ore, 2 zecimale; null pentru un pontaj deschis */
    BigDecimal calculeazaOreLucrate(PontajAngajat pontaj);

    /** RC11: suma salariala = salariul lunar valabil acum */
    Bani calculeazaSumaSalariu(Angajat angajat);

    /** V4 */
    List<OreLucrateLunarView> oreLucrateLunar();
}
