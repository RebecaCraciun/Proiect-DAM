package ro.dam.salafitness.domain.model.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object pentru sume de bani: NUMERIC(10,2) cu CHECK &ge; 0 (RV05, RV08, RV13, RV19-RV22).
 * Imutabil; numele coloanei se stabileste in entitate prin {@code @AttributeOverride}.
 */
@Embeddable
public class Bani implements Comparable<Bani> {

    public static final Bani ZERO = new Bani(BigDecimal.ZERO);

    @NotNull(message = "Suma este obligatorie")
    @DecimalMin(value = "0.00", message = "Suma trebuie sa fie >= 0")
    @Digits(integer = 8, fraction = 2, message = "Suma are maximum 8 cifre intregi si 2 zecimale")
    @Column(name = "valoare", precision = 10, scale = 2)
    private BigDecimal valoare;

    protected Bani() {
        // JPA
    }

    private Bani(BigDecimal valoare) {
        this.valoare = Objects.requireNonNull(valoare, "valoare").setScale(2, RoundingMode.HALF_UP);
    }

    public static Bani of(BigDecimal valoare) {
        return new Bani(valoare);
    }

    public static Bani of(String valoare) {
        return new Bani(new BigDecimal(valoare));
    }

    public static Bani of(long valoare) {
        return new Bani(BigDecimal.valueOf(valoare));
    }

    public Bani plus(Bani alta) {
        return new Bani(valoare.add(alta.valoare));
    }

    public Bani inmultitCu(int factor) {
        return new Bani(valoare.multiply(BigDecimal.valueOf(factor)));
    }

    public boolean esteNegativ() {
        return valoare.signum() < 0;
    }

    public BigDecimal getValoare() {
        return valoare;
    }

    @Override
    public int compareTo(Bani alta) {
        return valoare.compareTo(alta.valoare);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Bani b && valoare.compareTo(b.valoare) == 0);
    }

    @Override
    public int hashCode() {
        return valoare.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return valoare.toPlainString() + " RON";
    }
}
