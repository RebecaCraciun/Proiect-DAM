package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.dam.salafitness.domain.model.entity.AbonamentClient;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.FeedbackClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Integer> {

    Optional<Client> findByEmailAdresa(String adresa);

    List<Client> findByNumeContainingIgnoreCaseOrPrenumeContainingIgnoreCase(String nume, String prenume);

    // clientii cu abonament valabil la o data (UC07)
    @Query("select distinct c from Client c join c.abonamente a "
            + "where :data between a.perioada.dataInceput and a.perioada.dataSfarsit")
    List<Client> findCuAbonamentValabilLa(@Param("data") LocalDate data);

    // V1: abonamentele valabile la o data
    @Query("select a from AbonamentClient a join fetch a.client "
            + "where :data between a.perioada.dataInceput and a.perioada.dataSfarsit")
    List<AbonamentClient> findAbonamenteValabileLa(@Param("data") LocalDate data);

    // RB01: tipul de abonament folosit nu se poate sterge
    @Query("select case when count(a) > 0 then true else false end from AbonamentClient a "
            + "where a.idTipAbonament = :idTip")
    boolean existaAbonamentDeTip(@Param("idTip") Integer idTipAbonament);

    // V6: toate recenziile
    @Query("select f from FeedbackClient f order by f.dataFeedback")
    List<FeedbackClient> findToateFeedbackurile();
}
