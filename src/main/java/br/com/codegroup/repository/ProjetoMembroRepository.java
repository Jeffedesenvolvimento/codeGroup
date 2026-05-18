package br.com.codegroup.repository;

import br.com.codegroup.enums.StatusProjeto;
import br.com.codegroup.model.ProjetoMembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetoMembroRepository extends JpaRepository<ProjetoMembro, Long> {

    List<ProjetoMembro> findByProjetoId(Long projetoId);

    Optional<ProjetoMembro> findByProjetoIdAndMembroId(Long projetoId, Long membroId);

    long countByProjetoId(Long projetoId);

    boolean existsByProjetoIdAndMembroId(Long projetoId, Long membroId);

    @Query("""
        SELECT COUNT(pm) FROM ProjetoMembro pm
        WHERE pm.membroId = :membroId
        AND pm.projeto.status NOT IN :statusExcluidos
    """)
    long countProjetosAtivosByMembroId(
            @Param("membroId") Long membroId,
            @Param("statusExcluidos") List<StatusProjeto> statusExcluidos
    );
}