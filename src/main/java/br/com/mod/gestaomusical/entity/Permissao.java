package br.com.mod.gestaomusical.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "permissao")
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "nome",
            nullable = false,
            unique = true
    )
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(
            name = "ativo",
            nullable = false
    )
    private Boolean ativo;

    @Column(
            name = "criado_em",
            nullable = false
    )
    private LocalDateTime criadoEm;

    @Column(
            name = "atualizado_em",
            nullable = false
    )
    private LocalDateTime atualizadoEm;


    public Permissao() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getNome() {
        return nome;
    }


    public void setNome(String nome) {
        this.nome = nome;
    }


    public String getDescricao() {
        return descricao;
    }


    public void setDescricao(
            String descricao
    ) {
        this.descricao = descricao;
    }


    public Boolean getAtivo() {
        return ativo;
    }


    public void setAtivo(
            Boolean ativo
    ) {
        this.ativo = ativo;
    }


    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }


    public void setCriadoEm(
            LocalDateTime criadoEm
    ) {
        this.criadoEm = criadoEm;
    }


    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }


    public void setAtualizadoEm(
            LocalDateTime atualizadoEm
    ) {
        this.atualizadoEm = atualizadoEm;
    }

}