package br.com.mod.gestaomusical.repository;

import br.com.mod.gestaomusical.entity.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissaoRepository
        extends JpaRepository<Permissao, Long> {

    List<Permissao> findAllByOrderByNomeAsc();

    Optional<Permissao> findByNome(String nome);
}