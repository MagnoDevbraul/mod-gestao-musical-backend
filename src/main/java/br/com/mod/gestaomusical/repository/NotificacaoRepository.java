package br.com.mod.gestaomusical.repository;

import br.com.mod.gestaomusical.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository
        extends JpaRepository<Notificacao, Long> {

    Optional<Notificacao>
    findTopByAlunoIdAndUsuarioIdAndTipoEventoOrderByDataHoraDesc(
            Long alunoId,
            Long usuarioId,
            String tipoEvento
    );

    List<Notificacao>
    findTop5ByOrderByDataHoraDesc();
}