package ro.dam.salafitness.domain.repository.jpa;

import ro.dam.salafitness.domain.model.entity.Vanzare;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Bonus: depozitul agregatului Vanzare
public interface IVanzareRepository extends IRepository<Vanzare, Long> {

    List<Vanzare> findByClient(Integer idClient);

    List<Vanzare> findInPerioada(LocalDateTime de, LocalDateTime pana);

    // venitul din produse (V3)
    BigDecimal totalInPerioada(LocalDateTime de, LocalDateTime pana);
}