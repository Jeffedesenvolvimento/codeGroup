package br.com.codegroup.repository;

import br.com.codegroup.enums.StatusProjeto;
import br.com.codegroup.model.Projeto;
import br.com.codegroup.model.ProjetoMembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProjetoRepository extends JpaRepository<Projeto, Long>,
        JpaSpecificationExecutor<Projeto> {
}