package ro.dam.salafitness.service.rules.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.enums.RolAngajat;
import ro.dam.salafitness.domain.repository.AbonamentClientRepository;
import ro.dam.salafitness.domain.repository.AccesSalaRepository;
import ro.dam.salafitness.domain.repository.AngajatRepository;
import ro.dam.salafitness.domain.repository.ClasaFitnessRepository;
import ro.dam.salafitness.domain.repository.ClientRepository;
import ro.dam.salafitness.domain.repository.LinieVanzareRepository;
import ro.dam.salafitness.domain.repository.MentenantaEchipamentRepository;
import ro.dam.salafitness.domain.repository.PlataRepository;
import ro.dam.salafitness.domain.repository.ProdusRepository;
import ro.dam.salafitness.domain.repository.RezervareClasaRepository;
import ro.dam.salafitness.domain.repository.SedintaPTRepository;
import ro.dam.salafitness.domain.repository.TipAbonamentRepository;
import ro.dam.salafitness.domain.repository.VanzareRepository;
import ro.dam.salafitness.service.audit.AuditService;
import ro.dam.salafitness.service.rules.RetentieIstoricService;
import ro.dam.salafitness.service.validation.DomainValidationService;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RetentieIstoricServiceImpl implements RetentieIstoricService {

    private final ClientRepository clientRepository;
    private final AngajatRepository angajatRepository;
    private final ProdusRepository produsRepository;
    private final TipAbonamentRepository tipAbonamentRepository;
    private final AbonamentClientRepository abonamentClientRepository;
    private final PlataRepository plataRepository;
    private final VanzareRepository vanzareRepository;
    private final LinieVanzareRepository linieVanzareRepository;
    private final RezervareClasaRepository rezervareClasaRepository;
    private final SedintaPTRepository sedintaPTRepository;
    private final AccesSalaRepository accesSalaRepository;
    private final ClasaFitnessRepository clasaFitnessRepository;
    private final MentenantaEchipamentRepository mentenantaRepository;
    private final DomainValidationService validationService;
    private final AuditService auditService;

    public RetentieIstoricServiceImpl(ClientRepository clientRepository, AngajatRepository angajatRepository,
                                      ProdusRepository produsRepository, TipAbonamentRepository tipAbonamentRepository,
                                      AbonamentClientRepository abonamentClientRepository,
                                      PlataRepository plataRepository, VanzareRepository vanzareRepository,
                                      LinieVanzareRepository linieVanzareRepository,
                                      RezervareClasaRepository rezervareClasaRepository,
                                      SedintaPTRepository sedintaPTRepository, AccesSalaRepository accesSalaRepository,
                                      ClasaFitnessRepository clasaFitnessRepository,
                                      MentenantaEchipamentRepository mentenantaRepository,
                                      DomainValidationService validationService, AuditService auditService) {
        this.clientRepository = clientRepository;
        this.angajatRepository = angajatRepository;
        this.produsRepository = produsRepository;
        this.tipAbonamentRepository = tipAbonamentRepository;
        this.abonamentClientRepository = abonamentClientRepository;
        this.plataRepository = plataRepository;
        this.vanzareRepository = vanzareRepository;
        this.linieVanzareRepository = linieVanzareRepository;
        this.rezervareClasaRepository = rezervareClasaRepository;
        this.sedintaPTRepository = sedintaPTRepository;
        this.accesSalaRepository = accesSalaRepository;
        this.clasaFitnessRepository = clasaFitnessRepository;
        this.mentenantaRepository = mentenantaRepository;
        this.validationService = validationService;
        this.auditService = auditService;
    }

    @Override
    public void stergeClient(Integer idClient, Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "RB01");
        Client client = clientRepository.findById(idClient)
                .orElseThrow(() -> new EntitateNegasitaException("Client", idClient));

        List<String> referinte = new ArrayList<>();
        if (!client.getAbonamente().isEmpty()) referinte.add("abonamente");
        if (plataRepository.existsByIdClient(idClient)) referinte.add("plati");
        if (vanzareRepository.existsByIdClient(idClient)) referinte.add("vanzari");
        if (rezervareClasaRepository.existsByIdClient(idClient)) referinte.add("rezervari");
        if (sedintaPTRepository.existsByIdClient(idClient)) referinte.add("sedinte PT");
        if (accesSalaRepository.existsByIdUtilizator(idClient)) referinte.add("accese in sala");
        refuzaDacaExistaIstoric("Clientul " + client.getNumeComplet(), referinte);

        clientRepository.delete(client); // RB02: feedback-urile se sterg in cascada
        auditService.inregistreaza("STERGERE", "Client", idClient, idManager, null);
    }

    @Override
    public void stergeAngajat(Integer idAngajat, Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "RB01");
        Angajat angajat = angajatRepository.findById(idAngajat)
                .orElseThrow(() -> new EntitateNegasitaException("Angajat", idAngajat));

        List<String> referinte = new ArrayList<>();
        if (!angajat.getPontaje().isEmpty()) referinte.add("pontaje");
        if (!angajat.getSalarii().isEmpty()) referinte.add("salarii");
        if (plataRepository.existsByIdAngajat(idAngajat)) referinte.add("plati incasate");
        if (vanzareRepository.existsByIdAngajat(idAngajat)) referinte.add("vanzari");
        if (clasaFitnessRepository.existsByIdAntrenor(idAngajat)) referinte.add("clase");
        if (sedintaPTRepository.existsByIdAntrenor(idAngajat)) referinte.add("sedinte PT");
        if (mentenantaRepository.existsByIdAngajatResponsabil(idAngajat)) referinte.add("interventii de mentenanta");
        if (accesSalaRepository.existsByIdUtilizator(idAngajat)) referinte.add("accese in sala");
        refuzaDacaExistaIstoric("Angajatul " + angajat.getNumeComplet(), referinte);

        angajatRepository.delete(angajat);
        auditService.inregistreaza("STERGERE", "Angajat", idAngajat, idManager, null);
    }

    @Override
    public void stergeProdus(Integer idProdus, Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "RB01");
        if (!produsRepository.existsById(idProdus)) {
            throw new EntitateNegasitaException("Produs", idProdus);
        }
        if (linieVanzareRepository.existsByIdProdus(idProdus)) {
            refuzaDacaExistaIstoric("Produsul " + idProdus, List.of("linii de vanzare"));
        }
        produsRepository.deleteById(idProdus);
        auditService.inregistreaza("STERGERE", "Produs", idProdus, idManager, null);
    }

    @Override
    public void stergeTipAbonament(Integer idTipAbonament, Integer idManager) {
        validationService.verificaRol(idManager, RolAngajat.MANAGER, "RB01");
        if (!tipAbonamentRepository.existsById(idTipAbonament)) {
            throw new EntitateNegasitaException("TipAbonament", idTipAbonament);
        }
        if (abonamentClientRepository.existsByIdTipAbonament(idTipAbonament)) {
            refuzaDacaExistaIstoric("Tipul de abonament " + idTipAbonament, List.of("abonamente cumparate"));
        }
        tipAbonamentRepository.deleteById(idTipAbonament);
        auditService.inregistreaza("STERGERE", "TipAbonament", idTipAbonament, idManager, null);
    }

    private static void refuzaDacaExistaIstoric(String entitate, List<String> referinte) {
        if (!referinte.isEmpty()) {
            throw new BusinessRuleException("RB01", entitate + " nu poate fi sters deoarece este referit de: "
                    + String.join(", ", referinte));
        }
    }
}
