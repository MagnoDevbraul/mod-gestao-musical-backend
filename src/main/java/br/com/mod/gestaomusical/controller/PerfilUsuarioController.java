package br.com.mod.gestaomusical.controller;

import br.com.mod.gestaomusical.dto.PerfilUsuarioResponseDTO;
import br.com.mod.gestaomusical.service.PerfilUsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/perfis-usuarios")
@PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
public class PerfilUsuarioController {

    private final PerfilUsuarioService service;

    public PerfilUsuarioController(
            PerfilUsuarioService service) {

        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PerfilUsuarioResponseDTO>>
    listarTodos() {

        return ResponseEntity.ok(
                service.listarTodos()
        );
    }
}