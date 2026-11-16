# Sprint 3 — Persistence Model Implementation

_Sistem de Management al Sălii de Fitness — modelul de persistență JPA, depozitele și regulile implementate prin `@EntityListeners`_

| | |
|---|---|
| **Baza de date** | PostgreSQL — `sala_fitness` (teste: H2 în modul PostgreSQL) |
| **Stack** | Spring Boot 3.3, Spring Data JPA, Hibernate 6 |
| **Echipa** | Acununei Marius Petrișor, Crăciun Andreea-Rebeca |

## 1. Corespondența cerințe → implementare

| Cerință Sprint 3 | Implementare |
|---|---|
| Entități `@Entity` | 20 de entități de domeniu + `AuditLog` (`domain/model/entity`) |
| `@Inheritance` | `Utilizator` (abstract) ← `Client`, `Angajat`, strategia `JOINED` |
| Value objects: `@Convert` / `@Enumerated` | `@Convert(converter = …)` explicit pe toate cele 6 câmpuri enum; `@Embeddable` pentru `Bani`, `Email`, `Telefon`, `ContactUrgenta`, `PerioadaValabilitate`, `IntervalOrar` |
| Rădăcina agregatului: `@OneToMany` cu `mappedBy`, `fetch`, `cascade`, `orphanRemoval` | `Client`, `Angajat`, `ClasaFitness`, `Vanzare`, `Echipament` (vezi 2.2) |
| Relațiile dintre entitățile componente | `ProgramClasa` → `RezervareClasa` (`@OneToMany`, al doilea nivel al agregatului Clasă Fitness) |
| `JpaRepository<RootEntity, T>` | 12 depozite, **doar pentru rădăcini** (vezi 2.3) |
| Operații custom `findByPropertyName()` | ex. `findByEmailAdresa`, `findByIdTipAngajat`, `findByIdClientOrderByDataVanzareDesc`, `findByStareUzuraIgnoreCase` |
| Operații custom `@Query` | ex. `findSesiuniSuprapuse` (RA12), `findAbonamenteValabileLa` (V1), `findByRol`, `findSesiunePentruRezervare` (cu `@Lock`) |
| Servicii și fabrici care folosesc depozitele JPA | toate serviciile de workflow, validare, calcul și reguli |
| Reguli de calcul cu `@EntityListeners` | `CampuriCalculateListener` — `@PrePersist`, `@PreUpdate` (RC01, RC02, RC03) |
| Reguli de audit cu `@EntityListeners` | `AuditEntitateListener` — `@PostPersist`, `@PostUpdate`, `@PreRemove` (RB04) |
| **(Bonus)** interfață CRUD `IRepository` | `domain/repository/jpa/IRepository` |
| **(Bonus)** `EntityRepositoryJPA` cu `EntityManager` `@Autowired` | `AbstractRepositoryJPA` + `VanzareRepositoryJPA` (agregatul Vânzare) |
| Teste noi pentru depozite | 7 clase, 29 de teste (vezi 4) |
| Teste actualizate pentru serviciile de business | `ClientWorkflowServiceTest`, `VanzareWorkflowServiceTest` (auditul pe două niveluri) |

## 2. Modelul de persistență

### 2.1 Enumerări: de ce `@Convert` și nu `@Enumerated`

`@Enumerated(EnumType.STRING)` scrie în coloană **numele constantei Java** (`IN_ASTEPTARE`, `PERMIS`). Constrângerile `CHECK` din baza de date acceptă însă valorile de business, unele cu diacritice: `'În așteptare'`, `'Permis'`, `'Confirmat'`. De aceea fiecare enumerare implementează `ValoareBD` și are un `AttributeConverter` propriu (`ValoareBDConverter`), aplicat explicit pe câmp:

```java
@Convert(converter = StatusPlataSalariu.Converter.class)
@Column(name = "status_plata", nullable = false, length = 20)
private StatusPlataSalariu statusPlata;
```

Testul `AngajatRepositoryTest.salariul_…` verifică prin SQL nativ că în coloană ajunge exact `'În așteptare'`.

### 2.2 Cascada în agregate

`fetch = FetchType.LAZY` este scris explicit pe toate relațiile `@OneToMany`. Tipul de cascadă urmează regulile de ștergere din Sprint 1:

| Rădăcină → componentă | Cascadă | Motiv |
|---|---|---|
| `Vanzare` → `LinieVanzare` | `ALL` + `orphanRemoval` | Compoziție pură: o linie nu există fără vânzare (RB02, `ON DELETE CASCADE`) |
| `Client` → `FeedbackClient` | `ALL` + `orphanRemoval` | Recenziile se șterg odată cu clientul (RB02) |
| `Client` → `AbonamentClient` | `PERSIST`, `MERGE` | Istoric financiar: nu se șterge odată cu rădăcina (RB01, `ON DELETE RESTRICT`) |
| `Angajat` → `PontajAngajat`, `SalariuIstoric` | `PERSIST`, `MERGE` | Istoric operațional și salarial (RB01) |
| `ClasaFitness` → `ProgramClasa` → `RezervareClasa` | `PERSIST`, `MERGE` | Rezervările sunt istoric de participare (RB01) |
| `Echipament` → `MentenantaEchipament` | `PERSIST`, `MERGE` | Istoricul intervențiilor și al costurilor (RB01) |

Lipsa lui `REMOVE` / `orphanRemoval` este, așadar, intenționată: modelul JPA respectă aceleași reguli ca și cheile străine din baza de date.

### 2.3 Depozite doar pentru rădăcinile agregatelor

Cerința „`JpaRepository<RootEntity, T>`” și regula din README („modificările se fac numai prin rădăcină”) au condus la **eliminarea celor 7 depozite ale entităților componente** (`AbonamentClient`, `FeedbackClient`, `PontajAngajat`, `ProgramClasa`, `RezervareClasa`, `LinieVanzare`, `MentenantaEchipament`). Interogările lor de citire au fost mutate în depozitul rădăcinii:

| Depozit (rădăcină) | Interogări asupra componentelor |
|---|---|
| `ClientRepository` | `findAbonamenteValabileLa`, `findCuAbonamentValabilLa`, `existaAbonamentDeTip`, `findToateFeedbackurile` |
| `AngajatRepository` | `findPontajeInchise`, `findByRol` |
| `ClasaFitnessRepository` | `findToateSesiunile`, `findSesiuniSuprapuse`, `findSesiuniInPerioada`, `findSesiunePentruRezervare` (`PESSIMISTIC_WRITE`), `existaRezervareClient` |
| `VanzareRepository` | `existaLinieCuProdus`, `findInPerioadaCuLinii` |
| `EchipamentRepository` | `existaInterventieCuResponsabil` |

Depozitele rămase: `Utilizator`, `Client`, `Angajat`, `TipAngajat`, `TipAbonament`, `Plata`, `ClasaFitness`, `SedintaPT`, `AccesSala`, `Echipament`, `Produs`, `Vanzare`, plus `AuditLog` (jurnal tehnic, nu agregat de domeniu).

## 3. Reguli implementate cu `@EntityListeners`

### 3.1 Reguli de calcul — `CampuriCalculateListener`

Formulele rămân în entități (model bogat). Listenerul le reaplică automat **înainte de fiecare INSERT (`@PrePersist`) și UPDATE (`@PreUpdate`)**, deci în baza de date nu poate ajunge o valoare inconsistentă, nici dacă entitatea a fost modificată ocolind metodele de business. Entitățile participante implementează `CuCampuriCalculate`:

| Entitate | Regulă |
|---|---|
| `LinieVanzare` | RC01 `total_linie = cantitate × pret_unitar` |
| `Vanzare` | RC02 `total_plata = Σ total_linie` |
| `PontajAngajat` | RC03 `ore_lucrate = (check_out − check_in)` în ore |

Pentru RC02, listenerul verifică întâi (`PersistenceUtil.isLoaded`) că liniile vânzării sunt încărcate. Hibernate nu permite inițializarea unei colecții LAZY în timpul flush-ului. Dacă liniile nu au fost încărcate, înseamnă că nu s-au modificat, deci totalul stocat rămâne valid. Echivalentele din baza de date sunt triggerele T2 și T3 (Anexa B din README).

### 3.2 Regula de audit — `AuditEntitateListener` (RB04)

Orice creare, modificare sau ștergere a unei entități `Auditabil` se scrie în `audit_log`, indiferent dacă operația vine dintr-un serviciu de workflow sau direct dintr-un depozit. Entitățile auditate sunt cele financiare și cele cu date despre persoane: `Plata`, `Vanzare`, `SalariuIstoric`, `AbonamentClient`, `TipAbonament`, `Produs`, `Client`, `Angajat`.

Două decizii de implementare:

1. **Callback-uri:** `@PostPersist` și `@PostUpdate`, pentru că id-ul generat prin `IDENTITY` există abia după INSERT. `@PreRemove`, pentru că id-ul încă există înainte de DELETE.
2. **Scriere prin JDBC, nu prin `EntityManager`:** specificația JPA interzice modificarea contextului de persistență din callback-uri. `JdbcTemplate` folosește aceeași conexiune și aceeași tranzacție, deci intrarea de audit se anulează odată cu operația (rollback).

Auditul are acum două niveluri:

| Nivel | Unde | Ce reține |
|---|---|---|
| De business | `AuditService` (apelat din workflow) | operația de business (`VANZARE`, `ACHIZITIE_ABONAMENT` …) **și actorul** (`id_utilizator`) |
| De persistență | `AuditEntitateListener` | `CREARE` / `MODIFICARE` / `STERGERE` pe orice cale de modificare; `id_utilizator` = NULL |

Detaliile scrise în jurnal nu conțin date personale (RB07): pentru `Client` se reține doar id-ul și tipul abonamentului.

Listenerul este instanțiat de Spring: Spring Boot configurează automat `SpringBeanContainer` pentru Hibernate, de aceea `JdbcTemplate` și `Clock` se pot injecta cu `@Autowired`.

## 4. (Bonus) Depozit JPA complet

```
IRepository<T, ID>                      ← interfața CRUD generică
  └─ IVanzareRepository                  ← + findByClient, findInPerioada, totalInPerioada
AbstractRepositoryJPA<T, ID>            ← CRUD cu EntityManager (@Autowired)
  └─ VanzareRepositoryJPA (@Repository)  ← operațiile custom în JPQL
```

`save()` face `persist` pentru o entitate nouă (fără id) și `merge` pentru una existentă, adică exact ce face `SimpleJpaRepository` din Spring Data. Testul `VanzareRepositoryJPATest.save_peOEntitateDetasata_faceMerge` verifică ambele ramuri.

## 5. Teste noi

| Clasă | Ce verifică |
|---|---|
| `ClientRepositoryTest` | operații derivate, filtre `@Query` (UC07), cascada spre abonamente, `@Convert`, ștergerea în cascadă a feedback-ului (RB02) |
| `AngajatRepositoryTest` | cascada pontajelor și a salariilor, filtrul pontajelor închise (V4), `findByRol`, convertorul cu diacritice |
| `ClasaFitnessRepositoryTest` | intervalul semi-deschis pentru RA12, cascada pe două niveluri, blocarea pesimistă (RA01) |
| `VanzareRepositoryTest` | `CascadeType.ALL`, `orphanRemoval`, filtre după perioadă și client |
| `EchipamentRepositoryTest` | cascada intervențiilor, operații derivate |
| `EntityListenersTest` | RC01 / RC02 / RC03 recalculate la `@PrePersist` / `@PreUpdate`; audit CREARE / MODIFICARE / STERGERE; fără date personale |
| `VanzareRepositoryJPATest` | CRUD-ul depozitului bonus, persist vs merge, operațiile custom |

Testele de depozit extind `AbstractRepositoryTest`. Aceasta oferă `sincronizeaza()` (flush + clear, pentru ca citirea următoare să vină din baza de date) și interogări native, pentru a verifica valoarea exactă scrisă în coloană.

## 6. Rulare

```bash
mvn test                                  # toate testele (H2 în memorie)
mvn test -Dtest='*RepositoryTest,EntityListenersTest,VanzareRepositoryJPATest'   # doar Sprint 3
```
