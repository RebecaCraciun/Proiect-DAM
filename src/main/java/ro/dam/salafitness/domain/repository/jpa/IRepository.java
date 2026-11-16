package ro.dam.salafitness.domain.repository.jpa;

import java.util.List;
import java.util.Optional;

// Bonus: interfata CRUD generica
public interface IRepository<T, ID> {

    // persist pentru o entitate noua, merge pentru una existenta
    T save(T entitate);

    Optional<T> findById(ID id);

    List<T> findAll();

    boolean existsById(ID id);

    long count();

    void delete(T entitate);

    void deleteById(ID id);
}