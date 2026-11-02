package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.Plata;

import java.util.List;

public interface PlataRepository extends JpaRepository<Plata, Long> {

    List<Plata> findByIdClientOrderByDataPlataDesc(Integer idClient);

    boolean existsByIdClient(Integer idClient);

    boolean existsByIdAngajat(Integer idAngajat);
}
