package ro.dam.salafitness.service.validation.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.exception.EntitateNegasitaException;
import ro.dam.salafitness.domain.model.entity.Angajat;
import ro.dam.salafitness.domain.model.entity.Client;
import ro.dam.salafitness.domain.model.entity.ProgramClasa;
import ro.dam.salafitness.domain.model.entity.SedintaPT;
import ro.dam.salafitness.domain.model.enums.RolAngajat;
import ro.dam.salafitness.domain.model.enums.StatusSedinta;
import ro.dam.salafitness.domain.model.vo.Email;
import ro.dam.salafitness.domain.model.vo.IntervalOrar;
import ro.dam.salafitness.domain.repository.AngajatRepository;
import ro.dam.salafitness.domain.repository.ClasaFitnessRepository;
import ro.dam.salafitness.domain.repository.ClientRepository;
import ro.dam.salafitness.domain.repository.SedintaPTRepository;
import ro.dam.salafitness.domain.repository.TipAbonamentRepository;
import ro.dam.salafitness.domain.repository.TipAngajatRepository;
import ro.dam.salafitness.domain.repository.UtilizatorRepository;
import ro.dam.salafitness.service.validation.DomainValidationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DomainValidationServiceImpl implements DomainValidationService {

    private final Validator validator;
    private final UtilizatorRepository utilizatorRepository;
    private final ClientRepository clientRepository;
    private final AngajatRepository angajatRepository;
    private final TipAngajatRepository tipAngajatRepository;
    private final TipAbonamentRepository tipAbonamentRepository;
    private final ClasaFitnessRepository clasaFitnessRepository;
    private final SedintaPTRepository sedintaPTRepository;

    public DomainValidationServiceImpl(Validator validator, UtilizatorRepository utilizatorRepository,
                                       ClientRepository clientRepository, AngajatRepository angajatRepository,
                                       TipAngajatRepository tipAngajatRepository,
                                       TipAbonamentRepository tipAbonamentRepository,
                                       ClasaFitnessRepository clasaFitnessRepository,
                                       SedintaPTRepository sedintaPTRepository) {
        this.validator = validator;
        this.utilizatorRepository = utilizatorRepository;
        this.clientRepository = clientRepository;
        this.angajatRepository = angajatRepository;
        this.tipAngajatRepository = tipAngajatRepository;
        this.tipAbonamentRepository = tipAbonamentRepository;
        this.clasaFitnessRepository = clasaFitnessRepository;
        this.sedintaPTRepository = sedintaPTRepository;
    }

    @Override
    public <T> Set<ConstraintViolation<T>> verifica(T entitate) {
        return validator.validate(entitate);
    }

    @Override
    public <T> T valideaza(T entitate) {
        Set<ConstraintViolation<T>> violari = validator.validate(entitate);
        if (!violari.isEmpty()) {
            throw new ConstraintViolationException(violari);
        }
        return entitate;
    }

    @Override
    public void verificaEmailUnic(String email) {
        String adresa = Email.of(email).getAdresa();
        if (adresa != null && utilizatorRepository.existsByEmailAdresa(adresa)) {
            throw new BusinessRuleException("RV01", "Emailul " + adresa + " este deja folosit");
        }
    }

    @Override
    public void verificaDenumireTipAbonamentUnica(String denumire) {
        if (tipAbonamentRepository.existsByDenumireIgnoreCase(denumire)) {
            throw new BusinessRuleException("RV07", "Tipul de abonament '" + denumire + "' exista deja");
        }
    }

    @Override
    public Angajat verificaRol(Integer idAngajat, RolAngajat rol, String codRegula) {
        return verificaUnulDinRoluri(idAngajat, codRegula, rol);
    }

    @Override
    public Angajat verificaUnulDinRoluri(Integer idAngajat, String codRegula, RolAngajat... roluri) {
        Angajat angajat = angajatRepository.findById(idAngajat)
                .orElseThrow(() -> new BusinessRuleException(codRegula, "Utilizatorul " + idAngajat + " nu este angajat"));
        boolean areRol = tipAngajatRepository.findById(angajat.getIdTipAngajat())
                .map(tip -> Arrays.stream(roluri).anyMatch(tip::esteRol))
                .orElse(false);
        if (!areRol) {
            String denumiri = Arrays.stream(roluri).map(RolAngajat::getDenumire).collect(Collectors.joining(" / "));
            throw new BusinessRuleException(codRegula,
                    "Angajatul " + angajat.getNumeComplet() + " nu are rolul " + denumiri);
        }
        return angajat;
    }

    @Override
    public Angajat verificaAntrenor(Integer idAngajat) {
        return verificaRol(idAngajat, RolAngajat.ANTRENOR, "RA09");
    }

    @Override
    public Angajat verificaTehnician(Integer idAngajat) {
        return verificaRol(idAngajat, RolAngajat.TEHNIC, "RA10");
    }

    @Override
    public Angajat verificaReceptioner(Integer idAngajat) {
        return verificaRol(idAngajat, RolAngajat.RECEPTIONER, "RA11");
    }

    @Override
    public boolean areAbonamentValid(Integer idClient, LocalDate data) {
        return clientRepository.findById(idClient).map(c -> c.poateIntra(data)).orElse(false);
    }

    @Override
    public Client verificaAbonamentValid(Integer idClient, LocalDate data, String codRegula) {
        Client client = clientRepository.findById(idClient)
                .orElseThrow(() -> new EntitateNegasitaException("Client", idClient));
        if (!client.poateIntra(data)) {
            throw new BusinessRuleException(codRegula, "Clientul nu are un abonament valid la data " + data);
        }
        return client;
    }

    @Override
    public void verificaSalaLibera(String salaStudio, IntervalOrar interval, Long idProgramExclus) {
        clasaFitnessRepository.findSesiuniSuprapuse(salaStudio, interval.getInceput(), interval.getSfarsit()).stream()
                .filter(p -> !p.getIdProgram().equals(idProgramExclus))
                .findAny()
                .ifPresent((ProgramClasa p) -> {
                    throw new BusinessRuleException("RA12", "Sala " + salaStudio + " este ocupata de clasa "
                            + p.getClasa().getDenumire() + " in intervalul " + p.getInterval());
                });
    }

    @Override
    public void verificaDisponibilitateAntrenor(Integer idAntrenor, LocalDateTime dataOra) {
        LocalDateTime de = dataOra.minusMinutes(SedintaPT.DURATA_MINUTE);
        LocalDateTime pana = dataOra.plusMinutes(SedintaPT.DURATA_MINUTE);
        if (sedintaPTRepository.existaSedintaActivaIntre(idAntrenor, de, pana, StatusSedinta.ANULAT)) {
            throw new BusinessRuleException("RA13", "Antrenorul are deja o sedinta PT programata la " + dataOra);
        }
    }

    @Override
    public void verificaProfilUnic(Integer idUtilizator) {
        if (clientRepository.existsById(idUtilizator) && angajatRepository.existsById(idUtilizator)) {
            throw new BusinessRuleException("RB08", "Utilizatorul " + idUtilizator + " are atat profil de client, cat si de angajat");
        }
    }
}