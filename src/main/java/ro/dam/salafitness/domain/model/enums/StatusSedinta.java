package ro.dam.salafitness.domain.model.enums;

/**
 * Statusul unei sedinte de personal training (RV17).
 */
public enum StatusSedinta implements ValoareBD {
    PROGRAMAT("Programat"),
    FINALIZAT("Finalizat"),
    ANULAT("Anulat"),
    ABSENT("Absent");

    private final String valoare;

    StatusSedinta(String valoare) {
        this.valoare = valoare;
    }

    @Override
    public String getValoare() {
        return valoare;
    }

    @jakarta.persistence.Converter(autoApply = true)
    public static class Converter extends ValoareBDConverter<StatusSedinta> {
        public Converter() {
            super(StatusSedinta.class);
        }
    }
}
