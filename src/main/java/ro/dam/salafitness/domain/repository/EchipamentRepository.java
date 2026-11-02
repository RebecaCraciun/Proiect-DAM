package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.Echipament;

public interface EchipamentRepository extends JpaRepository<Echipament, Integer> {
}
