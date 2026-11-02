package ro.dam.salafitness.service.workflow.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.enums.RolAngajat;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.repository.ProdusRepository;
import ro.dam.salafitness.domain.repository.TipAbonamentRepository;
import ro.dam.salafitness.service.audit.AuditService;
import ro.dam.salafitness.service.validation.DomainValidationService;
import ro.dam.salafitness.service.workflow.NomenclatoareWorkflowService;

import java.math.BigDecimal;

@Service
@Transactional
public class NomenclatoareWorkflowServiceImpl implements NomenclatoareWorkflowService {

    private final TipAbonamentRepository tipAbonamentRepository;
    private final ProdusRepository produsRepository;
    private final DomainValidationService validationService;
    private final AuditService auditService;

    public NomenclatoareWorkflowServiceImpl(TipAbonamentRepository tipAbonamentRepository,
                                            ProdusRepository produsRepository,
                                            DomainValidationService validationService, AuditService auditService) {
        this.tipAbonamentRepository = tipAbonamentRepository;
        this.produsRepository = produsRepository;
        this.validationService = validationService;
        this.auditService = auditService;
    }

    @Override
    public TipAbonament definesteTipAbonament(String denumire, Integer durataZile, BigDecimal pret,
                                              Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "UC21");
        validationService.verificaDenumireTipAbonamentUnica(denumire);
        TipAbonament tip = tipAbonamentRepository.save(
                validationService.valideaza(new TipAbonament(denumire, durataZile, bani(pret))));
        auditService.inregistreaza("DEFINIRE", "TipAbonament", tip.getIdTipAbonament(), idManager,
                denumire + ", " + durataZile + " zile, " + tip.getPret());
        return tip;
    }

    @Override
    public TipAbonament modificaTipAbonament(Integer idTipAbonament, Integer durataZile, BigDecimal pret,
                                             Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "UC21");
        TipAbonament tip = tipAbonamentRepository.findById(idTipAbonament)
                .orElseThrow(() -> new EntitateNegasitaException("TipAbonament", idTipAbonament));
        if (durataZile != null) {
            tip.modificaDurata(durataZile);
        }
        if (pret != null) {
            tip.modificaPret(Bani.of(pret));
        }
        validationService.valideaza(tip);
        auditService.inregistreaza("MODIFICARE", "TipAbonament", idTipAbonament, idManager,
                tip.getDurataZile() + " zile, " + tip.getPret());
        return tip;
    }

    @Override
    public Produs adaugaProdus(String denumire, String categorie, BigDecimal pret, Integer stocInitial,
                               Integer idAngajat) {
        validationService.verificaUnulDinRoluri(idAngajat, "UC23", RolAngajat.MANAGER, RolAngajat.RECEPTIONER);
        Produs produs = produsRepository.save(validationService.valideaza(
                new Produs(denumire, categorie, bani(pret), stocInitial == null ? 0 : stocInitial)));
        auditService.inregistreaza("ADAUGARE", "Produs", produs.getIdProdus(), idAngajat, denumire);
        return produs;
    }

    @Override
    public Produs receptioneazaStoc(Integer idProdus, int cantitate, Integer idAngajat) {
        validationService.verificaUnulDinRoluri(idAngajat, "UC23", RolAngajat.MANAGER, RolAngajat.RECEPTIONER);
        Produs produs = produs(idProdus);
        produs.adaugaStoc(cantitate);
        auditService.inregistreaza("RECEPTIE_STOC", "Produs", idProdus, idAngajat, "+" + cantitate);
        return produs;
    }

    @Override
    public Produs modificaPretProdus(Integer idProdus, BigDecimal pretNou, Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "UC23");
        Produs produs = produs(idProdus);
        Bani pretVechi = produs.getPret();
        produs.modificaPret(bani(pretNou));
        validationService.valideaza(produs);
        auditService.inregistreaza("MODIFICARE_PRET", "Produs", idProdus, idManager,
                pretVechi + " -> " + produs.getPret());
        return produs;
    }

    private Produs produs(Integer idProdus) {
        return produsRepository.findById(idProdus).orElseThrow(() -> new EntitateNegasitaException("Produs", idProdus));
    }

    private static Bani bani(BigDecimal valoare) {
        return valoare == null ? null : Bani.of(valoare);
    }
}
