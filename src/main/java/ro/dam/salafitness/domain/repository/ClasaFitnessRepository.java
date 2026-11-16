package ro.dam.salafitness.domain.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.dam.salafitness.domain.model.entity.ClasaFitness;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClasaFitnessRepository extends JpaRepository<ClasaFitness, Integer> {

    boolean existsByIdAntrenor(Integer idAntrenor);

    List<ClasaFitness> findByIdAntrenor(Integer idAntrenor);

    // V2: toate sesiunile
    @Query("select p from ProgramClasa p join fetch p.clasa order by p.interval.inceput")
    List<ProgramClasa> findToateSesiunile();

    // RA12: sesiunile suprapuse din aceeasi sala
    @Query("select p from ProgramClasa p where lower(p.salaStudio) = lower(:sala) "
            + "and p.interval.inceput < :sfarsit and p.interval.sfarsit > :inceput")
    List<ProgramClasa> findSesiuniSuprapuse(@Param("sala") String sala, @Param("inceput") LocalDateTime inceput,
                                            @Param("sfarsit") LocalDateTime sfarsit);

    // UC08: sesiunile dintr-o perioada
    @Query("select p from ProgramClasa p join fetch p.clasa "
            + "where p.interval.inceput >= :de and p.interval.inceput < :pana order by p.interval.inceput")
    List<ProgramClasa> findSesiuniInPerioada(@Param("de") LocalDateTime de, @Param("pana") LocalDateTime pana);

    // RA01: blocheaza sesiunea pana la commit
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProgramClasa p where p.idProgram = :id")
    Optional<ProgramClasa> findSesiunePentruRezervare(@Param("id") Long idProgram);

    // RB01: clientul cu rezervari nu se poate sterge
    @Query("select case when count(r) > 0 then true else false end from RezervareClasa r "
            + "where r.idClient = :idClient")
    boolean existaRezervareClient(@Param("idClient") Integer idClient);
}