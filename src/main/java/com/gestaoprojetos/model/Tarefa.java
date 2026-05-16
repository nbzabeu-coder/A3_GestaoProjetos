// Package: organiza a classe dentro da estrutura de pastas do projeto 
// (domínio reverso)
package com.gestaoprojetos.model;

// Importando classes para manipulação de datas e horas
import java.time.LocalDate;
import java.time.LocalDateTime;

// Classe tarefa: representa as tarefas que compõem um projeto
public class Tarefa {
    private int id;
    private int projetoId;
    private String titulo;
    private String descricao;
    private LocalDateTime dataInicioReal;
    private LocalDateTime dataTerminoReal;
    private LocalDate dataTerminoPrevista;
    private StatusTarefa status;
    private Prioridade prioridade;
    private Equipe equipe;
    private Usuario responsavel;

    // Construtor: inicializa os atributos essenciais da tarefa
    public Tarefa(String titulo, LocalDate dataTerminoPrevista,
            Prioridade prioridade, Equipe equipe, int projetoId) {
        if (projetoId <= 0) {
            throw new IllegalArgumentException("Projeto é obrigatório");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        if (dataTerminoPrevista == null) {
            throw new IllegalArgumentException("Data de término prevista é obrigatória");
        }
        if (prioridade == null) {
            throw new IllegalArgumentException("Prioridade é obrigatória");
        }
        if (equipe == null) {
            throw new IllegalArgumentException("Equipe é obrigatória");
        }
        this.titulo = titulo;
        this.dataTerminoPrevista = dataTerminoPrevista;
        this.prioridade = prioridade;
        this.equipe = equipe;
        this.status = StatusTarefa.PENDENTE; // Toda tarefa começa com status PENDENTE
        this.projetoId = projetoId;
    }

    // Construtor de reconstituição — usado pela camada de persistência (DAO)
    // para recriar uma Tarefa que já existe no banco, com todos os seus dados.
    public Tarefa(int id, String titulo, String descricao,
            LocalDateTime dataInicioReal, LocalDateTime dataTerminoReal,
            LocalDate dataTerminoPrevista, StatusTarefa status,
            Prioridade prioridade, Equipe equipe, Usuario responsavel,
            int projetoId) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataInicioReal = dataInicioReal;
        this.dataTerminoReal = dataTerminoReal;
        this.dataTerminoPrevista = dataTerminoPrevista;
        this.status = status;
        this.prioridade = prioridade;
        this.equipe = equipe;
        this.responsavel = responsavel;
        this.projetoId = projetoId;
    }

    // Getters: leem os valores dos atributos da tarefa.
    // Alguns podem retornar null se ainda não foram preenchidos pois não incluímos
    // no construtor (ex: responsavel, dataInicioReal, dataTerminoReal)
    public int getId() {
        return this.id;
    }

    public int getProjetoId() {
        return this.projetoId;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public LocalDateTime getDataInicioReal() {
        return this.dataInicioReal;
    }

    public LocalDateTime getDataTerminoReal() {
        return this.dataTerminoReal;
    }

    public LocalDate getDataTerminoPrevista() {
        return this.dataTerminoPrevista;
    }

    public StatusTarefa getStatus() {
        return this.status;
    }

    public Prioridade getPrioridade() {
        return this.prioridade;
    }

    public Equipe getEquipe() {
        return this.equipe;
    }

    public Usuario getResponsavel() {
        return this.responsavel;
    }

    // Setters: alteram apenas os atributos editáveis pelo cliente
    // (titulo, descricao, dataTerminoPrevista, prioridade).
    // Status e datas reais não têm setter — são alterados pelos métodos de negócio.
    public void setId(int id) {
        this.id = id;
    }

    public void setProjetoId(int projetoId) {
        this.projetoId = projetoId;
    }

    public void setTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        this.titulo = titulo;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao; // Descrição é opcional, pode ser nula ou vazia
    }

    public void setDataTerminoPrevista(LocalDate dataTerminoPrevista) {
        if (dataTerminoPrevista == null) { // isBlank só se aplica a String
            throw new IllegalArgumentException("Data de término prevista é obrigatória");
        }
        this.dataTerminoPrevista = dataTerminoPrevista;
    }

    public void setPrioridade(Prioridade prioridade) {
        if (prioridade == null) {
            throw new IllegalArgumentException("Prioridade é obrigatória");
        }
        this.prioridade = prioridade;
    }

    // Métodos de negócio: alteram o estado da tarefa seguindo as regras do domínio.
    // Cada método valida se a operação é permitida no estado atual antes de
    // executar.
    public void iniciar() {
        if (this.status != StatusTarefa.PENDENTE) {
            throw new IllegalStateException("Só é possível iniciar uma tarefa que esteja PENDENTE");
        }
        this.status = StatusTarefa.EM_ANDAMENTO;
        this.dataInicioReal = LocalDateTime.now(); // Registra a data e hora de início
    }

    public void concluir() {
        if (this.status != StatusTarefa.EM_ANDAMENTO) {
            throw new IllegalStateException("Só é possível concluir uma tarefa que esteja EM ANDAMENTO");
        }
        this.status = StatusTarefa.CONCLUIDA;
        this.dataTerminoReal = LocalDateTime.now(); // Registra a data e hora de término
    }

    public void reabrir() {
        if (this.status != StatusTarefa.CONCLUIDA) {
            throw new IllegalStateException("Só é possível reabrir uma tarefa que esteja CONCLUIDA");
        }
        this.status = StatusTarefa.EM_ANDAMENTO; // Reabre a tarefa, voltando para EM_ANDAMENTO
        this.dataTerminoReal = null; // Limpa a data de término real, pois a tarefa volta a estar em andamento
    }

    public void atribuirResponsavel(Usuario responsavel) {
        if (responsavel == null) {
            throw new IllegalArgumentException("Responsável é obrigatório");
        }
        if (!this.equipe.listarMembros().contains(responsavel)) {
            throw new IllegalArgumentException("Responsável deve ser membro da equipe da tarefa");
        }
        this.responsavel = responsavel;
    }

    public void removerResponsavel() {
        this.responsavel = null; // Permite desatribuir o responsável, deixando a tarefa sem responsável
    }
}
