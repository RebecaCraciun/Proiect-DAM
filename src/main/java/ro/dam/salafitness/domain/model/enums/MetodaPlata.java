package ro.dam.salafitness.domain.model.enums;

/**
 * Metoda de plata acceptata la receptie (RV13).
 */
public enum MetodaPlata implements ValoareBD {
    CARD("Card"),
    NUMERAR("Numerar"),
    TRANSFER("Transfer");

    private final String valoare;

    MetodaPlata(String valoare) {
        this.valoare = valoare;
    }

    @Override
    public String getValoare() {
        return valoare;
    }

    @jakarta.persistence.Converter(autoApply = true)
    public static class Converter extends ValoareBDConverter<MetodaPlata> {
        public Converter() {
            super(MetodaPlata.class);
        }
    }
}
