package ro.dam.salafitness.service.workflow.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.factory.ClientFactory;
import ro.dam.salafitness.domain.factory.DateClientNou;
import ro.dam.salafitness.domain.model.entity.AbonamentClient;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.FeedbackClient;
import ro.dam.salafitness.domain.model.entity.Plata;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.enums.MetodaPlata;
import ro.dam.salafitness.domain.model.enums.RolAngajat;
import ro.dam.salafitness.domain.repository.ClientRepository;
import ro.dam.salafitness.domain.repository.PlataRepository;
import ro.dam.salafitness.domain.repository.TipAbonamentRepository;
import ro.dam.salafitness.service.audit.AuditService;
import ro.dam.salafitness.service.computation.FieldComputationService;
import ro.dam.salafitness.service.validation.DomainValidationService;
import ro.dam.salafitness.service.workflow.ClientWorkflowService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class ClientWorkflowServiceImpl implements ClientWorkflowService {

    private final ClientRepository clientRepository;
    private final TipAbonamentRepository tipAbonamentRepository;
    private final PlataRepository plataRepository;
    private final ClientFactory clientFactory;
    private final DomainValidationService validationService;
    private final FieldComputationService computationService;
    private final AuditService auditService;
    private final Clock clock;

    public ClientWorkflowServiceImpl(ClientRepository clientRepository, TipAbonamentRepository tipAbonamentRepository,
                                     PlataRepository plataRepository, ClientFactory clientFactory,
                                     DomainValidationService validationService,
                                     FieldComputationService computationService, AuditService auditService,
                                     Clock clock) {
        this.clientRepository = clientRepository;
        this.tipAbonamentRepository = tipAbonamentRepository;
        this.plataRepository = plataRepository;
        this.clientFactory = clientFactory;
        this.validationService = validationService;
        this.computationService = computationService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Override
    public Client inregistreazaClient(DateClientNou date, Integer idReceptioner) {
        validationService.verificaRol(idReceptioner, RolAngajat.RECEPTIONER, "UC01");
        validationService.verificaEmailUnic(date.email());
        Client client = clientRepository.save(clientFactory.creeazaClient(date));
        auditService.inregistreaza("INREGISTRARE", "Client", client.getIdUtilizator(), idReceptioner,
                client.getEmail().getAdresa());
        return client;
    }

    @Override
    public AchizitieAbonament cumparaAbonament(Integer idClient, Integer idTipAbonament, MetodaPlata metodaPlata,
                                               Integer idReceptioner) {
        validationService.verificaReceptioner(idReceptioner);
        Client client = client(idClient);
        TipAbonament tip = tipAbonamentRepository.findById(idTipAbonament)
                .orElseThrow(() -> new EntitateNegasitaException("TipAbonament", idTipAbonament));
        LocalDateTime acum = LocalDateTime.now(clock);

        // UC03 5a: plata invalida opreste achizitia inainte de a crea abonamentul (RV13, RC06)
        Plata plata = validationService.valideaza(new Plata(idClient, idReceptioner,
                computationService.calculeazaSumaPlataAbonament(tip), metodaPlata, acum));
        // RA05, RA06, RC04, RC05, RC10 - invariantii agregatului Client
        AbonamentClient abonament = client.cumparaAbonament(tip, acum.toLocalDate());

        // radacina este deja gestionata: flush (nu merge) persista componenta noua prin cascada si ii da id-ul
        clientRepository.flush();
        plataRepository.save(plata);
        auditService.inregistreaza("ACHIZITIE_ABONAMENT", "AbonamentClient", abonament.getIdAbonamentClient(),
                idReceptioner, tip.getDenumire() + " " + abonament.getPerioada() + ", " + plata.getSuma() + " "
                        + metodaPlata.getValoare());
        return new AchizitieAbonament(abonament, plata);
    }

    @Override
    public AchizitieAbonament inregistreazaSiCumparaAbonament(DateClientNou date, Integer idTipAbonament,
                                                              MetodaPlata metodaPlata, Integer idReceptioner) {
        Client client = inregistreazaClient(date, idReceptioner);
        return cumparaAbonament(client.getIdUtilizator(), idTipAbonament, metodaPlata, idReceptioner);
    }

    @Override
    @Transactional(readOnly = true)
    public SituatieClient consultaSituatie(Integer idClient) {
        Client client = client(idClient);
        LocalDate azi = LocalDate.now(clock);
        AbonamentClient curent = client.abonamentActiv(azi).orElse(null);
        return new SituatieClient(idClient, client.getNumeComplet(), client.getTipAbonament(), curent,
                curent == null ? null : curent.getPerioada().zileRamase(azi),
                plataRepository.findByIdClientOrderByDataPlataDesc(idClient));
    }

    @Override
    public FeedbackClient acordaFeedback(Integer idClient, Integer rating, String comentariu) {
        Client client = client(idClient);
        FeedbackClient feedback = validationService.valideaza(
                client.adaugaFeedback(rating, comentariu, LocalDateTime.now(clock)));
        // radacina este deja gestionata: flush (nu merge) persista componenta noua prin cascada si ii da id-ul
        clientRepository.flush();
        return feedback;
    }

    private Client client(Integer idClient) {
        return clientRepository.findById(idClient).orElseThrow(() -> new EntitateNegasitaException("Client", idClient));
    }
}
