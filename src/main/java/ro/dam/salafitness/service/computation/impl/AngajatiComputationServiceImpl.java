package ro.dam.salafitness.service.computation.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;
import ro.dam.salafitness.domain.model.entity.TipAngajat;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.repository.AngajatRepository;
import ro.dam.salafitness.domain.repository.TipAngajatRepository;
import ro.dam.salafitness.service.computation.AngajatiComputationService;
import ro.dam.salafitness.service.computation.view.OreLucrateLunarView;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AngajatiComputationServiceImpl implements AngajatiComputationService {

    private final AngajatRepository angajatRepository;
    private final TipAngajatRepository tipAngajatRepository;

    public AngajatiComputationServiceImpl(AngajatRepository angajatRepository,
                                          TipAngajatRepository tipAngajatRepository) {
        this.angajatRepository = angajatRepository;
        this.tipAngajatRepository = tipAngajatRepository;
    }

    @Override
    public BigDecimal calculeazaOreLucrate(PontajAngajat pontaj) {
        return pontaj.recalculeazaOreLucrate();
    }

    @Override
    public Bani calculeazaSumaSalariu(Angajat angajat) {
        return angajat.getSalariu();
    }

    @Override
    @Transactional(readOnly = true)
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
}
