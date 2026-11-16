package ro.dam.salafitness.service.computation.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.model.entity.LinieVanzare;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.Vanzare;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.repository.PlataRepository;
import ro.dam.salafitness.domain.repository.VanzareRepository;
import ro.dam.salafitness.service.computation.VanzariComputationService;
import ro.dam.salafitness.service.computation.view.VenitLunarView;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VanzariComputationServiceImpl implements VanzariComputationService {

    private final PlataRepository plataRepository;
    private final VanzareRepository vanzareRepository;

    public VanzariComputationServiceImpl(PlataRepository plataRepository, VanzareRepository vanzareRepository) {
        this.plataRepository = plataRepository;
        this.vanzareRepository = vanzareRepository;
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
    @Transactional(readOnly = true)
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
}
