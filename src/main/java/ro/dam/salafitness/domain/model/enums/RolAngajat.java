package ro.dam.salafitness.domain.model.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Rolurile cunoscute din nomenclatorul tip_angajat. Nomenclatorul ramane tabela (TipAngajat);
 * enumerarea este folosita de regulile RA09, RA10, RA11 pentru a compara denumirile.
 */
public enum RolAngajat {
    ANTRENOR("Antrenor"),
    RECEPTIONER("Recepționer"),
    FEMEIE_DE_SERVICIU("Femeie de serviciu"),
    MANAGER("Manager"),
    TEHNIC("Tehnic");

    private final String denumire;

    RolAngajat(String denumire) {
        this.denumire = denumire;
    }

    public String getDenumire() {
        return denumire;
    }

    public static Optional<RolAngajat> dinDenumire(String denumire) {
        return Arrays.stream(values()).filter(r -> r.denumire.equalsIgnoreCase(denumire)).findFirst();
    }
}
