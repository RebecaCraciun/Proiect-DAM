package ro.dam.salafitness.service.computation.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.ClasaFitness;
import ro.dam.salafitness.domain.model.entity.FeedbackClient;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.entity.TipAngajat;
import ro.dam.salafitness.domain.repository.AngajatRepository;
import ro.dam.salafitness.domain.repository.ClasaFitnessRepository;
import ro.dam.salafitness.domain.repository.ClientRepository;
import ro.dam.salafitness.domain.repository.EchipamentRepository;
import ro.dam.salafitness.domain.repository.PlataRepository;
import ro.dam.salafitness.domain.repository.TipAbonamentRepository;
import ro.dam.salafitness.domain.repository.TipAngajatRepository;
import ro.dam.salafitness.domain.repository.VanzareRepository;
import ro.dam.salafitness.service.computation.ViewComputationService;
import ro.dam.salafitness.service.computation.view.AbonamentActivView;
import ro.dam.salafitness.service.computation.view.CostMentenantaView;
import ro.dam.salafitness.service.computation.view.FeedbackLunarView;
import ro.dam.salafitness.service.computation.view.OcupareClasaView;
import ro.dam.salafitness.service.computation.view.OreLucrateLunarView;
import ro.dam.salafitness.service.computation.view.VenitLunarView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ViewComputationServiceImpl implements ViewComputationService {

    private static final BigDecimal O_SUTA = BigDecimal.valueOf(100);

    private final ClientRepository clientRepository;
    private final TipAbonamentRepository tipAbonamentRepository;
    private final ClasaFitnessRepository clasaFitnessRepository;
    private final PlataRepository plataRepository;
    private final VanzareRepository vanzareRepository;
    private final AngajatRepository angajatRepository;
    private final TipAngajatRepository tipAngajatRepository;
    private final EchipamentRepository echipamentRepository;

    public ViewComputationServiceImpl(ClientRepository clientRepository,
                                      TipAbonamentRepository tipAbonamentRepository,
                                      ClasaFitnessRepository clasaFitnessRepository,
                                      PlataRepository plataRepository,
                                      VanzareRepository vanzareRepository,
                                      AngajatRepository angajatRepository,
                                      TipAngajatRepository tipAngajatRepository,
                                      EchipamentRepository echipamentRepository) {
        this.clientRepository = clientRepository;
        this.tipAbonamentRepository = tipAbonamentRepository;
        this.clasaFitnessRepository = clasaFitnessRepository;
        this.plataRepository = plataRepository;
        this.vanzareRepository = vanzareRepository;
        this.angajatRepository = angajatRepository;
        this.tipAngajatRepository = tipAngajatRepository;
        this.echipamentRepository = echipamentRepository;
    }

    @Override
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

    @Override
    public List<OcupareClasaView> ocupareClase() {
        return clasaFitnessRepository.findToateSesiunile().stream().map(this::ocupare).toList();
    }

    @Override
    public List<OcupareClasaView> ocupareClase(LocalDateTime de, LocalDateTime pana) {
        return clasaFitnessRepository.findSesiuniInPerioada(de, pana).stream().map(this::ocupare).toList();
    }

    private OcupareClasaView ocupare(ProgramClasa p) {
        ClasaFitness clasa = p.getClasa();
        int ocupate = p.locuriOcupate();
        BigDecimal grad = O_SUTA.multiply(BigDecimal.valueOf(ocupate))
                .divide(BigDecimal.valueOf(clasa.getCapacitateMaxima()), 1, RoundingMode.HALF_UP);
        return new OcupareClasaView(p.getIdProgram(), clasa.getDenumire(), p.getInterval().getInceput(),
                p.getSalaStudio(), clasa.getCapacitateMaxima(), ocupate, p.locuriDisponibile(), grad);
    }

    @Override
    public List<VenitLunarView> venituriLunare() {
        Map<YearMonth, BigDecimal> abonamente = sumaPeLuna(plataRepository.findAll(),
                p -> YearMonth.from(p.getDataPlata()), p -> p.getSuma().getValoare());
        Map<YearMonth, BigDecimal> produse = sumaPeLuna(vanzareRepository.findAll(),
                v -> YearMonth.from(v.getDataVanzare()), v -> v.getTotalPlata().getValoare());

        TreeSet<YearMonth> luni = new TreeSet<>(abonamente.keySet());
        luni.addAll(produse.keySet());
        return luni.stream()
                .map(luna -> {
                    BigDecimal a = abonamente.getOrDefault(luna, BigDecimal.ZERO);
                    BigDecimal p = produse.getOrDefault(luna, BigDecimal.ZERO);
                    return new VenitLunarView(luna, a, p, a.add(p));
                })
                .toList();
    }

    private static <T> Map<YearMonth, BigDecimal> sumaPeLuna(List<T> elemente, Function<T, YearMonth> luna,
                                                             Function<T, BigDecimal> suma) {
        return elemente.stream().collect(Collectors.groupingBy(luna, TreeMap::new,
                Collectors.reducing(BigDecimal.ZERO, suma, BigDecimal::add)));
    }

    @Override
    public List<OreLucrateLunarView> oreLucrateLunar() {
        Map<Integer, String> tipuri = tipAngajatRepository.findAll().stream()
                .collect(Collectors.toMap(TipAngajat::getIdTipAngajat, TipAngajat::getDenumire));

        record Cheie(Angajat angajat, YearMonth luna) {
        }
        Map<Cheie, List<PontajAngajat>> grupe = angajatRepository.findPontajeInchise().stream()
                .collect(Collectors.groupingBy(p -> new Cheie(p.getAngajat(), YearMonth.from(p.getCheckIn()))));

        return grupe.entrySet().stream()
                .map(e -> new OreLucrateLunarView(
                        e.getKey().angajat().getIdUtilizator(),
                        e.getKey().angajat().getNumeComplet(),
                        tipuri.get(e.getKey().angajat().getIdTipAngajat()),
                        e.getKey().luna(),
                        e.getValue().size(),
                        e.getValue().stream().map(PontajAngajat::getOreLucrate).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparing(OreLucrateLunarView::luna).thenComparing(OreLucrateLunarView::idUtilizator))
                .toList();
    }

    @Override
    public List<CostMentenantaView> costMentenantaEchipamente() {
        return echipamentRepository.findAll().stream()
                .map(e -> new CostMentenantaView(
                        e.getIdEchipament(),
                        e.getDenumire(),
                        e.getStareUzura(),
                        e.getInterventii().size(),
                        e.costTotalMentenanta().getValoare(),
                        e.ultimaVerificare().orElse(null)))
                .sorted(Comparator.comparing(CostMentenantaView::costTotal).reversed())
                .toList();
    }

    @Override
    public List<FeedbackLunarView> feedbackLunar() {
        Map<YearMonth, List<FeedbackClient>> peLuna = clientRepository.findToateFeedbackurile().stream()
                .collect(Collectors.groupingBy(f -> YearMonth.from(f.getDataFeedback()), TreeMap::new, Collectors.toList()));
        return peLuna.entrySet().stream()
                .map(e -> {
                    List<FeedbackClient> recenzii = e.getValue();
                    BigDecimal suma = BigDecimal.valueOf(recenzii.stream().mapToInt(FeedbackClient::getRating).sum());
                    BigDecimal medie = suma.divide(BigDecimal.valueOf(recenzii.size()), 2, RoundingMode.HALF_UP);
                    long negative = recenzii.stream().filter(FeedbackClient::esteNegativ).count();
                    return new FeedbackLunarView(e.getKey(), recenzii.size(), medie, negative);
                })
                .toList();
    }
}