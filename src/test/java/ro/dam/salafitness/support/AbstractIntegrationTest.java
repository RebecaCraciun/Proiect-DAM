package ro.dam.salafitness.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.factory.AngajatFactory;
import ro.dam.salafitness.domain.factory.ClientFactory;
import ro.dam.salafitness.domain.factory.DateAngajatNou;
import ro.dam.salafitness.domain.factory.DateClientNou;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.Produs;
import ro.dam.salafitness.domain.model.entity.TipAbonament;
import ro.dam.salafitness.domain.model.entity.TipAngajat;
import ro.dam.salafitness.domain.model.enums.RolAngajat;
import ro.dam.salafitness.domain.model.vo.Bani;
import ro.dam.salafitness.domain.repository.AngajatRepository;
import ro.dam.salafitness.domain.repository.ClientRepository;
import ro.dam.salafitness.domain.repository.ProdusRepository;
import ro.dam.salafitness.domain.repository.TipAbonamentRepository;
import ro.dam.salafitness.domain.repository.TipAngajatRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Baza testelor de integrare: context Spring complet pe H2, fiecare test intr-o tranzactie anulata la final.
 * Inainte de fiecare test exista cate un angajat pentru fiecare rol din nomenclatorul tip_angajat.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
public abstract class AbstractIntegrationTest {

    /** Miercuri, 28 octombrie 2026, ora 10:00. */
    public static final LocalDateTime ACUM = LocalDateTime.of(2026, 10, 28, 10, 0);
    public static final LocalDate AZI = ACUM.toLocalDate();

    @Autowired
    protected CeasControlat ceas;
    @Autowired
    protected TipAngajatRepository tipAngajatRepository;
    @Autowired
    protected AngajatRepository angajatRepository;
    @Autowired
    protected AngajatFactory angajatFactory;
    @Autowired
    protected ClientRepository clientRepository;
    @Autowired
    protected ClientFactory clientFactory;
    @Autowired
    protected TipAbonamentRepository tipAbonamentRepository;
    @Autowired
    protected ProdusRepository produsRepository;

    protected Angajat manager;
    protected Angajat receptioner;
    protected Angajat antrenor;
    protected Angajat tehnician;
    protected Angajat femeieDeServiciu;

    @BeforeEach
    void pregatesteAngajati() {
        ceas.seteaza(ACUM);
        manager = angajat(RolAngajat.MANAGER, "manager@sala.ro");
        receptioner = angajat(RolAngajat.RECEPTIONER, "receptie@sala.ro");
        antrenor = angajat(RolAngajat.ANTRENOR, "antrenor@sala.ro");
        tehnician = angajat(RolAngajat.TEHNIC, "tehnic@sala.ro");
        femeieDeServiciu = angajat(RolAngajat.FEMEIE_DE_SERVICIU, "curatenie@sala.ro");
    }

    protected Angajat angajat(RolAngajat rol, String email) {
        TipAngajat tip = tipAngajatRepository.findByDenumireIgnoreCase(rol.getDenumire())
                .orElseGet(() -> tipAngajatRepository.save(new TipAngajat(rol.getDenumire(), null)));
        return angajatRepository.save(angajatFactory.creeazaAngajat(new DateAngajatNou(
                "Angajat", rol.getDenumire(), email, "parola123", "0722000111", tip.getIdTipAngajat(),
                rol.getDenumire(), new BigDecimal("4000.00"), AZI.minusYears(1), "L-V 08-16")));
    }

    protected Client client(String email) {
        return clientRepository.save(clientFactory.creeazaClient(new DateClientNou(
                "Popescu", "Ana", email, "parola123", "0722123456", "yoga, cardio", "Ion Popescu 0733000111")));
    }

    protected TipAbonament tipAbonament(String denumire, int durataZile, String pret) {
        return tipAbonamentRepository.save(new TipAbonament(denumire, durataZile, Bani.of(pret)));
    }

    protected Produs produs(String denumire, String pret, int stoc) {
        return produsRepository.save(new Produs(denumire, "Suplimente", Bani.of(pret), stoc));
    }

    protected static BusinessRuleException assertRegula(String codRegula, Executable operatie) {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, operatie);
        assertEquals(codRegula, ex.getCodRegula(), ex.getMessage());
        return ex;
    }
}
