package ro.dam.salafitness.support;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

// baza testelor de persistenta (Sprint 3)
public abstract class AbstractRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    protected EntityManager entityManager;

    // scrie in BD si goleste memoria Hibernate
    protected void sincronizeaza() {
        entityManager.flush();
        entityManager.clear();
    }

    // valoarea unei coloane, citita cu SQL nativ (parametrul :id)
    protected Object valoareNativa(String sql, Object id) {
        return entityManager.createNativeQuery(sql).setParameter("id", id).getSingleResult();
    }

    protected BigDecimal zecimalNativ(String sql, Object id) {
        return new BigDecimal(valoareNativa(sql, id).toString());
    }

    protected long numarNativ(String sql, Object id) {
        return ((Number) valoareNativa(sql, id)).longValue();
    }

    // compara zecimale indiferent de scara (2.0 == 2.00)
    protected static boolean egal(String asteptat, BigDecimal valoare) {
        return valoare != null && new BigDecimal(asteptat).compareTo(valoare) == 0;
    }
}