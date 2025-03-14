package br.com.viniciusfinancas.financas.service;

import br.com.viniciusfinancas.financas.domain.user.Despesa;
import br.com.viniciusfinancas.financas.repositories.DespesaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DespesaService {
    @Autowired
    private DespesaRepository despesaRepository;

    @Autowired
    private UserService userService;

    public DespesaService(DespesaRepository despesaRepository){
        this.despesaRepository = despesaRepository;
    }

    public boolean deleteDespesa(Long id){
        if (despesaRepository.existsById(id)){
            despesaRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
