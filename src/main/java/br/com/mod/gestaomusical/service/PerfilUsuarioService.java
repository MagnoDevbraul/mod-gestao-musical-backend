package br.com.mod.gestaomusical.service;

import br.com.mod.gestaomusical.dto.PerfilUsuarioResponseDTO;
import br.com.mod.gestaomusical.entity.PerfilUsuario;
import br.com.mod.gestaomusical.repository.PerfilUsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class PerfilUsuarioService {

    private final PerfilUsuarioRepository repository;

    public PerfilUsuarioService(
            PerfilUsuarioRepository repository) {

        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PerfilUsuarioResponseDTO> listarTodos() {

        return repository
                .findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                PerfilUsuario::getNome,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .map(this::converterParaDTO)
                .toList();
    }

    private PerfilUsuarioResponseDTO converterParaDTO(
            PerfilUsuario perfil) {

        PerfilUsuarioResponseDTO dto =
                new PerfilUsuarioResponseDTO();

        dto.setId(
                perfil.getId()
        );

        dto.setNome(
                perfil.getNome()
        );

        dto.setDescricao(
                perfil.getDescricao()
        );

        dto.setAtivo(
                perfil.getAtivo()
        );

        return dto;
    }
}