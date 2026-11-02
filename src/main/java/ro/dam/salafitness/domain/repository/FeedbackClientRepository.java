package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.FeedbackClient;

public interface FeedbackClientRepository extends JpaRepository<FeedbackClient, Long> {
}
