package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.Vanzare;

public interface VanzareRepository extends JpaRepository<Vanzare, Long> {

    boolean existsByIdClient(Integer idClient);

    boolean existsByIdAngajat(Integer idAngajat);
}
