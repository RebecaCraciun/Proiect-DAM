package ro.dam.salafitness.domain.model.enums;

/**
 * Statusul unei plati salariale (RV12, RB05).
 */
public enum StatusPlataSalariu implements ValoareBD {
    ACHITAT("Achitat"),
    IN_ASTEPTARE("În așteptare");

    private final String valoare;

    StatusPlataSalariu(String valoare) {
        this.valoare = valoare;
    }

    @Override
    public String getValoare() {
        return valoare;
    }

    @jakarta.persistence.Converter(autoApply = true)
    public static class Converter extends ValoareBDConverter<StatusPlataSalariu> {
        public Converter() {
            super(StatusPlataSalariu.class);
        }
    }
}
