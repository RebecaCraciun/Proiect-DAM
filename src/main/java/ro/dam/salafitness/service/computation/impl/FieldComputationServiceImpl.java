package ro.dam.salafitness.service.computation.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.model.entity.AbonamentClient;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.LinieVanzare;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.entity.Vanzare;
import ro.dam.salafitness.domain.model.enums.StatusAbonament;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.model.vo.PerioadaValabilitate;
import ro.dam.salafitness.domain.repository.ClientRepository;
import ro.dam.salafitness.domain.repository.TipAbonamentRepository;
import ro.dam.salafitness.service.computation.FieldComputationService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Formulele sunt incapsulate in entitati si value objects (model bogat); serviciul le aplica pe entitati
 * si ofera punctul unic de acces la regulile de calcul pentru serviciile de workflow.
 */
@Service
public class FieldComputationServiceImpl implements FieldComputationService {

    private final ClientRepository clientRepository;
    private final TipAbonamentRepository tipAbonamentRepository;
    private final Clock clock;

    public FieldComputationServiceImpl(ClientRepository clientRepository,
                                       TipAbonamentRepository tipAbonamentRepository, Clock clock) {
        this.clientRepository = clientRepository;
        this.tipAbonamentRepository = tipAbonamentRepository;
        this.clock = clock;
    }

    @Override
    public Bani calculeazaTotalLinie(LinieVanzare linie) {
        return linie.recalculeazaTotal();
    }

    @Override
    public Bani calculeazaTotalVanzare(Vanzare vanzare) {
        return vanzare.calculeazaTotal();
    }

    @Override
    public BigDecimal calculeazaOreLucrate(PontajAngajat pontaj) {
        return pontaj.recalculeazaOreLucrate();
    }

    @Override
    public LocalDate calculeazaDataInceputAbonament(Client client, LocalDate azi) {
        return client.dataInceputUrmatorAbonament(azi);
    }

    @Override
    public PerioadaValabilitate calculeazaPerioada(TipAbonament tip, LocalDate dataInceput) {
        return PerioadaValabilitate.dinDurata(dataInceput, tip.getDurataZile());
    }

    @Override
    public StatusAbonament calculeazaStatusAbonament(AbonamentClient abonament, LocalDate azi) {
        abonament.actualizeazaStatus(azi);
        return abonament.getStatus();
    }

    @Override
    @Transactional
    public int actualizeazaStatusAbonamente() {
        LocalDate azi = LocalDate.now(clock);
        return clientRepository.findAll().stream()
                .mapToInt(client -> client.actualizeazaStatusAbonamente(azi))
                .sum();
    }

    @Override
    public Bani calculeazaSumaPlataAbonament(TipAbonament tip) {
        return tip.getPret();
    }

    @Override
    public Bani calculeazaPretUnitar(Produs produs) {
        return produs.getPret();
    }

    @Override
    public int calculeazaStocRamas(Produs produs, int cantitateVanduta) {
        int ramas = produs.getStocCurent() - cantitateVanduta;
        if (ramas < 0) {
            throw new BusinessRuleException("RA14", "Stoc insuficient pentru " + produs.getDenumire());
        }
        return ramas;
    }

    @Override
    public int calculeazaLocuriLibere(ProgramClasa sesiune) {
        return sesiune.locuriDisponibile();
    }

    @Override
    @Transactional(readOnly = true)
    public String calculeazaTipAbonamentClient(Client client) {
        String denumire = client.ultimulAbonament()
                .flatMap(a -> tipAbonamentRepository.findById(a.getIdTipAbonament()))
                .map(TipAbonament::getDenumire)
                .orElse(null);
        client.actualizeazaTipAbonament(denumire);
        return denumire;
    }

    @Override
    public Bani calculeazaSumaSalariu(Angajat angajat) {
        return angajat.getSalariu();
    }
}
