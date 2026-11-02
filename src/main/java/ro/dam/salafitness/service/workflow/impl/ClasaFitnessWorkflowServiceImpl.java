package ro.dam.salafitness.service.workflow.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.factory.ClasaFitnessFactory;
import ro.dam.salafitness.domain.model.entity.ClasaFitness;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.domain.model.entity.RezervareClasa;
import ro.dam.salafitness.domain.model.enums.RolAngajat;
import ro.dam.salafitness.domain.model.vo.IntervalOrar;
import ro.dam.salafitness.domain.repository.ClasaFitnessRepository;
import ro.dam.salafitness.domain.repository.ProgramClasaRepository;
import ro.dam.salafitness.service.audit.AuditService;
import ro.dam.salafitness.service.computation.ViewComputationService;
import ro.dam.salafitness.service.computation.view.OcupareClasaView;
import ro.dam.salafitness.service.validation.DomainValidationService;
import ro.dam.salafitness.service.workflow.ClasaFitnessWorkflowService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ClasaFitnessWorkflowServiceImpl implements ClasaFitnessWorkflowService {

    private final ClasaFitnessRepository clasaFitnessRepository;
    private final ProgramClasaRepository programClasaRepository;
    private final ClasaFitnessFactory clasaFitnessFactory;
    private final DomainValidationService validationService;
    private final ViewComputationService viewComputationService;
    private final AuditService auditService;
    private final Clock clock;

    public ClasaFitnessWorkflowServiceImpl(ClasaFitnessRepository clasaFitnessRepository,
                                           ProgramClasaRepository programClasaRepository,
                                           ClasaFitnessFactory clasaFitnessFactory,
                                           DomainValidationService validationService,
                                           ViewComputationService viewComputationService, AuditService auditService,
                                           Clock clock) {
        this.clasaFitnessRepository = clasaFitnessRepository;
        this.programClasaRepository = programClasaRepository;
        this.clasaFitnessFactory = clasaFitnessFactory;
        this.validationService = validationService;
        this.viewComputationService = viewComputationService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Override
    public ClasaFitness definesteClasa(String denumire, Integer idAntrenor, int capacitateMaxima, String descriere,
                                       Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "UC22");
        validationService.verificaAntrenor(idAntrenor);
        ClasaFitness clasa = clasaFitnessRepository.save(
                clasaFitnessFactory.creeazaClasa(denumire, idAntrenor, capacitateMaxima, descriere));
        auditService.inregistreaza("DEFINIRE_CLASA", "ClasaFitness", clasa.getIdClasa(), idManager, denumire);
        return clasa;
    }

    @Override
    public ProgramClasa programeazaSesiune(Integer idClasa, IntervalOrar interval, String salaStudio,
                                           Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "UC22");
        validationService.valideaza(interval);
        ClasaFitness clasa = clasaFitnessRepository.findById(idClasa)
                .orElseThrow(() -> new EntitateNegasitaException("ClasaFitness", idClasa));
        validationService.verificaSalaLibera(salaStudio, interval, null);

        ProgramClasa sesiune = validationService.valideaza(clasa.programeazaSesiune(interval, salaStudio));
        // radacina este deja gestionata: flush (nu merge) persista componenta noua prin cascada si ii da id-ul
        clasaFitnessRepository.flush();
        return sesiune;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcupareClasaView> programClase(LocalDateTime de, LocalDateTime pana) {
        return viewComputationService.ocupareClase(de, pana);
    }

    @Override
    public RezervareClasa rezervaLoc(Long idProgram, Integer idClient) {
        // RA01: sesiunea ramane blocata pana la commit, ca doua rezervari simultane sa nu depaseasca capacitatea
        ProgramClasa sesiune = sesiunePentruModificare(idProgram);
        validationService.verificaAbonamentValid(idClient, sesiune.getInterval().getInceput().toLocalDate(), "RA03");

        RezervareClasa rezervare = sesiune.getClasa().rezerva(idProgram, idClient, LocalDateTime.now(clock));
        // radacina este deja gestionata: flush (nu merge) persista componenta noua prin cascada si ii da id-ul
        programClasaRepository.flush();
        return rezervare;
    }

    @Override
    public RezervareClasa anuleazaRezervare(Long idProgram, Integer idClient) {
        ProgramClasa sesiune = sesiunePentruModificare(idProgram);
        return sesiune.getClasa().anuleazaRezervare(idProgram, idClient, LocalDateTime.now(clock));
    }

    @Override
    public RezervareClasa marcheazaPrezenta(Long idProgram, Integer idClient, Integer idAntrenor) {
        ProgramClasa sesiune = sesiunePentruModificare(idProgram);
        return sesiune.getClasa().marcheazaPrezenta(idProgram, idClient, idAntrenor, LocalDateTime.now(clock));
    }

    private ProgramClasa sesiunePentruModificare(Long idProgram) {
        return programClasaRepository.findByIdPentruRezervare(idProgram)
                .orElseThrow(() -> new EntitateNegasitaException("ProgramClasa", idProgram));
    }
}
