package ro.dam.salafitness.service.computation;

import ro.dam.salafitness.domain.model.entity.AbonamentClient;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.enums.StatusAbonament;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.model.vo.PerioadaValabilitate;
import ro.dam.salafitness.service.computation.view.AbonamentActivView;

import java.time.LocalDate;
import java.util.List;

/**
 * Reguli de calcul pentru abonamente (RC04, RC05, RC06, RC10, V1).
 */
public interface AbonamenteComputationService {

    /** UC03 pas 3: data curenta sau ziua de dupa expirarea abonamentului activ */
    LocalDate calculeazaDataInceputAbonament(Client client, LocalDate azi);

    /** RC04: data_sfarsit = data_inceput + durata_zile - 1 */
    PerioadaValabilitate calculeazaPerioada(TipAbonament tip, LocalDate dataInceput);

    /** RC05: Activ daca azi &le; data_sfarsit, altfel Expirat (actualizeaza entitatea) */
    StatusAbonament calculeazaStatusAbonament(AbonamentClient abonament, LocalDate azi);

    /** RC05 pentru toate abonamentele din baza de date, la data curenta; intoarce cate s-au schimbat */
    int actualizeazaStatusAbonamente();

    /** RC06: suma platii = pretul tipului de abonament la momentul achizitiei */
    Bani calculeazaSumaPlataAbonament(TipAbonament tip);

    /** RC10: denumirea tipului ultimului abonament cumparat (actualizeaza clientul) */
    String calculeazaTipAbonamentClient(Client client);

    /** V1 */
    List<AbonamentActivView> abonamenteActive(LocalDate data);
}
