package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.RezervareClasa;

public interface RezervareClasaRepository extends JpaRepository<RezervareClasa, Long> {

    boolean existsByIdClient(Integer idClient);
}
