package br.com.mod.gestaomusical.service;

import br.com.mod.gestaomusical.dto.AtualizarPermissoesPerfilRequestDTO;
import br.com.mod.gestaomusical.dto.PermissaoResponseDTO;
import br.com.mod.gestaomusical.entity.PerfilUsuario;
import br.com.mod.gestaomusical.entity.Permissao;
import br.com.mod.gestaomusical.repository.PerfilUsuarioRepository;
import br.com.mod.gestaomusical.repository.PermissaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissaoService {

    private static final String PERFIL_SECRETARIA =
            "SECRETARIA";

    private static final String PERMISSAO_APROVAR_ALTERACAO_RESTRITA =
            "ALTERACAO_RESTRITA_APROVAR";

    private final PermissaoRepository permissaoRepository;
    private final PerfilUsuarioRepository perfilUsuarioRepository;
    private final AuditoriaService auditoriaService;

    public PermissaoService(
            PermissaoRepository permissaoRepository,
            PerfilUsuarioRepository perfilUsuarioRepository,
            AuditoriaService auditoriaService) {

        this.permissaoRepository =
                permissaoRepository;

        this.perfilUsuarioRepository =
                perfilUsuarioRepository;

        this.auditoriaService =
                auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<PermissaoResponseDTO> listarTodas() {

        return permissaoRepository
                .findAllByOrderByNomeAsc()
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PermissaoResponseDTO> listarPorPerfil(
            Long perfilId) {

        PerfilUsuario perfil =
                perfilUsuarioRepository
                        .findComPermissoesById(perfilId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Perfil de usuário não encontrado"
                                )
                        );

        return perfil.getPermissoes()
                .stream()
                .sorted(
                        (a, b) ->
                                a.getNome()
                                        .compareToIgnoreCase(
                                                b.getNome()
                                        )
                )
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public List<PermissaoResponseDTO> atualizarPermissoesPerfil(
            Long perfilId,
            AtualizarPermissoesPerfilRequestDTO dto) {

        if (
                dto == null
                        || dto.getPermissaoIds() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Lista de permissões é obrigatória"
            );
        }

        PerfilUsuario perfil =
                perfilUsuarioRepository
                        .findComPermissoesById(perfilId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Perfil de usuário não encontrado"
                                )
                        );

        Permissao permissaoCritica =
                permissaoRepository
                        .findByNome(
                                PERMISSAO_APROVAR_ALTERACAO_RESTRITA
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        "Permissão obrigatória "
                                                + PERMISSAO_APROVAR_ALTERACAO_RESTRITA
                                                + " não encontrada"
                                )
                        );

        boolean perfilEhSecretaria =
                perfil.getNome() != null
                        && PERFIL_SECRETARIA
                        .equalsIgnoreCase(
                                perfil.getNome().trim()
                        );

        /*
         * Verifica diretamente se o ID da
         * permissão crítica foi enviado.
         */
        boolean solicitouPermissaoCritica =
                dto.getPermissaoIds()
                        .contains(
                                permissaoCritica.getId()
                        );

        /*
         * REGRA CRÍTICA:
         *
         * Somente SECRETARIA pode possuir
         * ALTERACAO_RESTRITA_APROVAR.
         */
        if (
                !perfilEhSecretaria
                        && solicitouPermissaoCritica
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A permissão "
                            + PERMISSAO_APROVAR_ALTERACAO_RESTRITA
                            + " é exclusiva do perfil SECRETARIA"
            );
        }

        Set<Long> anteriores =
                perfil.getPermissoes()
                        .stream()
                        .map(Permissao::getId)
                        .collect(
                                Collectors.toSet()
                        );

        List<Permissao> encontradas =
                permissaoRepository
                        .findAllById(
                                dto.getPermissaoIds()
                        );

        if (
                encontradas.size()
                        != dto.getPermissaoIds().size()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Uma ou mais permissões não foram encontradas"
            );
        }

        Set<Permissao> novas =
                new HashSet<>(
                        encontradas
                );

        /*
         * SECRETARIA nunca pode perder
         * ALTERACAO_RESTRITA_APROVAR.
         */
        if (perfilEhSecretaria) {

            novas.add(
                    permissaoCritica
            );
        }

        /*
         * Proteção adicional.
         *
         * Mesmo que algum dado inconsistente
         * tenha chegado até aqui, nenhum perfil
         * diferente da SECRETARIA manterá
         * a permissão crítica.
         */
        if (!perfilEhSecretaria) {

            novas.removeIf(
                    permissao ->
                            permissao.getId()
                                    .equals(
                                            permissaoCritica.getId()
                                    )
            );
        }

        perfil.setPermissoes(
                novas
        );

        perfilUsuarioRepository.save(
                perfil
        );

        Map<String, Object> anterior =
                new LinkedHashMap<>();

        anterior.put(
                "permissaoIds",
                anteriores
        );

        Map<String, Object> novo =
                new LinkedHashMap<>();

        novo.put(
                "permissaoIds",
                novas
                        .stream()
                        .map(Permissao::getId)
                        .collect(
                                Collectors.toSet()
                        )
        );

        auditoriaService.registrar(
                "ALTERACAO_PERMISSOES_PERFIL",
                "perfil_usuario",
                perfil.getId(),
                "Permissões do perfil alteradas no MOD.",
                anterior,
                novo
        );

        return novas
                .stream()
                .sorted(
                        (a, b) ->
                                a.getNome()
                                        .compareToIgnoreCase(
                                                b.getNome()
                                        )
                )
                .map(this::converterParaDTO)
                .toList();
    }

    private PermissaoResponseDTO converterParaDTO(
            Permissao permissao) {

        PermissaoResponseDTO dto =
                new PermissaoResponseDTO();

        dto.setId(
                permissao.getId()
        );

        dto.setNome(
                permissao.getNome()
        );

        dto.setDescricao(
                permissao.getDescricao()
        );

        dto.setAtivo(
                permissao.getAtivo()
        );

        return dto;
    }
}