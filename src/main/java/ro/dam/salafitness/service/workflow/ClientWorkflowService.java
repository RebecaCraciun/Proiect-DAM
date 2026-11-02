package ro.dam.salafitness.service.workflow;

import ro.dam.salafitness.domain.factory.DateClientNou;
import ro.dam.salafitness.domain.model.entity.AbonamentClient;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.FeedbackClient;
import ro.dam.salafitness.domain.model.entity.Plata;
import ro.dam.salafitness.domain.model.enums.MetodaPlata;

import java.util.List;

/**
 * Workflow-urile agregatului Client: UC01, UC03 (cu UC04 inclus), UC05, UC13.
 */
public interface ClientWorkflowService {

    /** Rezultatul UC03: abonamentul nou si plata, inregistrate in aceeasi tranzactie. */
    record AchizitieAbonament(AbonamentClient abonament, Plata plata) {
    }

    /** Rezultatul UC05: abonamentul curent, zilele ramase si istoricul platilor. */
    record SituatieClient(Integer idClient, String numeComplet, String tipAbonament, AbonamentClient abonamentCurent,
                          Long zileRamase, List<Plata> plati) {
    }

    /** UC01: RV01-RV04; inregistrarea o face receptia. */
    Client inregistreazaClient(DateClientNou date, Integer idReceptioner);

    /** UC03 + UC04: RV08, RV09, RV13, RA05, RA06, RA11, RC04, RC05, RC06, RC10. */
    AchizitieAbonament cumparaAbonament(Integer idClient, Integer idTipAbonament, MetodaPlata metodaPlata,
                                        Integer idReceptioner);

    /** UC03 flux alternativ 1a: clientul nu exista, se executa UC01 (extend) si apoi achizitia. */
    AchizitieAbonament inregistreazaSiCumparaAbonament(DateClientNou date, Integer idTipAbonament,
                                                       MetodaPlata metodaPlata, Integer idReceptioner);

    /** UC05 */
    SituatieClient consultaSituatie(Integer idClient);

    /** UC13: RV23 */
    FeedbackClient acordaFeedback(Integer idClient, Integer rating, String comentariu);
}
