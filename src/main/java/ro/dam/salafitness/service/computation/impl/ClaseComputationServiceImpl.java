package ro.dam.salafitness.service.computation.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.model.entity.ClasaFitness;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.domain.repository.ClasaFitnessRepository;
import ro.dam.salafitness.service.computation.ClaseComputationService;
import ro.dam.salafitness.service.computation.view.OcupareClasaView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClaseComputationServiceImpl implements ClaseComputationService {

    private static final BigDecimal O_SUTA = BigDecimal.valueOf(100);

    private final ClasaFitnessRepository clasaFitnessRepository;

    public ClaseComputationServiceImpl(ClasaFitnessRepository clasaFitnessRepository) {
        this.clasaFitnessRepository = clasaFitnessRepository;
    }

    @Override
    public int calculeazaLocuriLibere(ProgramClasa sesiune) {
        return sesiune.locuriDisponibile();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OcupareClasaView> ocupareClase() {
        return clasaFitnessRepository.findToateSesiunile().stream().map(this::ocupare).toList();
    }

    @Override
    @Transactional(readOnly = true)
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
}
