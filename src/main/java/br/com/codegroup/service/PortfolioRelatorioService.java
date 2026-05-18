package br.com.codegroup.service;

import br.com.codegroup.dto.response.PortfolioRelatorioResponse;
import br.com.codegroup.enums.StatusProjeto;
import br.com.codegroup.model.Projeto;
import br.com.codegroup.model.ProjetoMembro;
import br.com.codegroup.repository.ProjetoMembroRepository;
import br.com.codegroup.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioRelatorioService {

    private final ProjetoRepository projetoRepository;
    private final ProjetoMembroRepository projetoMembroRepository;

    @Transactional(readOnly = true)
    public PortfolioRelatorioResponse gerar() {
        List<Projeto> projetos = projetoRepository.findAll();

        Map<StatusProjeto, Long> quantidadePorStatus = calcularQuantidadePorStatus(projetos);
        Map<StatusProjeto, BigDecimal> totalOrcadoPorStatus = calcularTotalOrcadoPorStatus(projetos);
        Double mediaDuracao = calcularMediaDuracaoEncerrados(projetos);
        Long totalMembrosUnicos = calcularTotalMembrosUnicos();

        return new PortfolioRelatorioResponse(
                quantidadePorStatus,
                totalOrcadoPorStatus,
                mediaDuracao,
                totalMembrosUnicos
        );
    }

    // quantidade de projetos agrupados por status
    private Map<StatusProjeto, Long> calcularQuantidadePorStatus(List<Projeto> projetos) {
        return projetos.stream()
                .collect(Collectors.groupingBy(Projeto::getStatus, Collectors.counting()));
    }

    // soma do orçamento total agrupado por status
    private Map<StatusProjeto, BigDecimal> calcularTotalOrcadoPorStatus(List<Projeto> projetos) {
        return projetos.stream()
                .filter(p -> p.getOrcamentoTotal() != null)
                .collect(Collectors.groupingBy(
                        Projeto::getStatus,
                        Collectors.reducing(BigDecimal.ZERO, Projeto::getOrcamentoTotal, BigDecimal::add)
                ));
    }

    // média de duração em dias dos projetos encerrados (dataInicio → dataRealTermino)
    private Double calcularMediaDuracaoEncerrados(List<Projeto> projetos) {
        return projetos.stream()
                .filter(p -> p.getStatus() == StatusProjeto.ENCERRADO)
                .filter(p -> p.getDataInicio() != null && p.getDataRealTermino() != null)
                .mapToLong(p -> ChronoUnit.DAYS.between(p.getDataInicio(), p.getDataRealTermino()))
                .average()
                .orElse(0.0);
    }

    // total de membros únicos alocados em qualquer projeto
    private Long calcularTotalMembrosUnicos() {
        return projetoMembroRepository.findAll()
                .stream()
                .map(ProjetoMembro::getMembroId)
                .distinct()
                .count();
    }
}
