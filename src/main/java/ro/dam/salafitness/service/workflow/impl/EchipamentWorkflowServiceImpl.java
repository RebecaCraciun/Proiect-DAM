package ro.dam.salafitness.service.workflow.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.factory.EchipamentFactory;
import ro.dam.salafitness.domain.model.entity.Echipament;
import ro.dam.salafitness.domain.model.entity.MentenantaEchipament;
import ro.dam.salafitness.domain.model.enums.RolAngajat;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.repository.EchipamentRepository;
import ro.dam.salafitness.service.audit.AuditService;
import ro.dam.salafitness.service.validation.DomainValidationService;
import ro.dam.salafitness.service.workflow.EchipamentWorkflowService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

@Service
@Transactional
public class EchipamentWorkflowServiceImpl implements EchipamentWorkflowService {

    private final EchipamentRepository echipamentRepository;
    private final EchipamentFactory echipamentFactory;
    private final DomainValidationService validationService;
    private final AuditService auditService;
    private final Clock clock;

    public EchipamentWorkflowServiceImpl(EchipamentRepository echipamentRepository,
                                         EchipamentFactory echipamentFactory,
                                         DomainValidationService validationService, AuditService auditService,
                                         Clock clock) {
        this.echipamentRepository = echipamentRepository;
        this.echipamentFactory = echipamentFactory;
        this.validationService = validationService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Override
    public Echipament inregistreazaEchipament(String denumire, LocalDate dataAchizitie, String stareUzura,
                                              String locatieInSala, Integer idAngajat) {
        validationService.verificaUnulDinRoluri(idAngajat, "UC24", RolAngajat.MANAGER, RolAngajat.TEHNIC);
        Echipament echipament = echipamentRepository.save(
                echipamentFactory.creeazaEchipament(denumire, dataAchizitie, stareUzura, locatieInSala));
        auditService.inregistreaza("INREGISTRARE", "Echipament", echipament.getIdEchipament(), idAngajat, denumire);
        return echipament;
    }

    @Override
    public Echipament mutaEchipament(Integer idEchipament, String locatieNoua, Integer idAngajat) {
        validationService.verificaUnulDinRoluri(idAngajat, "UC24", RolAngajat.MANAGER, RolAngajat.TEHNIC);
        Echipament echipament = echipament(idEchipament);
        echipament.muta(locatieNoua);
        return validationService.valideaza(echipament);
    }

    @Override
    public MentenantaEchipament inregistreazaMentenanta(Integer idEchipament, Integer idTehnician,
                                                        String descriereProblema, BigDecimal cost, String stareNoua) {
        validationService.verificaTehnician(idTehnician);
        Echipament echipament = echipament(idEchipament);
        MentenantaEchipament interventie = validationService.valideaza(echipament.inregistreazaMentenanta(
                LocalDate.now(clock), descriereProblema, cost == null ? null : Bani.of(cost), idTehnician, stareNoua));
        // radacina este deja gestionata: flush (nu merge) persista componenta noua prin cascada si ii da id-ul
        echipamentRepository.flush();
        auditService.inregistreaza("MENTENANTA", "Echipament", idEchipament, idTehnician,
                interventie.getCost() + (descriereProblema == null ? "" : ": " + descriereProblema));
        return interventie;
    }

    private Echipament echipament(Integer idEchipament) {
        return echipamentRepository.findById(idEchipament)
                .orElseThrow(() -> new EntitateNegasitaException("Echipament", idEchipament));
    }
}
