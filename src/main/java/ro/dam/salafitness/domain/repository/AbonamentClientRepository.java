package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.dam.salafitness.domain.model.entity.AbonamentClient;

import java.time.LocalDate;
import java.util.List;

public interface AbonamentClientRepository extends JpaRepository<AbonamentClient, Long> {

    /** v_abonamente_active: data_inceput &le; data &le; data_sfarsit */
    @Query("select a from AbonamentClient a where :data between a.perioada.dataInceput and a.perioada.dataSfarsit")
    List<AbonamentClient> findValabileLa(@Param("data") LocalDate data);

    boolean existsByIdTipAbonament(Integer idTipAbonament);
}
