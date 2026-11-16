package ro.dam.salafitness.service.computation.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.repository.EchipamentRepository;
import ro.dam.salafitness.service.computation.EchipamenteComputationService;
import ro.dam.salafitness.service.computation.view.CostMentenantaView;

import java.util.Comparator;
import java.util.List;

@Service
public class EchipamenteComputationServiceImpl implements EchipamenteComputationService {

    private final EchipamentRepository echipamentRepository;

    public EchipamenteComputationServiceImpl(EchipamentRepository echipamentRepository) {
        this.echipamentRepository = echipamentRepository;
    }

    @Override
    @Transactional(readOnly = true)
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
}
