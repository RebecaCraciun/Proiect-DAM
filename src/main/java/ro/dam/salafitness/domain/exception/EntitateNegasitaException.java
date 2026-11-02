package ro.dam.salafitness.domain.exception;

public class EntitateNegasitaException extends RuntimeException {

    public EntitateNegasitaException(String entitate, Object id) {
        super(entitate + " cu id " + id + " nu exista");
    }
}
