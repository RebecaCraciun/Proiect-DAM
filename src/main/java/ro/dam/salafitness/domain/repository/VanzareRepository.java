package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.dam.salafitness.domain.model.entity.Vanzare;

import java.time.LocalDateTime;
import java.util.List;

public interface VanzareRepository extends JpaRepository<Vanzare, Long> {

    boolean existsByIdClient(Integer idClient);

    boolean existsByIdAngajat(Integer idAngajat);

    List<Vanzare> findByIdClientOrderByDataVanzareDesc(Integer idClient);

    // vanzarile dintr-o perioada, cu liniile incarcate
    @Query("select distinct v from Vanzare v left join fetch v.linii "
            + "where v.dataVanzare >= :de and v.dataVanzare < :pana order by v.dataVanzare")
    List<Vanzare> findInPerioadaCuLinii(@Param("de") LocalDateTime de, @Param("pana") LocalDateTime pana);

    // RB01: produsul vandut nu se poate sterge
    @Query("select case when count(l) > 0 then true else false end from LinieVanzare l where l.idProdus = :idProdus")
    boolean existaLinieCuProdus(@Param("idProdus") Integer idProdus);
}