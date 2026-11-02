package ro.dam.salafitness.domain.model.enums;

/**
 * Statusul unei rezervari la o sesiune de clasa (RV16).
 */
public enum StatusRezervare implements ValoareBD {
    CONFIRMAT("Confirmat"),
    ANULAT("Anulat"),
    PARTICIPAT("Participat");

    private final String valoare;

    StatusRezervare(String valoare) {
        this.valoare = valoare;
    }

    @Override
    public String getValoare() {
        return valoare;
    }

    @jakarta.persistence.Converter(autoApply = true)
    public static class Converter extends ValoareBDConverter<StatusRezervare> {
        public Converter() {
            super(StatusRezervare.class);
        }
    }
}
