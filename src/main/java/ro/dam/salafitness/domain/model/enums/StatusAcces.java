package ro.dam.salafitness.domain.model.enums;

/**
 * Rezultatul unei incercari de acces in sala (RV18, RA07).
 */
public enum StatusAcces implements ValoareBD {
    PERMIS("Permis"),
    RESPINS("Respins");

    private final String valoare;

    StatusAcces(String valoare) {
        this.valoare = valoare;
    }

    @Override
    public String getValoare() {
        return valoare;
    }

    @jakarta.persistence.Converter(autoApply = true)
    public static class Converter extends ValoareBDConverter<StatusAcces> {
        public Converter() {
            super(StatusAcces.class);
        }
    }
}
