package ro.dam.salafitness.service.workflow;

import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.TipAbonament;

import java.math.BigDecimal;

/**
 * Administrarea ofertei salii: UC21 (tipuri de abonament) si UC23 (produse si stoc).
 */
public interface NomenclatoareWorkflowService {

    /** UC21: RV07, RV08; doar managerul. */
    TipAbonament definesteTipAbonament(String denumire, Integer durataZile, BigDecimal pret, Integer idManager);

    /** UC21 / RB06: abonamentele si platile existente pastreaza pretul si perioada din momentul achizitiei. */
    TipAbonament modificaTipAbonament(Integer idTipAbonament, Integer durataZile, BigDecimal pret, Integer idManager);

    /** UC23: RV19; Manager sau Receptioner. */
    Produs adaugaProdus(String denumire, String categorie, BigDecimal pret, Integer stocInitial, Integer idAngajat);

    /** UC23: receptia unei cantitati noi; Manager sau Receptioner. */
    Produs receptioneazaStoc(Integer idProdus, int cantitate, Integer idAngajat);

    /** UC23 / RB06: liniile de vanzare existente pastreaza pretul vechi (RC07); doar managerul. */
    Produs modificaPretProdus(Integer idProdus, BigDecimal pretNou, Integer idManager);
}
