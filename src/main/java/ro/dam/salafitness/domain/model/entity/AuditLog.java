package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Jurnal de audit (trasabilitate RB04): cine a facut ce operatie de business si asupra carei entitati.
 * Tabela nu face parte din cele 20 de tabele ale domeniului; scriptul ei este in resources/db/01_sala_fitness.sql.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_audit")
    private Long idAudit;

    @NotNull
    @Column(name = "data_ora", nullable = false)
    private LocalDateTime dataOra;

    @NotBlank
    @Size(max = 50)
    @Column(name = "actiune", nullable = false, length = 50)
    private String actiune;

    @NotBlank
    @Size(max = 50)
    @Column(name = "entitate", nullable = false, length = 50)
    private String entitate;

    @Size(max = 50)
    @Column(name = "id_entitate", length = 50)
    private String idEntitate;

    /** Utilizatorul (de regula angajatul) care a efectuat operatia; null pentru operatii de sistem. */
    @Column(name = "id_utilizator")
    private Integer idUtilizator;

    @Size(max = 1000)
    @Column(name = "detalii", length = 1000)
    private String detalii;

    protected AuditLog() {
        // JPA
    }

    public AuditLog(LocalDateTime dataOra, String actiune, String entitate, String idEntitate, Integer idUtilizator,
                    String detalii) {
        this.dataOra = dataOra;
        this.actiune = actiune;
        this.entitate = entitate;
        this.idEntitate = idEntitate;
        this.idUtilizator = idUtilizator;
        this.detalii = detalii;
    }

    public Long getIdAudit() {
        return idAudit;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public String getActiune() {
        return actiune;
    }

    public String getEntitate() {
        return entitate;
    }

    public String getIdEntitate() {
        return idEntitate;
    }

    public Integer getIdUtilizator() {
        return idUtilizator;
    }

    public String getDetalii() {
        return detalii;
    }
}
