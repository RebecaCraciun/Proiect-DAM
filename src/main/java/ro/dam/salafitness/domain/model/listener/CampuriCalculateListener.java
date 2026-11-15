package ro.dam.salafitness.domain.model.listener;

import jakarta.persistence.Persistence;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

// RC01, RC02, RC03: recalculeaza campurile inainte de INSERT si UPDATE
public class CampuriCalculateListener {

    @PrePersist
    @PreUpdate
    public void recalculeaza(Object entitate) {
        if (!(entitate instanceof CuCampuriCalculate cuCampuri)) {
            return;
        }
        // o colectie LAZY neincarcata nu se poate citi in timpul flush-ului
        if (cuCampuri.colectieNecesara() != null
                && !Persistence.getPersistenceUtil().isLoaded(entitate, cuCampuri.colectieNecesara())) {
            return;
        }
        cuCampuri.recalculeazaCampuriCalculate();
    }
}