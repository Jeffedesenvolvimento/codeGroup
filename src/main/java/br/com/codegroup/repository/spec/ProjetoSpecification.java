package br.com.codegroup.repository.spec;

import br.com.codegroup.enums.StatusProjeto;
import br.com.codegroup.model.Projeto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProjetoSpecification {

    private ProjetoSpecification() {}

    public static Specification<Projeto> filtrar(StatusProjeto status, String nome) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (nome != null && !nome.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}