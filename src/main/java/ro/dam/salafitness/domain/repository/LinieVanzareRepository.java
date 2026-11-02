package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.LinieVanzare;

public interface LinieVanzareRepository extends JpaRepository<LinieVanzare, Long> {

    boolean existsByIdProdus(Integer idProdus);
}
