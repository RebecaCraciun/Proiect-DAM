package ro.dam.salafitness.service.workflow.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.model.entity.SedintaPT;
import ro.dam.salafitness.domain.model.enums.StatusSedinta;
import ro.dam.salafitness.domain.repository.ClientRepository;
import ro.dam.salafitness.domain.repository.SedintaPTRepository;
import ro.dam.salafitness.service.validation.DomainValidationService;
import ro.dam.salafitness.service.workflow.SedintaPTWorkflowService;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional
public class SedintaPTWorkflowServiceImpl implements SedintaPTWorkflowService {

    private final SedintaPTRepository sedintaPTRepository;
    private final ClientRepository clientRepository;
    private final DomainValidationService validationService;
    private final Clock clock;

    public SedintaPTWorkflowServiceImpl(SedintaPTRepository sedintaPTRepository, ClientRepository clientRepository,
                                        DomainValidationService validationService, Clock clock) {
        this.sedintaPTRepository = sedintaPTRepository;
        this.clientRepository = clientRepository;
        this.validationService = validationService;
        this.clock = clock;
    }

    @Override
    public SedintaPT programeazaSedinta(Integer idClient, Integer idAntrenor, LocalDateTime dataOra) {
        if (!clientRepository.existsById(idClient)) {
            throw new EntitateNegasitaException("Client", idClient);
        }
        validationService.verificaAntrenor(idAntrenor);
        if (dataOra == null || !dataOra.isAfter(LocalDateTime.now(clock))) {
            throw new BusinessRuleException("UC12", "Sedinta PT se poate programa doar in viitor");
        }
        validationService.verificaDisponibilitateAntrenor(idAntrenor, dataOra);
        return sedintaPTRepository.save(validationService.valideaza(new SedintaPT(idAntrenor, idClient, dataOra)));
    }

    @Override
    public SedintaPT finalizeazaSedinta(Long idSedinta, Integer idAntrenor, StatusSedinta statusFinal, String note) {
        SedintaPT sedinta = sedinta(idSedinta);
        if (!sedinta.getIdAntrenor().equals(idAntrenor)) {
            throw new BusinessRuleException("UC16", "Doar antrenorul sedintei o poate finaliza");
        }
        sedinta.finalizeaza(statusFinal, note, LocalDateTime.now(clock));
        return validationService.valideaza(sedinta);
    }

    @Override
    public SedintaPT anuleazaSedinta(Long idSedinta) {
        SedintaPT sedinta = sedinta(idSedinta);
        sedinta.anuleaza();
        return sedinta;
    }

    private SedintaPT sedinta(Long idSedinta) {
        return sedintaPTRepository.findById(idSedinta)
                .orElseThrow(() -> new EntitateNegasitaException("SedintaPT", idSedinta));
    }
}
