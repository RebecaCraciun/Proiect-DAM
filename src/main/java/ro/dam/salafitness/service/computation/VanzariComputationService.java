package ro.dam.salafitness.service.computation;

import ro.dam.salafitness.domain.model.entity.LinieVanzare;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.Vanzare;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.service.computation.view.VenitLunarView;

import java.util.List;

/**
 * Reguli de calcul pentru vanzari si venituri (RC01, RC02, RC07, RC08, V3).
 */
public interface VanzariComputationService {

    /** RC01: total_linie = cantitate x pret_unitar */
    Bani calculeazaTotalLinie(LinieVanzare linie);

    /** RC02: total_plata = suma total_linie */
    Bani calculeazaTotalVanzare(Vanzare vanzare);

    /** RC07: pret_unitar = pretul curent al produsului (snapshot) */
    Bani calculeazaPretUnitar(Produs produs);

    /** RC08: stocul ramas dupa vanzarea cantitatii */
    int calculeazaStocRamas(Produs produs, int cantitateVanduta);

    /** V3 */
    List<VenitLunarView> venituriLunare();
}
