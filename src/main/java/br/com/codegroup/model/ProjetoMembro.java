package br.com.codegroup.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "projeto_membro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjetoMembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id", nullable = false)
    private Projeto projeto;

    @Column(name = "membro_id", nullable = false)
    private Long membroId;
}