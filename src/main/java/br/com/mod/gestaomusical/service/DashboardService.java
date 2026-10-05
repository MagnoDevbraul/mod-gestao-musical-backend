package br.com.mod.gestaomusical.service;

import br.com.mod.gestaomusical.dto.DashboardAlteracaoPendenteResponseDTO;
import br.com.mod.gestaomusical.dto.DashboardAlunosPorComumResponseDTO;
import br.com.mod.gestaomusical.dto.DashboardAtividadeResponseDTO;
import br.com.mod.gestaomusical.dto.DashboardNotificacaoResponseDTO;
import br.com.mod.gestaomusical.dto.DashboardResponseDTO;
import br.com.mod.gestaomusical.entity.Aluno;
import br.com.mod.gestaomusical.entity.Historico;
import br.com.mod.gestaomusical.entity.Notificacao;
import br.com.mod.gestaomusical.entity.SolicitacaoAlteracaoAluno;
import br.com.mod.gestaomusical.entity.StatusSolicitacaoAlteracaoAluno;
import br.com.mod.gestaomusical.repository.AlunoRepository;
import br.com.mod.gestaomusical.repository.HistoricoRepository;
import br.com.mod.gestaomusical.repository.NotificacaoRepository;
import br.com.mod.gestaomusical.repository.SolicitacaoAlteracaoAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final AlunoRepository alunoRepository;

    private final SolicitacaoAlteracaoAlunoRepository
            solicitacaoAlteracaoAlunoRepository;

    private final NotificacaoRepository notificacaoRepository;

    private final HistoricoRepository historicoRepository;


    public DashboardService(
            AlunoRepository alunoRepository,
            SolicitacaoAlteracaoAlunoRepository
                    solicitacaoAlteracaoAlunoRepository,
            NotificacaoRepository notificacaoRepository,
            HistoricoRepository historicoRepository) {

        this.alunoRepository =
                alunoRepository;

        this.solicitacaoAlteracaoAlunoRepository =
                solicitacaoAlteracaoAlunoRepository;

        this.notificacaoRepository =
                notificacaoRepository;

        this.historicoRepository =
                historicoRepository;
    }


    @Transactional(readOnly = true)
    public DashboardResponseDTO obterDashboardSecretaria() {

        DashboardResponseDTO dto =
                new DashboardResponseDTO();


        /*
         * Total geral:
         * ativos + arquivados.
         */
        dto.setTotalAlunos(
                alunoRepository.count()
        );


        /*
         * Total de alunos ativos.
         */
        dto.setAlunosAtivos(
                alunoRepository
                        .countBySituacaoIgnoreCase(
                                "ATIVO"
                        )
        );


        /*
         * Total de alunos arquivados.
         */
        dto.setAlunosArquivados(
                alunoRepository
                        .countBySituacaoIgnoreCase(
                                "ARQUIVADO"
                        )
        );


        dto.setAlteracoesPendentes(
                solicitacaoAlteracaoAlunoRepository
                        .countByStatus(
                                StatusSolicitacaoAlteracaoAluno.PENDENTE
                        )
        );


        dto.setNotificacoes(
                notificacaoRepository.count()
        );


        /*
         * Atividades recentes.
         */
        List<DashboardAtividadeResponseDTO>
                atividades =
                historicoRepository
                        .findTop5ByOrderByDataHoraDesc()
                        .stream()
                        .map(
                                this::converterAtividade
                        )
                        .toList();

        dto.setAtividadesRecentes(
                atividades
        );


        /*
         * ==================================================
         * ALUNOS ATIVOS POR COMUM
         * ==================================================
         *
         * O gráfico representa somente os alunos ATIVOS.
         *
         * Portanto:
         *
         * ATIVO -> ARQUIVADO
         * a barra da Comum diminui.
         *
         * ARQUIVADO -> ATIVO
         * a barra da Comum aumenta.
         *
         * Os registros arquivados continuam fazendo parte
         * do total geral e do indicador de arquivados,
         * mas não da distribuição de alunos ativos.
         */
        List<Aluno> alunosAtivos =
                alunoRepository
                        .findAll()
                        .stream()
                        .filter(
                                aluno ->
                                        "ATIVO"
                                                .equalsIgnoreCase(
                                                        aluno.getSituacao()
                                                )
                        )
                        .filter(
                                aluno ->
                                        aluno.getComum() != null
                                                && aluno.getComum()
                                                .getNome() != null
                        )
                        .toList();


        Map<String, Long> quantidadePorComum =
                alunosAtivos
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        aluno ->
                                                aluno.getComum()
                                                        .getNome(),
                                        Collectors.counting()
                                )
                        );


        List<DashboardAlunosPorComumResponseDTO>
                alunosPorComum =
                quantidadePorComum
                        .entrySet()
                        .stream()

                        /*
                         * Primeiro maior quantidade.
                         * Em empate, ordena pelo nome.
                         */
                        .sorted(
                                (a, b) -> {

                                    int comparacaoQuantidade =
                                            Long.compare(
                                                    b.getValue(),
                                                    a.getValue()
                                            );

                                    if (
                                            comparacaoQuantidade
                                                    != 0
                                    ) {

                                        return comparacaoQuantidade;
                                    }

                                    return a.getKey()
                                            .compareToIgnoreCase(
                                                    b.getKey()
                                            );
                                }
                        )
                        .map(
                                item ->
                                        new DashboardAlunosPorComumResponseDTO(
                                                item.getKey(),
                                                item.getValue()
                                        )
                        )
                        .toList();


        dto.setAlunosPorComum(
                alunosPorComum
        );


        /*
         * Notificações recentes.
         */
        List<DashboardNotificacaoResponseDTO>
                notificacoesRecentes =
                notificacaoRepository
                        .findTop5ByOrderByDataHoraDesc()
                        .stream()
                        .map(
                                this::converterNotificacao
                        )
                        .toList();

        dto.setNotificacoesRecentes(
                notificacoesRecentes
        );


        /*
         * Alterações restritas pendentes.
         */
        List<DashboardAlteracaoPendenteResponseDTO>
                alteracoesRestritasPendentes =
                solicitacaoAlteracaoAlunoRepository
                        .findByStatusOrderByCriadoEmDesc(
                                StatusSolicitacaoAlteracaoAluno.PENDENTE
                        )
                        .stream()
                        .limit(5)
                        .map(
                                this::converterAlteracaoPendente
                        )
                        .toList();

        dto.setAlteracoesRestritasPendentes(
                alteracoesRestritasPendentes
        );


        return dto;
    }


    private DashboardAtividadeResponseDTO
    converterAtividade(
            Historico historico) {

        DashboardAtividadeResponseDTO dto =
                new DashboardAtividadeResponseDTO();

        dto.setId(
                historico.getId()
        );

        dto.setTipoEvento(
                historico.getTipoEvento()
        );

        dto.setDescricao(
                historico.getDescricao()
        );

        dto.setDataHora(
                historico.getDataHora()
        );

        return dto;
    }


    private DashboardNotificacaoResponseDTO
    converterNotificacao(
            Notificacao notificacao) {

        DashboardNotificacaoResponseDTO dto =
                new DashboardNotificacaoResponseDTO();

        dto.setId(
                notificacao.getId()
        );

        dto.setTipoEvento(
                notificacao.getTipoEvento()
        );

        dto.setTitulo(
                notificacao.getTitulo()
        );

        dto.setMensagem(
                notificacao.getMensagem()
        );

        dto.setDataHora(
                notificacao.getDataHora()
        );

        dto.setLida(
                notificacao.getLida()
        );

        return dto;
    }


    private DashboardAlteracaoPendenteResponseDTO
    converterAlteracaoPendente(
            SolicitacaoAlteracaoAluno solicitacao) {

        DashboardAlteracaoPendenteResponseDTO dto =
                new DashboardAlteracaoPendenteResponseDTO();

        dto.setId(
                solicitacao.getId()
        );

        dto.setAluno(
                solicitacao
                        .getAluno()
                        .getNome()
        );

        dto.setCampo(
                identificarCampoAlterado(
                        solicitacao
                )
        );

        dto.setSolicitante(
                solicitacao
                        .getSolicitante()
                        .getNome()
        );

        dto.setStatus(
                solicitacao
                        .getStatus()
                        .name()
        );

        dto.setCriadoEm(
                solicitacao.getCriadoEm()
        );

        return dto;
    }


    private String identificarCampoAlterado(
            SolicitacaoAlteracaoAluno solicitacao) {

        if (
                solicitacao.getComumId()
                        != null
        ) {

            return "Comum";
        }


        if (
                solicitacao.getNivelId()
                        != null
        ) {

            return "Nível";
        }


        if (
                solicitacao
                        .getCargoMinisterioId()
                        != null
        ) {

            return "Cargo/Ministério";
        }


        if (
                solicitacao.getDataBatismo()
                        != null
        ) {

            return "Data de Batismo";
        }


        if (
                solicitacao.getDataInicioGem()
                        != null
        ) {

            return "Data de Início GEM";
        }


        return "Alteração restrita";
    }
}