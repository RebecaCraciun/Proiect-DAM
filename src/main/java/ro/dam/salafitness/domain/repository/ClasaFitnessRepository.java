package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.ClasaFitness;

public interface ClasaFitnessRepository extends JpaRepository<ClasaFitness, Integer> {

    boolean existsByIdAntrenor(Integer idAntrenor);
}
