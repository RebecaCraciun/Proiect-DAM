package ro.dam.salafitness.service.workflow.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.factory.AngajatFactory;
import ro.dam.salafitness.domain.factory.DateAngajatNou;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;
import ro.dam.salafitness.domain.model.entity.SalariuIstoric;
import ro.dam.salafitness.domain.model.enums.RolAngajat;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.repository.AngajatRepository;
import ro.dam.salafitness.domain.repository.TipAngajatRepository;
import ro.dam.salafitness.service.audit.AuditService;
import ro.dam.salafitness.service.validation.DomainValidationService;
import ro.dam.salafitness.service.workflow.AngajatWorkflowService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class AngajatWorkflowServiceImpl implements AngajatWorkflowService {

    private final AngajatRepository angajatRepository;
    private final TipAngajatRepository tipAngajatRepository;
    private final AngajatFactory angajatFactory;
    private final DomainValidationService validationService;
    private final AuditService auditService;
    private final Clock clock;

    public AngajatWorkflowServiceImpl(AngajatRepository angajatRepository, TipAngajatRepository tipAngajatRepository,
                                      AngajatFactory angajatFactory, DomainValidationService validationService,
                                      AuditService auditService, Clock clock) {
        this.angajatRepository = angajatRepository;
        this.tipAngajatRepository = tipAngajatRepository;
        this.angajatFactory = angajatFactory;
        this.validationService = validationService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Override
    public Angajat angajeaza(DateAngajatNou date, Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "UC19");
        validationService.verificaEmailUnic(date.email());
        if (date.idTipAngajat() != null && !tipAngajatRepository.existsById(date.idTipAngajat())) {
            throw new EntitateNegasitaException("TipAngajat", date.idTipAngajat());
        }
        Angajat angajat = angajatRepository.save(angajatFactory.creeazaAngajat(date));
        auditService.inregistreaza("ANGAJARE", "Angajat", angajat.getIdUtilizator(), idManager, angajat.getFunctie());
        return angajat;
    }

    @Override
    public Angajat modificaSalariu(Integer idAngajat, BigDecimal salariuNou, Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "UC19");
        Angajat angajat = angajat(idAngajat);
        Bani salariuVechi = angajat.getSalariu();
        angajat.modificaSalariu(salariuNou == null ? null : Bani.of(salariuNou));
        validationService.valideaza(angajat);
        auditService.inregistreaza("MODIFICARE_SALARIU", "Angajat", idAngajat, idManager,
                salariuVechi + " -> " + angajat.getSalariu());
        return angajat;
    }

    @Override
    public PontajAngajat checkIn(Integer idAngajat) {
        Angajat angajat = angajat(idAngajat);
        PontajAngajat pontaj = angajat.checkIn(LocalDateTime.now(clock));
        // radacina este deja gestionata: flush (nu merge) persista componenta noua prin cascada si ii da id-ul
        angajatRepository.flush();
        return pontaj;
    }

    @Override
    public PontajAngajat checkOut(Integer idAngajat) {
        Angajat angajat = angajat(idAngajat);
        return validationService.valideaza(angajat.checkOut(LocalDateTime.now(clock)));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal oreLucrate(Integer idAngajat, YearMonth luna) {
        return angajat(idAngajat).oreLucrate(luna);
    }

    @Override
    public List<SalariuIstoric> genereazaSalarii(YearMonth luna, Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "UC20");
        List<SalariuIstoric> generate = angajatRepository.findAll().stream()
                .filter(a -> !a.areSalariuGenerat(luna))
                .filter(a -> !a.getDataAngajarii().isAfter(luna.atEndOfMonth()))
                .map(a -> a.genereazaSalariu(luna))
                .toList();
        angajatRepository.flush();
        auditService.inregistreaza("GENERARE_SALARII", "SalariuIstoric", luna, idManager,
                generate.size() + " plati generate");
        return generate;
    }

    @Override
    public SalariuIstoric confirmaPlataSalariu(Integer idAngajat, Long idPlata, Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "UC20");
        SalariuIstoric plata = angajat(idAngajat).confirmaPlataSalariu(idPlata, LocalDate.now(clock));
        auditService.inregistreaza("PLATA_SALARIU", "SalariuIstoric", idPlata, idManager,
                plata.getSumaPlatita() + " pentru " + YearMonth.from(plata.getLunaAn()));
        return plata;
    }

    private Angajat angajat(Integer idAngajat) {
        return angajatRepository.findById(idAngajat)
                .orElseThrow(() -> new EntitateNegasitaException("Angajat", idAngajat));
    }
}
