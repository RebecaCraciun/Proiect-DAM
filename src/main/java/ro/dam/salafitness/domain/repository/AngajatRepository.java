package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.Angajat;

import java.util.List;

public interface AngajatRepository extends JpaRepository<Angajat, Integer> {

    List<Angajat> findByIdTipAngajat(Integer idTipAngajat);
}
