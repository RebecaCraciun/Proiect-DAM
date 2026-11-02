package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;

import java.util.List;

public interface PontajAngajatRepository extends JpaRepository<PontajAngajat, Long> {

    List<PontajAngajat> findByCheckOutIsNotNull();
}
