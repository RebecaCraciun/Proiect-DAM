package ro.dam.salafitness.domain.repository.jpa;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

// Bonus: CRUD-ul generic scris cu EntityManager
@Transactional(readOnly = true)
public abstract class AbstractRepositoryJPA<T, ID> implements IRepository<T, ID> {

    @Autowired
    protected EntityManager entityManager;

    private final Class<T> tipEntitate;

    protected AbstractRepositoryJPA(Class<T> tipEntitate) {
        this.tipEntitate = Objects.requireNonNull(tipEntitate);
    }

    // id-ul entitatii; null daca nu a fost salvata inca
    protected abstract ID idDe(T entitate);

    protected String numeEntitate() {
        return tipEntitate.getSimpleName();
    }

    @Override
    @Transactional
    public T save(T entitate) {
        if (idDe(entitate) == null) {
            entityManager.persist(entitate);
            return entitate;
        }
        return entityManager.merge(entitate);
    }

    @Override
    public Optional<T> findById(ID id) {
        return id == null ? Optional.empty() : Optional.ofNullable(entityManager.find(tipEntitate, id));
    }

    @Override
    public List<T> findAll() {
        return entityManager.createQuery("select e from " + numeEntitate() + " e", tipEntitate).getResultList();
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        return entityManager.createQuery("select count(e) from " + numeEntitate() + " e", Long.class)
                .getSingleResult();
    }

    @Override
    @Transactional
    public void delete(T entitate) {
        entityManager.remove(entityManager.contains(entitate) ? entitate : entityManager.merge(entitate));
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }
}