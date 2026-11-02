package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.dam.salafitness.domain.model.entity.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Integer> {

    Optional<Client> findByEmailAdresa(String adresa);

    List<Client> findByNumeContainingIgnoreCaseOrPrenumeContainingIgnoreCase(String nume, String prenume);
}
