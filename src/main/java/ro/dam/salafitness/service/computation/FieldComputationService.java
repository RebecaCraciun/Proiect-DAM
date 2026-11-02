package ro.dam.salafitness.service.computation;

import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.AbonamentClient;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.LinieVanzare;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.entity.Vanzare;
import ro.dam.salafitness.domain.model.enums.StatusAbonament;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.model.vo.PerioadaValabilitate;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reguli de calcul pentru campurile entitatilor (README 3.3, RC01-RC11).
 */
public interface FieldComputationService {

    /** RC01: total_linie = cantitate x pret_unitar */
    Bani calculeazaTotalLinie(LinieVanzare linie);

    /** RC02: total_plata = suma total_linie */
    Bani calculeazaTotalVanzare(Vanzare vanzare);

    /** RC03: (check_out - check_in) in ore, 2 zecimale; null pentru un pontaj deschis */
    BigDecimal calculeazaOreLucrate(PontajAngajat pontaj);

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

    /** RC07: pret_unitar = pretul curent al produsului (snapshot) */
    Bani calculeazaPretUnitar(Produs produs);

    /** RC08: stocul ramas dupa vanzarea cantitatii */
    int calculeazaStocRamas(Produs produs, int cantitateVanduta);

    /** RC09: capacitate_maxima - nr. rezervari neanulate */
    int calculeazaLocuriLibere(ProgramClasa sesiune);

    /** RC10: denumirea tipului ultimului abonament cumparat (actualizeaza clientul) */
    String calculeazaTipAbonamentClient(Client client);

    /** RC11: suma salariala = salariul lunar valabil acum */
    Bani calculeazaSumaSalariu(Angajat angajat);
}
