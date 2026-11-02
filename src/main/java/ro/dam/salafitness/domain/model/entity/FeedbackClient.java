package ro.dam.salafitness.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Componenta a agregatului Client (compozitie, ON DELETE CASCADE - RB02).
 */
@Entity
@Table(name = "feedback_clienti")
public class FeedbackClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feedback")
    private Long idFeedback;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utilizator_client", nullable = false)
    private Client client;

    /** RV23 */
    @NotNull(message = "Ratingul este obligatoriu")
    @Min(value = 1, message = "Ratingul minim este 1")
    @Max(value = 5, message = "Ratingul maxim este 5")
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Size(max = 2000, message = "Comentariul are maximum 2000 de caractere")
    @Column(name = "comentariu", length = 2000)
    private String comentariu;

    @NotNull
    @Column(name = "data_feedback", nullable = false)
    private LocalDateTime dataFeedback;

    protected FeedbackClient() {
        // JPA
    }

    FeedbackClient(Client client, Integer rating, String comentariu, LocalDateTime dataFeedback) {
        this.client = client;
        this.rating = rating;
        this.comentariu = comentariu;
        this.dataFeedback = dataFeedback;
    }

    /** v_feedback_lunar.recenzii_negative */
    public boolean esteNegativ() {
        return rating != null && rating < 2;
    }

    public Long getIdFeedback() {
        return idFeedback;
    }

    public Client getClient() {
        return client;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComentariu() {
        return comentariu;
    }

    public LocalDateTime getDataFeedback() {
        return dataFeedback;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof FeedbackClient f && idFeedback != null && idFeedback.equals(f.getIdFeedback()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idFeedback);
    }
}
