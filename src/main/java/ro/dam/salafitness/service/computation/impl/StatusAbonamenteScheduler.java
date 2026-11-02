package ro.dam.salafitness.service.computation.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ro.dam.salafitness.service.computation.FieldComputationService;

/**
 * RC05: statusul stocat al abonamentelor (Activ / Expirat) se recalculeaza zilnic, dupa miezul noptii.
 */
@Component
public class StatusAbonamenteScheduler {

    private static final Logger log = LoggerFactory.getLogger(StatusAbonamenteScheduler.class);

    private final FieldComputationService fieldComputationService;

    public StatusAbonamenteScheduler(FieldComputationService fieldComputationService) {
        this.fieldComputationService = fieldComputationService;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "Europe/Bucharest")
    public void actualizeazaStatusAbonamente() {
        int modificate = fieldComputationService.actualizeazaStatusAbonamente();
        log.info("RC05: {} abonamente si-au schimbat statusul", modificate);
    }
}
