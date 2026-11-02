package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.Utilizator;

import java.util.Optional;

public interface UtilizatorRepository extends JpaRepository<Utilizator, Integer> {

    boolean existsByEmailAdresa(String adresa);

    Optional<Utilizator> findByEmailAdresa(String adresa);
}
