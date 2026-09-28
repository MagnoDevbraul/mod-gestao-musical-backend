package br.com.mod.gestaomusical.service;

import br.com.mod.gestaomusical.dto.DashboardAlteracaoPendenteResponseDTO;
import br.com.mod.gestaomusical.dto.DashboardAlunosPorComumResponseDTO;
import br.com.mod.gestaomusical.dto.DashboardAtividadeResponseDTO;
import br.com.mod.gestaomusical.dto.DashboardNotificacaoResponseDTO;
import br.com.mod.gestaomusical.dto.DashboardResponseDTO;
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

        dto.setTotalAlunos(
                alunoRepository.count()
        );

        dto.setAlunosAtivos(
                alunoRepository
                        .countBySituacaoIgnoreCase(
                                "ATIVO"
                        )
        );

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

        List<DashboardAtividadeResponseDTO>
                atividades =
                historicoRepository
                        .findTop5ByOrderByDataHoraDesc()
                        .stream()
                        .map(this::converterAtividade)
                        .toList();

        dto.setAtividadesRecentes(
                atividades
        );

        List<DashboardAlunosPorComumResponseDTO>
                alunosPorComum =
                alunoRepository
                        .contarAlunosPorComum()
                        .stream()
                        .map(item ->
                                new DashboardAlunosPorComumResponseDTO(
                                        item.getComum(),
                                        item.getQuantidade()
                                )
                        )
                        .toList();

        dto.setAlunosPorComum(
                alunosPorComum
        );

        List<DashboardNotificacaoResponseDTO>
                notificacoesRecentes =
                notificacaoRepository
                        .findTop5ByOrderByDataHoraDesc()
                        .stream()
                        .map(this::converterNotificacao)
                        .toList();

        dto.setNotificacoesRecentes(
                notificacoesRecentes
        );

        List<DashboardAlteracaoPendenteResponseDTO>
                alteracoesRestritasPendentes =
                solicitacaoAlteracaoAlunoRepository
                        .findByStatusOrderByCriadoEmDesc(
                                StatusSolicitacaoAlteracaoAluno.PENDENTE
                        )
                        .stream()
                        .limit(5)
                        .map(this::converterAlteracaoPendente)
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

        if (solicitacao.getComumId() != null) {
            return "Comum";
        }

        if (solicitacao.getNivelId() != null) {
            return "Nível";
        }

        if (solicitacao.getCargoMinisterioId() != null) {
            return "Cargo/Ministério";
        }

        if (solicitacao.getDataBatismo() != null) {
            return "Data de Batismo";
        }

        if (solicitacao.getDataInicioGem() != null) {
            return "Data de Início GEM";
        }

        return "Alteração restrita";
    }
}