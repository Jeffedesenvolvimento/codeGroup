package br.com.codegroup.model;

import br.com.codegroup.enums.ClassificacaoRisco;
import br.com.codegroup.enums.StatusProjeto;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projetos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projeto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private LocalDate dataInicio;
    private LocalDate previsaoTermino;
    private LocalDate dataRealTermino;
    private BigDecimal orcamentoTotal;
    private String descricao;

    @Column(name = "gerente_membro_id")
    private Long gerenteMembroId;

    @Enumerated(EnumType.STRING)
    private StatusProjeto status;

    @Enumerated(EnumType.STRING)
    private ClassificacaoRisco risco;

    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjetoMembro> membros = new ArrayList<>();
}