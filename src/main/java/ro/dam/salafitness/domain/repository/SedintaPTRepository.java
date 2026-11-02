package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.dam.salafitness.domain.model.entity.SedintaPT;
import ro.dam.salafitness.domain.model.enums.StatusSedinta;

import java.time.LocalDateTime;

public interface SedintaPTRepository extends JpaRepository<SedintaPT, Long> {

    /** RA13: sedintele neanulate ale antrenorului care incep in intervalul (de, pana). */
    @Query("select case when count(s) > 0 then true else false end from SedintaPT s "
            + "where s.idAntrenor = :idAntrenor and s.status <> :anulat and s.dataOra > :de and s.dataOra < :pana")
    boolean existaSedintaActivaIntre(@Param("idAntrenor") Integer idAntrenor, @Param("de") LocalDateTime de,
                                     @Param("pana") LocalDateTime pana, @Param("anulat") StatusSedinta anulat);

    boolean existsByIdClient(Integer idClient);

    boolean existsByIdAntrenor(Integer idAntrenor);
}
