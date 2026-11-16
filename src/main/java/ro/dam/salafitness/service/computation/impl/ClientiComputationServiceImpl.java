package ro.dam.salafitness.service.computation.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.model.entity.FeedbackClient;
import ro.dam.salafitness.domain.repository.ClientRepository;
import ro.dam.salafitness.service.computation.ClientiComputationService;
import ro.dam.salafitness.service.computation.view.FeedbackLunarView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class ClientiComputationServiceImpl implements ClientiComputationService {

    private final ClientRepository clientRepository;

    public ClientiComputationServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    @Transactional(readOnly = true)
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
