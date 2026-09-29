package br.com.mod.gestaomusical.service;

import br.com.mod.gestaomusical.dto.DecisaoAlteracaoAlunoRequestDTO;
import br.com.mod.gestaomusical.dto.SolicitacaoAlteracaoAlunoRequestDTO;
import br.com.mod.gestaomusical.dto.SolicitacaoAlteracaoAlunoResponseDTO;
import br.com.mod.gestaomusical.entity.*;
import br.com.mod.gestaomusical.repository.*;
import br.com.mod.gestaomusical.security.UsuarioAutenticadoService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SolicitacaoAlteracaoAlunoService {

    private static final String PERMISSAO_APROVAR =
            "ALTERACAO_RESTRITA_APROVAR";

    private final SolicitacaoAlteracaoAlunoRepository solicitacaoRepository;
    private final AlunoRepository alunoRepository;
    private final ComumCongregacaoRepository comumRepository;
    private final NivelRepository nivelRepository;
    private final CargoMinisterioRepository cargoMinisterioRepository;
    private final HistoricoRepository historicoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final AuditoriaService auditoriaService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public SolicitacaoAlteracaoAlunoService(
            SolicitacaoAlteracaoAlunoRepository solicitacaoRepository,
            AlunoRepository alunoRepository,
            ComumCongregacaoRepository comumRepository,
            NivelRepository nivelRepository,
            CargoMinisterioRepository cargoMinisterioRepository,
            HistoricoRepository historicoRepository,
            NotificacaoRepository notificacaoRepository,
            AuditoriaService auditoriaService,
            UsuarioAutenticadoService usuarioAutenticadoService) {

        this.solicitacaoRepository = solicitacaoRepository;
        this.alunoRepository = alunoRepository;
        this.comumRepository = comumRepository;
        this.nivelRepository = nivelRepository;
        this.cargoMinisterioRepository = cargoMinisterioRepository;
        this.historicoRepository = historicoRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.auditoriaService = auditoriaService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAlteracaoAlunoResponseDTO> listarTodos() {

        return solicitacaoRepository
                .findAllByOrderByCriadoEmDesc()
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoAlteracaoAlunoResponseDTO> listarPendentes() {

        return solicitacaoRepository
                .findByStatusOrderByCriadoEmDesc(
                        StatusSolicitacaoAlteracaoAluno.PENDENTE
                )
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<SolicitacaoAlteracaoAlunoResponseDTO> buscarPorId(
            Long id) {

        return solicitacaoRepository
                .findById(id)
                .map(this::converterParaDTO);
    }

    /*
     * Regra:
     *
     * SECRETARIA:
     * aplica a alteração restrita diretamente.
     * Não cria solicitação pendente.
     *
     * DEMAIS USUÁRIOS:
     * cria solicitação PENDENTE para decisão da Secretaria.
     */
    @Transactional
    public SolicitacaoAlteracaoAlunoResponseDTO solicitar(
            Long alunoId,
            SolicitacaoAlteracaoAlunoRequestDTO dto) {

        Aluno aluno = alunoRepository
                .findById(alunoId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Aluno não encontrado"
                        )
                );

        if ("ARQUIVADO".equalsIgnoreCase(
                aluno.getSituacao())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Aluno arquivado não pode ser alterado"
            );
        }

        if (dto.getMotivo() == null
                || dto.getMotivo().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Motivo da alteração restrita é obrigatório"
            );
        }

        if (dto.getComumId() == null
                && dto.getNivelId() == null
                && dto.getCargoMinisterioId() == null
                && dto.getDataBatismo() == null
                && dto.getDataInicioGem() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe pelo menos um campo restrito para alteração"
            );
        }

        validarReferencias(dto);

        Usuario usuario =
                usuarioAutenticadoService
                        .obterUsuarioAutenticado();

        /*
         * SECRETARIA:
         *
         * ALTERACAO_RESTRITA_APROVAR deve pertencer
         * exclusivamente ao perfil SECRETARIA.
         *
         * Se o usuário possui essa permissão, a alteração
         * é aplicada diretamente e NÃO cria pendência.
         */
        if (possuiPermissao(PERMISSAO_APROVAR)) {

            return aplicarAlteracaoDiretaSecretaria(
                    aluno,
                    usuario,
                    dto
            );
        }

        /*
         * Para os demais usuários não permitimos criar
         * uma segunda solicitação pendente para o mesmo aluno.
         */
        if (solicitacaoRepository
                .existsByAluno_IdAndStatus(
                        alunoId,
                        StatusSolicitacaoAlteracaoAluno.PENDENTE
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma solicitação de alteração restrita pendente para este aluno"
            );
        }

        SolicitacaoAlteracaoAluno solicitacao =
                new SolicitacaoAlteracaoAluno();

        solicitacao.setAluno(aluno);
        solicitacao.setSolicitante(usuario);

        solicitacao.setStatus(
                StatusSolicitacaoAlteracaoAluno.PENDENTE
        );

        solicitacao.setComumId(
                dto.getComumId()
        );

        solicitacao.setNivelId(
                dto.getNivelId()
        );

        solicitacao.setCargoMinisterioId(
                dto.getCargoMinisterioId()
        );

        solicitacao.setDataBatismo(
                dto.getDataBatismo()
        );

        solicitacao.setDataInicioGem(
                dto.getDataInicioGem()
        );

        solicitacao.setMotivo(
                dto.getMotivo().trim()
        );

        SolicitacaoAlteracaoAluno salva =
                solicitacaoRepository.save(solicitacao);

        Map<String, Object> dadosAtuais =
                criarSnapshotRestrito(aluno);

        Map<String, Object> dadosPropostos =
                criarSnapshotProposto(salva);

        /*
         * Histórico da solicitação.
         */
        Historico historico =
                new Historico();

        historico.setAluno(aluno);
        historico.setUsuario(usuario);

        historico.setTipoEvento(
                "SOLICITACAO_ALTERACAO_RESTRITA_ALUNO"
        );

        historico.setDescricao(
                "Solicitação de alteração restrita criada no MOD."
        );

        historico.setValorAnterior(
                dadosAtuais.toString()
        );

        historico.setValorNovo(
                dadosPropostos.toString()
        );

        historicoRepository.save(historico);

        /*
         * Notificação.
         */
        Notificacao notificacao =
                new Notificacao();

        notificacao.setUsuario(usuario);
        notificacao.setAluno(aluno);

        notificacao.setTipoEvento(
                "SOLICITACAO_ALTERACAO_RESTRITA_ALUNO"
        );

        notificacao.setTitulo(
                "Alteração restrita pendente"
        );

        notificacao.setMensagem(
                "Foi criada uma solicitação de alteração restrita para o aluno "
                        + aluno.getNome()
                        + "."
        );

        notificacao.setLida(false);
        notificacao.setDataLeitura(null);

        notificacaoRepository.save(notificacao);

        /*
         * Auditoria.
         */
        auditoriaService.registrar(
                "SOLICITACAO_ALTERACAO_RESTRITA_ALUNO",
                "solicitacao_alteracao_aluno",
                salva.getId(),
                "Solicitação de alteração restrita criada no MOD.",
                dadosAtuais,
                dadosPropostos
        );

        return converterParaDTO(salva);
    }

    /*
     * Alteração direta realizada pela Secretaria.
     *
     * Não gera solicitação pendente.
     * A alteração é aplicada imediatamente.
     *
     * Mesmo assim:
     * - registra Histórico;
     * - registra Notificação;
     * - registra Auditoria.
     */
    private SolicitacaoAlteracaoAlunoResponseDTO aplicarAlteracaoDiretaSecretaria(
            Aluno aluno,
            Usuario secretaria,
            SolicitacaoAlteracaoAlunoRequestDTO dto) {

        Map<String, Object> dadosAnteriores =
                criarSnapshotRestrito(aluno);

        aplicarAlteracoesDiretas(
                aluno,
                dto
        );

        Aluno alunoSalvo =
                alunoRepository.save(aluno);

        Map<String, Object> dadosNovos =
                criarSnapshotRestrito(alunoSalvo);

        /*
         * Histórico.
         */
        Historico historico =
                new Historico();

        historico.setAluno(alunoSalvo);
        historico.setUsuario(secretaria);

        historico.setTipoEvento(
                "ALTERACAO_RESTRITA_ALUNO_DIRETA_SECRETARIA"
        );

        historico.setDescricao(
                "Alteração restrita realizada diretamente pela Secretaria no MOD."
        );

        historico.setValorAnterior(
                dadosAnteriores.toString()
        );

        historico.setValorNovo(
                dadosNovos.toString()
        );

        historicoRepository.save(historico);

        /*
         * Notificação.
         */
        Notificacao notificacao =
                new Notificacao();

        notificacao.setUsuario(secretaria);
        notificacao.setAluno(alunoSalvo);

        notificacao.setTipoEvento(
                "ALTERACAO_RESTRITA_ALUNO_DIRETA_SECRETARIA"
        );

        notificacao.setTitulo(
                "Alteração restrita realizada"
        );

        notificacao.setMensagem(
                "A Secretaria realizou uma alteração restrita diretamente no aluno "
                        + alunoSalvo.getNome()
                        + "."
        );

        notificacao.setLida(false);
        notificacao.setDataLeitura(null);

        notificacaoRepository.save(notificacao);

        /*
         * Auditoria.
         */
        auditoriaService.registrar(
                "ALTERACAO_RESTRITA_ALUNO_DIRETA_SECRETARIA",
                "aluno",
                alunoSalvo.getId(),
                "Alteração restrita realizada diretamente pela Secretaria no MOD. Motivo: "
                        + dto.getMotivo().trim(),
                dadosAnteriores,
                dadosNovos
        );

        /*
         * Não existe solicitação persistida neste fluxo.
         *
         * O DTO apenas informa ao cliente que a alteração
         * foi aplicada diretamente.
         */
        SolicitacaoAlteracaoAlunoResponseDTO resposta =
                new SolicitacaoAlteracaoAlunoResponseDTO();

        resposta.setId(null);

        resposta.setAlunoId(
                alunoSalvo.getId()
        );

        resposta.setAlunoNome(
                alunoSalvo.getNome()
        );

        resposta.setSolicitanteId(
                secretaria.getId()
        );

        resposta.setSolicitanteNome(
                secretaria.getNome()
        );

        resposta.setAprovadorId(null);
        resposta.setAprovadorNome(null);

        resposta.setStatus(
                "APLICADA_DIRETAMENTE"
        );

        resposta.setComumId(
                dto.getComumId()
        );

        resposta.setNivelId(
                dto.getNivelId()
        );

        resposta.setCargoMinisterioId(
                dto.getCargoMinisterioId()
        );

        resposta.setDataBatismo(
                dto.getDataBatismo()
        );

        resposta.setDataInicioGem(
                dto.getDataInicioGem()
        );

        resposta.setMotivo(
                dto.getMotivo().trim()
        );

        resposta.setObservacaoDecisao(null);

        resposta.setCriadoEm(
                LocalDateTime.now()
        );

        resposta.setDecididoEm(
                LocalDateTime.now()
        );

        return resposta;
    }

    /*
     * Aprova uma solicitação.
     *
     * Somente a Secretaria pode executar esta operação.
     */
    @Transactional
    public SolicitacaoAlteracaoAlunoResponseDTO aprovar(
            Long solicitacaoId,
            DecisaoAlteracaoAlunoRequestDTO dto) {

        validarPermissaoDecisao();

        SolicitacaoAlteracaoAluno solicitacao =
                buscarSolicitacaoPendente(
                        solicitacaoId
                );

        Usuario aprovador =
                usuarioAutenticadoService
                        .obterUsuarioAutenticado();

        validarDecisorDiferenteDoSolicitante(
                solicitacao,
                aprovador
        );

        Aluno aluno =
                solicitacao.getAluno();

        if ("ARQUIVADO".equalsIgnoreCase(
                aluno.getSituacao())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Aluno arquivado não pode receber alteração"
            );
        }

        Map<String, Object> dadosAnteriores =
                criarSnapshotRestrito(aluno);

        aplicarAlteracoes(
                aluno,
                solicitacao
        );

        Aluno alunoSalvo =
                alunoRepository.save(aluno);

        solicitacao.setStatus(
                StatusSolicitacaoAlteracaoAluno.APROVADA
        );

        solicitacao.setAprovador(
                aprovador
        );

        solicitacao.setObservacaoDecisao(
                normalizarObservacao(dto)
        );

        solicitacao.setDecididoEm(
                LocalDateTime.now()
        );

        SolicitacaoAlteracaoAluno salva =
                solicitacaoRepository.save(solicitacao);

        Map<String, Object> dadosNovos =
                criarSnapshotRestrito(alunoSalvo);

        /*
         * Histórico.
         */
        Historico historico =
                new Historico();

        historico.setAluno(alunoSalvo);
        historico.setUsuario(aprovador);

        historico.setTipoEvento(
                "ALTERACAO_RESTRITA_ALUNO_APROVADA"
        );

        historico.setDescricao(
                "Alteração restrita do aluno aprovada e aplicada no MOD."
        );

        historico.setValorAnterior(
                dadosAnteriores.toString()
        );

        historico.setValorNovo(
                dadosNovos.toString()
        );

        historicoRepository.save(historico);

        /*
         * Notificação.
         */
        Notificacao notificacao =
                new Notificacao();

        notificacao.setUsuario(aprovador);
        notificacao.setAluno(alunoSalvo);

        notificacao.setTipoEvento(
                "ALTERACAO_RESTRITA_ALUNO_APROVADA"
        );

        notificacao.setTitulo(
                "Alteração restrita aprovada"
        );

        notificacao.setMensagem(
                "A alteração restrita do aluno "
                        + alunoSalvo.getNome()
                        + " foi aprovada e aplicada no MOD."
        );

        notificacao.setLida(false);
        notificacao.setDataLeitura(null);

        notificacaoRepository.save(notificacao);

        auditoriaService.registrar(
                "ALTERACAO_RESTRITA_ALUNO_APROVADA",
                "aluno",
                alunoSalvo.getId(),
                "Alteração restrita do aluno aprovada e aplicada no MOD.",
                dadosAnteriores,
                dadosNovos
        );

        return converterParaDTO(salva);
    }

    /*
     * Rejeita uma solicitação.
     *
     * Somente a Secretaria pode executar esta operação.
     */
    @Transactional
    public SolicitacaoAlteracaoAlunoResponseDTO rejeitar(
            Long solicitacaoId,
            DecisaoAlteracaoAlunoRequestDTO dto) {

        validarPermissaoDecisao();

        SolicitacaoAlteracaoAluno solicitacao =
                buscarSolicitacaoPendente(
                        solicitacaoId
                );

        if (dto == null
                || dto.getObservacao() == null
                || dto.getObservacao().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Observação da rejeição é obrigatória"
            );
        }

        Usuario decisor =
                usuarioAutenticadoService
                        .obterUsuarioAutenticado();

        validarDecisorDiferenteDoSolicitante(
                solicitacao,
                decisor
        );

        solicitacao.setStatus(
                StatusSolicitacaoAlteracaoAluno.REJEITADA
        );

        /*
         * O campo aprovador_id representa o usuário
         * responsável pela decisão, inclusive quando rejeita.
         */
        solicitacao.setAprovador(
                decisor
        );

        solicitacao.setObservacaoDecisao(
                dto.getObservacao().trim()
        );

        solicitacao.setDecididoEm(
                LocalDateTime.now()
        );

        SolicitacaoAlteracaoAluno salva =
                solicitacaoRepository.save(solicitacao);

        Aluno aluno =
                solicitacao.getAluno();

        Historico historico =
                new Historico();

        historico.setAluno(aluno);
        historico.setUsuario(decisor);

        historico.setTipoEvento(
                "ALTERACAO_RESTRITA_ALUNO_REJEITADA"
        );

        historico.setDescricao(
                "Solicitação de alteração restrita rejeitada no MOD."
        );

        historico.setValorAnterior(
                "PENDENTE"
        );

        historico.setValorNovo(
                "REJEITADA"
        );

        historicoRepository.save(historico);

        Notificacao notificacao =
                new Notificacao();

        notificacao.setUsuario(decisor);
        notificacao.setAluno(aluno);

        notificacao.setTipoEvento(
                "ALTERACAO_RESTRITA_ALUNO_REJEITADA"
        );

        notificacao.setTitulo(
                "Alteração restrita rejeitada"
        );

        notificacao.setMensagem(
                "A solicitação de alteração restrita do aluno "
                        + aluno.getNome()
                        + " foi rejeitada."
        );

        notificacao.setLida(false);
        notificacao.setDataLeitura(null);

        notificacaoRepository.save(notificacao);

        Map<String, Object> anterior =
                new LinkedHashMap<>();

        anterior.put(
                "status",
                "PENDENTE"
        );

        Map<String, Object> novo =
                new LinkedHashMap<>();

        novo.put(
                "status",
                "REJEITADA"
        );

        novo.put(
                "observacao",
                dto.getObservacao().trim()
        );

        auditoriaService.registrar(
                "ALTERACAO_RESTRITA_ALUNO_REJEITADA",
                "solicitacao_alteracao_aluno",
                salva.getId(),
                "Solicitação de alteração restrita rejeitada no MOD.",
                anterior,
                novo
        );

        return converterParaDTO(salva);
    }

    /*
     * Validação adicional de segurança.
     *
     * Mesmo que o Controller possua @PreAuthorize,
     * o Service também garante que apenas quem possui
     * ALTERACAO_RESTRITA_APROVAR possa decidir.
     */
    private void validarPermissaoDecisao() {

        if (!possuiPermissao(PERMISSAO_APROVAR)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Somente a Secretaria pode aprovar ou rejeitar alterações restritas"
            );
        }
    }

    private boolean possuiPermissao(
            String permissao) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        permissao.equals(
                                authority.getAuthority()
                        )
                );
    }

    private SolicitacaoAlteracaoAluno buscarSolicitacaoPendente(
            Long solicitacaoId) {

        SolicitacaoAlteracaoAluno solicitacao =
                solicitacaoRepository
                        .findById(solicitacaoId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Solicitação não encontrada"
                                )
                        );

        if (solicitacao.getStatus()
                != StatusSolicitacaoAlteracaoAluno.PENDENTE) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A solicitação já foi decidida"
            );
        }

        return solicitacao;
    }

    private void validarDecisorDiferenteDoSolicitante(
            SolicitacaoAlteracaoAluno solicitacao,
            Usuario decisor) {

        if (solicitacao.getSolicitante() != null
                && solicitacao.getSolicitante().getId()
                .equals(decisor.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O usuário que solicitou a alteração não pode aprovar ou rejeitar a própria solicitação"
            );
        }
    }

    private void validarReferencias(
            SolicitacaoAlteracaoAlunoRequestDTO dto) {

        if (dto.getComumId() != null
                && !comumRepository.existsById(
                dto.getComumId())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Comum não encontrada"
            );
        }

        if (dto.getNivelId() != null
                && !nivelRepository.existsById(
                dto.getNivelId())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Nível não encontrado"
            );
        }

        if (dto.getCargoMinisterioId() != null
                && !cargoMinisterioRepository.existsById(
                dto.getCargoMinisterioId())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Cargo ministerial não encontrado"
            );
        }
    }

    /*
     * Aplicação de alteração oriunda de solicitação
     * aprovada pela Secretaria.
     */
    private void aplicarAlteracoes(
            Aluno aluno,
            SolicitacaoAlteracaoAluno solicitacao) {

        if (solicitacao.getComumId() != null) {

            aluno.setComum(
                    comumRepository
                            .findById(
                                    solicitacao.getComumId()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Comum não encontrada"
                                    )
                            )
            );
        }

        if (solicitacao.getNivelId() != null) {

            aluno.setNivel(
                    nivelRepository
                            .findById(
                                    solicitacao.getNivelId()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Nível não encontrado"
                                    )
                            )
            );
        }

        if (solicitacao.getCargoMinisterioId() != null) {

            aluno.setCargoMinisterio(
                    cargoMinisterioRepository
                            .findById(
                                    solicitacao.getCargoMinisterioId()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Cargo ministerial não encontrado"
                                    )
                            )
            );
        }

        if (solicitacao.getDataBatismo() != null) {

            aluno.setDataBatismo(
                    solicitacao.getDataBatismo()
            );
        }

        if (solicitacao.getDataInicioGem() != null) {

            aluno.setDataInicioGem(
                    solicitacao.getDataInicioGem()
            );
        }

        aluno.setAtualizadoEm(
                LocalDateTime.now()
        );
    }

    /*
     * Aplicação direta quando a Secretaria é
     * o próprio autor da alteração.
     */
    private void aplicarAlteracoesDiretas(
            Aluno aluno,
            SolicitacaoAlteracaoAlunoRequestDTO dto) {

        if (dto.getComumId() != null) {

            aluno.setComum(
                    comumRepository
                            .findById(
                                    dto.getComumId()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Comum não encontrada"
                                    )
                            )
            );
        }

        if (dto.getNivelId() != null) {

            aluno.setNivel(
                    nivelRepository
                            .findById(
                                    dto.getNivelId()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Nível não encontrado"
                                    )
                            )
            );
        }

        if (dto.getCargoMinisterioId() != null) {

            aluno.setCargoMinisterio(
                    cargoMinisterioRepository
                            .findById(
                                    dto.getCargoMinisterioId()
                            )
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Cargo ministerial não encontrado"
                                    )
                            )
            );
        }

        if (dto.getDataBatismo() != null) {

            aluno.setDataBatismo(
                    dto.getDataBatismo()
            );
        }

        if (dto.getDataInicioGem() != null) {

            aluno.setDataInicioGem(
                    dto.getDataInicioGem()
            );
        }

        aluno.setAtualizadoEm(
                LocalDateTime.now()
        );
    }

    private String normalizarObservacao(
            DecisaoAlteracaoAlunoRequestDTO dto) {

        if (dto == null
                || dto.getObservacao() == null
                || dto.getObservacao().isBlank()) {

            return null;
        }

        return dto.getObservacao().trim();
    }

    private Map<String, Object> criarSnapshotRestrito(
            Aluno aluno) {

        Map<String, Object> dados =
                new LinkedHashMap<>();

        dados.put(
                "comumId",
                aluno.getComum() != null
                        ? aluno.getComum().getId()
                        : null
        );

        dados.put(
                "nivelId",
                aluno.getNivel() != null
                        ? aluno.getNivel().getId()
                        : null
        );

        dados.put(
                "cargoMinisterioId",
                aluno.getCargoMinisterio() != null
                        ? aluno.getCargoMinisterio().getId()
                        : null
        );

        dados.put(
                "dataBatismo",
                aluno.getDataBatismo()
        );

        dados.put(
                "dataInicioGem",
                aluno.getDataInicioGem()
        );

        return dados;
    }

    private Map<String, Object> criarSnapshotProposto(
            SolicitacaoAlteracaoAluno solicitacao) {

        Map<String, Object> dados =
                new LinkedHashMap<>();

        dados.put(
                "solicitacaoId",
                solicitacao.getId()
        );

        dados.put(
                "comumId",
                solicitacao.getComumId()
        );

        dados.put(
                "nivelId",
                solicitacao.getNivelId()
        );

        dados.put(
                "cargoMinisterioId",
                solicitacao.getCargoMinisterioId()
        );

        dados.put(
                "dataBatismo",
                solicitacao.getDataBatismo()
        );

        dados.put(
                "dataInicioGem",
                solicitacao.getDataInicioGem()
        );

        dados.put(
                "motivo",
                solicitacao.getMotivo()
        );

        return dados;
    }

    private SolicitacaoAlteracaoAlunoResponseDTO converterParaDTO(
            SolicitacaoAlteracaoAluno solicitacao) {

        SolicitacaoAlteracaoAlunoResponseDTO dto =
                new SolicitacaoAlteracaoAlunoResponseDTO();

        dto.setId(
                solicitacao.getId()
        );

        if (solicitacao.getAluno() != null) {

            dto.setAlunoId(
                    solicitacao.getAluno().getId()
            );

            dto.setAlunoNome(
                    solicitacao.getAluno().getNome()
            );
        }

        if (solicitacao.getSolicitante() != null) {

            dto.setSolicitanteId(
                    solicitacao.getSolicitante().getId()
            );

            dto.setSolicitanteNome(
                    solicitacao.getSolicitante().getNome()
            );
        }

        if (solicitacao.getAprovador() != null) {

            dto.setAprovadorId(
                    solicitacao.getAprovador().getId()
            );

            dto.setAprovadorNome(
                    solicitacao.getAprovador().getNome()
            );
        }

        dto.setStatus(
                solicitacao.getStatus() != null
                        ? solicitacao.getStatus().name()
                        : null
        );

        dto.setComumId(
                solicitacao.getComumId()
        );

        dto.setNivelId(
                solicitacao.getNivelId()
        );

        dto.setCargoMinisterioId(
                solicitacao.getCargoMinisterioId()
        );

        dto.setDataBatismo(
                solicitacao.getDataBatismo()
        );

        dto.setDataInicioGem(
                solicitacao.getDataInicioGem()
        );

        dto.setMotivo(
                solicitacao.getMotivo()
        );

        dto.setObservacaoDecisao(
                solicitacao.getObservacaoDecisao()
        );

        dto.setCriadoEm(
                solicitacao.getCriadoEm()
        );

        dto.setDecididoEm(
                solicitacao.getDecididoEm()
        );

        return dto;
    }
}