package br.com.viniciusfinancas.financas.service;

import br.com.viniciusfinancas.financas.domain.user.Receita;
import br.com.viniciusfinancas.financas.domain.user.User;
import br.com.viniciusfinancas.financas.repositories.ReceitaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.lang.model.element.RecordComponentElement;
import java.util.List;
import java.util.Optional;

@Service
public class ReceitaService {
    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private UserService userService;

    public ReceitaService(ReceitaRepository receitaRepository){
        this.receitaRepository = receitaRepository;
    }

    public boolean deleteReceita(Long id) {
        if (receitaRepository.existsById(id)) {
            receitaRepository.deleteById(id);
            return true;
        }
        return false;
    }


}
