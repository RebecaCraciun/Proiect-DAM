package ro.dam.salafitness.service.workflow.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.dam.salafitness.domain.exception.BusinessRuleException;
import ro.dam.salafitness.domain.model.entity.Utilizator;
import ro.dam.salafitness.domain.model.vo.Email;
import ro.dam.salafitness.domain.repository.UtilizatorRepository;
import ro.dam.salafitness.service.workflow.AutentificareWorkflowService;

@Service
@Transactional(readOnly = true)
public class AutentificareWorkflowServiceImpl implements AutentificareWorkflowService {

    private final UtilizatorRepository utilizatorRepository;
    private final PasswordEncoder passwordEncoder;

    public AutentificareWorkflowServiceImpl(UtilizatorRepository utilizatorRepository,
                                            PasswordEncoder passwordEncoder) {
        this.utilizatorRepository = utilizatorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Utilizator autentifica(String email, String parola) {
        if (email == null || parola == null) {
            throw new BusinessRuleException("UC02", "Emailul si parola sunt obligatorii");
        }
        return utilizatorRepository.findByEmailAdresa(Email.of(email).getAdresa())
                .filter(u -> passwordEncoder.matches(parola, u.getParola()))
                .orElseThrow(() -> new BusinessRuleException("UC02", "Email sau parola incorecte"));
    }
}
