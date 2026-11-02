package ro.dam.salafitness.domain.model.enums;

/**
 * Statusul unui abonament de client (RV10, RC05).
 */
public enum StatusAbonament implements ValoareBD {
    ACTIV("Activ"),
    EXPIRAT("Expirat");

    private final String valoare;

    StatusAbonament(String valoare) {
        this.valoare = valoare;
    }

    @Override
    public String getValoare() {
        return valoare;
    }

    @jakarta.persistence.Converter(autoApply = true)
    public static class Converter extends ValoareBDConverter<StatusAbonament> {
        public Converter() {
            super(StatusAbonament.class);
        }
    }
}
