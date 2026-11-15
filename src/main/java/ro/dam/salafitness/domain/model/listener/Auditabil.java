package ro.dam.salafitness.domain.model.listener;

public interface Auditabil {

    Object idAudit();

    // fara date personale (RB07)
    default String detaliiAudit() {
        return null;
    }
}