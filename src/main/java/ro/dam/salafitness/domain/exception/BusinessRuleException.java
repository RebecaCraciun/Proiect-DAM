package ro.dam.salafitness.domain.exception;

/**
 * Incalcarea unei reguli de business; codul regulii (ex. "RA01") trimite la tabelele din README.
 */
public class BusinessRuleException extends RuntimeException {

    private final String codRegula;

    public BusinessRuleException(String codRegula, String mesaj) {
        super(codRegula + ": " + mesaj);
        this.codRegula = codRegula;
    }

    public String getCodRegula() {
        return codRegula;
    }
}
