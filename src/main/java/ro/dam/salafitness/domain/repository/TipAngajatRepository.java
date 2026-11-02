package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.TipAngajat;

import java.util.Optional;

public interface TipAngajatRepository extends JpaRepository<TipAngajat, Integer> {

    Optional<TipAngajat> findByDenumireIgnoreCase(String denumire);
}
