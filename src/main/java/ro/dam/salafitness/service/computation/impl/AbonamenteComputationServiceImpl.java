package ro.dam.salafitness.service.computation.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.model.entity.AbonamentClient;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.enums.StatusAbonament;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.model.vo.PerioadaValabilitate;
import ro.dam.salafitness.domain.repository.ClientRepository;
import ro.dam.salafitness.domain.repository.TipAbonamentRepository;
import ro.dam.salafitness.service.computation.AbonamenteComputationService;
import ro.dam.salafitness.service.computation.view.AbonamentActivView;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AbonamenteComputationServiceImpl implements AbonamenteComputationService {

    private final ClientRepository clientRepository;
    private final TipAbonamentRepository tipAbonamentRepository;
    private final Clock clock;

    public AbonamenteComputationServiceImpl(ClientRepository clientRepository,
                                            TipAbonamentRepository tipAbonamentRepository, Clock clock) {
        this.clientRepository = clientRepository;
        this.tipAbonamentRepository = tipAbonamentRepository;
        this.clock = clock;
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
    @Transactional(readOnly = true)
    public List<AbonamentActivView> abonamenteActive(LocalDate data) {
        Map<Integer, String> tipuri = tipAbonamentRepository.findAll().stream()
                .collect(Collectors.toMap(TipAbonament::getIdTipAbonament, TipAbonament::getDenumire));
        return clientRepository.findAbonamenteValabileLa(data).stream()
                .map(a -> new AbonamentActivView(
                        a.getIdAbonamentClient(),
                        a.getClient().getIdUtilizator(),
                        a.getClient().getNumeComplet(),
                        tipuri.get(a.getIdTipAbonament()),
                        a.getPerioada().getDataInceput(),
                        a.getPerioada().getDataSfarsit(),
                        a.getPerioada().zileRamase(data)))
                .sorted(Comparator.comparing(AbonamentActivView::zileRamase))
                .toList();
    }
}
