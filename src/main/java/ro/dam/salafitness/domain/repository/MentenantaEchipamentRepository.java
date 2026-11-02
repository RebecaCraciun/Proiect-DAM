package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.MentenantaEchipament;

public interface MentenantaEchipamentRepository extends JpaRepository<MentenantaEchipament, Long> {

    boolean existsByIdAngajatResponsabil(Integer idAngajat);
}
