package ro.dam.salafitness.service.rules.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.enums.RolAngajat;
import ro.dam.salafitness.domain.repository.AngajatRepository;
import ro.dam.salafitness.domain.repository.ClientRepository;
import ro.dam.salafitness.domain.repository.TipAngajatRepository;
import ro.dam.salafitness.service.audit.AuditService;
import ro.dam.salafitness.service.rules.ProtectiaDatelorService;

import java.util.EnumSet;
import java.util.Set;

@Service
@Transactional
public class ProtectiaDatelorServiceImpl implements ProtectiaDatelorService {

    private static final Set<RolAngajat> ROLURI_AUTORIZATE = EnumSet.of(RolAngajat.RECEPTIONER, RolAngajat.MANAGER);
    private static final String MASCAT = "****";

    private final ClientRepository clientRepository;
    private final AngajatRepository angajatRepository;
    private final TipAngajatRepository tipAngajatRepository;
    private final AuditService auditService;

    public ProtectiaDatelorServiceImpl(ClientRepository clientRepository, AngajatRepository angajatRepository,
                                       TipAngajatRepository tipAngajatRepository, AuditService auditService) {
        this.clientRepository = clientRepository;
        this.angajatRepository = angajatRepository;
        this.tipAngajatRepository = tipAngajatRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esteAutorizat(Integer idSolicitant, Integer idClient) {
        if (idSolicitant.equals(idClient)) {
            return true;
        }
        return angajatRepository.findById(idSolicitant)
                .flatMap(a -> tipAngajatRepository.findById(a.getIdTipAngajat()))
                .map(tip -> ROLURI_AUTORIZATE.stream().anyMatch(tip::esteRol))
                .orElse(false);
    }

    @Override
    public DateContact dateContactClient(Integer idClient, Integer idSolicitant) {
        Client client = clientRepository.findById(idClient)
                .orElseThrow(() -> new EntitateNegasitaException("Client", idClient));
        if (esteAutorizat(idSolicitant, idClient)) {
            // accesul la date personale este trasabil
            auditService.inregistreaza("ACCES_DATE_PERSONALE", "Client", idClient, idSolicitant, null);
            return new DateContact(idClient, client.getNumeComplet(),
                    client.getTelefon() == null ? null : client.getTelefon().getNumar(),
                    client.getContactUrgenta() == null ? null : client.getContactUrgenta().getDetalii(),
                    false);
        }
        return new DateContact(idClient, client.getNumeComplet(),
                client.getTelefon() == null ? null : client.getTelefon().mascat(),
                client.getContactUrgenta() == null ? null : MASCAT,
                true);
    }
}
