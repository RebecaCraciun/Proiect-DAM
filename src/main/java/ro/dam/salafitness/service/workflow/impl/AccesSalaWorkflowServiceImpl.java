package ro.dam.salafitness.service.workflow.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.model.entity.AccesSala;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.Utilizator;
import ro.dam.salafitness.domain.repository.AccesSalaRepository;
import ro.dam.salafitness.domain.repository.UtilizatorRepository;
import ro.dam.salafitness.service.workflow.AccesSalaWorkflowService;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional
public class AccesSalaWorkflowServiceImpl implements AccesSalaWorkflowService {

    private final UtilizatorRepository utilizatorRepository;
    private final AccesSalaRepository accesSalaRepository;
    private final Clock clock;

    public AccesSalaWorkflowServiceImpl(UtilizatorRepository utilizatorRepository,
                                        AccesSalaRepository accesSalaRepository, Clock clock) {
        this.utilizatorRepository = utilizatorRepository;
        this.accesSalaRepository = accesSalaRepository;
        this.clock = clock;
    }

    @Override
    public AccesSala inregistreazaIntrare(Integer idUtilizator) {
        Utilizator utilizator = utilizatorRepository.findById(idUtilizator)
                .orElseThrow(() -> new EntitateNegasitaException("Utilizator", idUtilizator));
        LocalDateTime acum = LocalDateTime.now(clock);

        // RA07 / UC07: angajatii (2a) nu au nevoie de abonament; clientii au nevoie de unul valid azi (2b)
        boolean permis = utilizator.esteAngajat()
                || (utilizator instanceof Client client && client.poateIntra(acum.toLocalDate()));
        AccesSala acces = permis ? AccesSala.permis(idUtilizator, acum) : AccesSala.respins(idUtilizator, acum);
        return accesSalaRepository.save(acces);
    }

    @Override
    public AccesSala inregistreazaIesire(Long idAcces) {
        AccesSala acces = accesSalaRepository.findById(idAcces)
                .orElseThrow(() -> new EntitateNegasitaException("AccesSala", idAcces));
        acces.inregistreazaIesire(LocalDateTime.now(clock));
        return acces;
    }
}
