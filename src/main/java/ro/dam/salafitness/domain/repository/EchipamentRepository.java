package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.dam.salafitness.domain.model.entity.Echipament;

import java.util.List;

public interface EchipamentRepository extends JpaRepository<Echipament, Integer> {

    List<Echipament> findByStareUzuraIgnoreCase(String stareUzura);

    List<Echipament> findByLocatieInSalaContainingIgnoreCase(String locatie);

    // RB01: tehnicianul cu interventii nu se poate sterge
    @Query("select case when count(m) > 0 then true else false end from MentenantaEchipament m "
            + "where m.idAngajatResponsabil = :idAngajat")
    boolean existaInterventieCuResponsabil(@Param("idAngajat") Integer idAngajat);
}