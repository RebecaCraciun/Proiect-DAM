package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.TipAbonament;

import java.util.Optional;

public interface TipAbonamentRepository extends JpaRepository<TipAbonament, Integer> {

    boolean existsByDenumireIgnoreCase(String denumire);

    Optional<TipAbonament> findByDenumireIgnoreCase(String denumire);
}
