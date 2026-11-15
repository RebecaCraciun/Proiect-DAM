package ro.dam.salafitness.domain.repository.jpa;

import org.springframework.stereotype.Repository;
import ro.dam.salafitness.domain.model.entity.Vanzare;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Bonus: implementarea JPA a depozitului Vanzare
@Repository
public class VanzareRepositoryJPA extends AbstractRepositoryJPA<Vanzare, Long> implements IVanzareRepository {

    public VanzareRepositoryJPA() {
        super(Vanzare.class);
    }

    @Override
    protected Long idDe(Vanzare vanzare) {
        return vanzare.getIdVanzare();
    }

    @Override
    public List<Vanzare> findByClient(Integer idClient) {
        return entityManager.createQuery(
                        "select v from Vanzare v where v.idClient = :idClient order by v.dataVanzare desc", Vanzare.class)
                .setParameter("idClient", idClient)
                .getResultList();
    }

    @Override
    public List<Vanzare> findInPerioada(LocalDateTime de, LocalDateTime pana) {
        return entityManager.createQuery("select distinct v from Vanzare v left join fetch v.linii "
                        + "where v.dataVanzare >= :de and v.dataVanzare < :pana order by v.dataVanzare", Vanzare.class)
                .setParameter("de", de)
                .setParameter("pana", pana)
                .getResultList();
    }

    @Override
    public BigDecimal totalInPerioada(LocalDateTime de, LocalDateTime pana) {
        BigDecimal total = entityManager.createQuery("select sum(v.totalPlata.valoare) from Vanzare v "
                        + "where v.dataVanzare >= :de and v.dataVanzare < :pana", BigDecimal.class)
                .setParameter("de", de)
                .setParameter("pana", pana)
                .getSingleResult();
        return total == null ? BigDecimal.ZERO.setScale(2) : total;
    }
}