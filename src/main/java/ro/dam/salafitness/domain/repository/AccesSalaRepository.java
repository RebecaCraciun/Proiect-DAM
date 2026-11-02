package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.AccesSala;

public interface AccesSalaRepository extends JpaRepository<AccesSala, Long> {

    boolean existsByIdUtilizator(Integer idUtilizator);
}
