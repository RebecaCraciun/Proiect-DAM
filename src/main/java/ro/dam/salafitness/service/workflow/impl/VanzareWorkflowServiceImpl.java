package ro.dam.salafitness.service.workflow.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.factory.VanzareFactory;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.Vanzare;
import ro.dam.salafitness.domain.repository.ClientRepository;
import ro.dam.salafitness.domain.repository.ProdusRepository;
import ro.dam.salafitness.domain.repository.VanzareRepository;
import ro.dam.salafitness.service.audit.AuditService;
import ro.dam.salafitness.service.validation.DomainValidationService;
import ro.dam.salafitness.service.workflow.VanzareWorkflowService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class VanzareWorkflowServiceImpl implements VanzareWorkflowService {

    private final VanzareRepository vanzareRepository;
    private final ProdusRepository produsRepository;
    private final ClientRepository clientRepository;
    private final VanzareFactory vanzareFactory;
    private final DomainValidationService validationService;
    private final AuditService auditService;

    public VanzareWorkflowServiceImpl(VanzareRepository vanzareRepository, ProdusRepository produsRepository,
                                      ClientRepository clientRepository, VanzareFactory vanzareFactory,
                                      DomainValidationService validationService, AuditService auditService) {
        this.vanzareRepository = vanzareRepository;
        this.produsRepository = produsRepository;
        this.clientRepository = clientRepository;
        this.vanzareFactory = vanzareFactory;
        this.validationService = validationService;
        this.auditService = auditService;
    }

    @Override
    public Vanzare vindeProduse(Integer idClient, Integer idReceptioner, List<LinieComanda> linii) {
        validationService.verificaReceptioner(idReceptioner);
        if (!clientRepository.existsById(idClient)) {
            throw new EntitateNegasitaException("Client", idClient);
        }

        Vanzare vanzare = vanzareFactory.deschideVanzare(idClient, idReceptioner);
        // produsele raman blocate pana la commit, ca stocul verificat sa fie si cel scazut (RC08)
        Map<Integer, Produs> produse = new LinkedHashMap<>();
        for (LinieComanda comanda : linii == null ? List.<LinieComanda>of() : linii) {
            Produs produs = produse.computeIfAbsent(comanda.idProdus(), id -> produsRepository.findByIdPentruVanzare(id)
                    .orElseThrow(() -> new EntitateNegasitaException("Produs", id)));
            vanzare.adaugaLinie(produs, comanda.cantitate());
        }
        vanzare.finalizeaza();
        produse.values().forEach(p -> p.scadeStoc(vanzare.cantitateProdus(p.getIdProdus())));

        Vanzare salvata = vanzareRepository.save(validationService.valideaza(vanzare));
        auditService.inregistreaza("VANZARE", "Vanzare", salvata.getIdVanzare(), idReceptioner,
                salvata.getLinii().size() + " linii, total " + salvata.getTotalPlata());
        return salvata;
    }
}
