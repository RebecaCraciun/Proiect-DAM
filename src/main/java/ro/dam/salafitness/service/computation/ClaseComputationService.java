package ro.dam.salafitness.service.computation;

import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.service.computation.view.OcupareClasaView;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reguli de calcul pentru clasele de fitness (RC09, V2).
 */
public interface ClaseComputationService {

    /** RC09: capacitate_maxima - nr. rezervari neanulate */
    int calculeazaLocuriLibere(ProgramClasa sesiune);

    /** V2 pentru toate sesiunile */
    List<OcupareClasaView> ocupareClase();

    /** V2 restrans la sesiunile care incep in [de, pana) - UC08 */
    List<OcupareClasaView> ocupareClase(LocalDateTime de, LocalDateTime pana);
}
