package ro.dam.salafitness.domain.model.enums;

import jakarta.persistence.AttributeConverter;

import java.util.Arrays;

/**
 * Converter generic enum &lt;-&gt; VARCHAR, folosit pentru toate enumerarile domeniului.
 */
public abstract class ValoareBDConverter<E extends Enum<E> & ValoareBD> implements AttributeConverter<E, String> {

    private final Class<E> tip;

    protected ValoareBDConverter(Class<E> tip) {
        this.tip = tip;
    }

    @Override
    public String convertToDatabaseColumn(E atribut) {
        return atribut == null ? null : atribut.getValoare();
    }

    @Override
    public E convertToEntityAttribute(String valoare) {
        if (valoare == null) {
            return null;
        }
        return Arrays.stream(tip.getEnumConstants())
                .filter(e -> e.getValoare().equals(valoare))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Valoare necunoscuta '" + valoare + "' pentru " + tip.getSimpleName()));
    }
}
