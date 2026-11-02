package ro.dam.salafitness.service.validation;

import jakarta.validation.ConstraintViolation;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.enums.RolAngajat;
import ro.dam.salafitness.domain.model.vo.IntervalOrar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Reguli de validare: JavaBean Validation la nivel de camp (RV01-RV23, adnotarile din entitati) si
 * reguli de agregat care implica mai multe agregate si nu pot fi protejate de o singura radacina.
 * Incalcarile sunt raportate prin {@link jakarta.validation.ConstraintViolationException} (camp) sau
 * {@link ro.dam.salafitness.domain.exception.BusinessRuleException} (agregat).
 */
public interface DomainValidationService {

    /** Ruleaza JavaBean Validation si intoarce incalcarile, fara exceptie. */
    <T> Set<ConstraintViolation<T>> verifica(T entitate);

    /** Ruleaza JavaBean Validation si arunca ConstraintViolationException daca exista incalcari. */
    <T> T valideaza(T entitate);

    /** RV01: emailul este unic in tabela utilizator. */
    void verificaEmailUnic(String email);

    /** RV07: denumirea tipului de abonament este unica. */
    void verificaDenumireTipAbonamentUnica(String denumire);

    /** RA09, RA10, RA11: angajatul exista si are rolul cerut. */
    Angajat verificaRol(Integer idAngajat, RolAngajat rol, String codRegula);

    /** RA09 */
    Angajat verificaAntrenor(Integer idAngajat);

    /** RA10 */
    Angajat verificaTehnician(Integer idAngajat);

    /** RA11 */
    Angajat verificaReceptioner(Integer idAngajat);

    /** Use case-uri cu mai multi actori (ex. UC23: Manager sau Receptioner): angajatul are unul dintre roluri. */
    Angajat verificaUnulDinRoluri(Integer idAngajat, String codRegula, RolAngajat... roluri);

    /** UC07: exista un abonament valid al clientului la data indicata. */
    boolean areAbonamentValid(Integer idClient, LocalDate data);

    /** RA03 / RA07: arunca exceptie daca nu exista abonament valid. */
    Client verificaAbonamentValid(Integer idClient, LocalDate data, String codRegula);

    /** RA12: nicio alta sesiune din aceeasi sala nu se suprapune cu intervalul. */
    void verificaSalaLibera(String salaStudio, IntervalOrar interval, Long idProgramExclus);

    /** RA13: antrenorul nu are alta sedinta PT neanulata la aceeasi ora. */
    void verificaDisponibilitateAntrenor(Integer idAntrenor, LocalDateTime dataOra);

    /** RB08: un utilizator are un singur profil (client sau angajat). */
    void verificaProfilUnic(Integer idUtilizator);
}
