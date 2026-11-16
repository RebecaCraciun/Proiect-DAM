package ro.dam.salafitness.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.PontajAngajat;

import java.util.List;

public interface AngajatRepository extends JpaRepository<Angajat, Integer> {

    List<Angajat> findByIdTipAngajat(Integer idTipAngajat);

    // angajatii cu un anumit rol (RA09, RA10, RA11)
    @Query("select a from Angajat a where a.idTipAngajat in "
            + "(select t.idTipAngajat from TipAngajat t where lower(t.denumire) = lower(:rol))")
    List<Angajat> findByRol(@Param("rol") String denumireRol);

    // V4: doar pontajele inchise
    @Query("select p from PontajAngajat p join fetch p.angajat where p.checkOut is not null")
    List<PontajAngajat> findPontajeInchise();
}