package ro.dam.salafitness.domain.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProgramClasaRepository extends JpaRepository<ProgramClasa, Long> {

    /** RA12: sesiunile din aceeasi sala care se suprapun cu intervalul [inceput, sfarsit). */
    @Query("select p from ProgramClasa p where lower(p.salaStudio) = lower(:sala) "
            + "and p.interval.inceput < :sfarsit and p.interval.sfarsit > :inceput")
    List<ProgramClasa> findSuprapuse(@Param("sala") String sala, @Param("inceput") LocalDateTime inceput,
                                     @Param("sfarsit") LocalDateTime sfarsit);

    /** UC08 */
    @Query("select p from ProgramClasa p where p.interval.inceput >= :de and p.interval.inceput < :pana "
            + "order by p.interval.inceput")
    List<ProgramClasa> findInPerioada(@Param("de") LocalDateTime de, @Param("pana") LocalDateTime pana);

    /** RA01: blocheaza sesiunea pe durata rezervarii (echivalentul FOR UPDATE din trigger-ul T1). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProgramClasa p where p.idProgram = :id")
    Optional<ProgramClasa> findByIdPentruRezervare(@Param("id") Long id);
}
