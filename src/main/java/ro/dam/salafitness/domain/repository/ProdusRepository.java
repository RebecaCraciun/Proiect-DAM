package ro.dam.salafitness.domain.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.dam.salafitness.domain.model.entity.Produs;

import java.util.Optional;

public interface ProdusRepository extends JpaRepository<Produs, Integer> {

    /** UC14: blocheaza produsul pana la scaderea stocului (RC08). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produs p where p.idProdus = :id")
    Optional<Produs> findByIdPentruVanzare(@Param("id") Integer id);
}
