// Package: organiza a classe dentro da estrutura de pastas do projeto 
// (domínio reverso)
package com.gestaoprojetos.model;

// Importando classes para manipulação de datas e horas
import java.time.LocalDate;
import java.time.LocalDateTime;

// Classe tarefa: representa as tarefas que compõem um projeto
public class Tarefa {
    private String titulo;
    private String descricao;
    private LocalDateTime dataInicioReal;
    private LocalDateTime dataTerminoReal;
    private LocalDate dataTerminoPrevista;
    private StatusTarefa status;
    private Prioridade prioridade;
    private Usuario responsavel;

    // Construtor: inicializa os atributos essenciais da tarefa
    public Tarefa(String titulo, LocalDate dataTerminoPrevista,
                  Prioridade prioridade) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório");
        }
        if (dataTerminoPrevista == null) {
            throw new IllegalArgumentException("Data de término prevista é obrigatória");
        }
        if (prioridade == null) {
            throw new IllegalArgumentException("Prioridade é obrigatória");
        }
        this.titulo = titulo;
        this.dataTerminoPrevista = dataTerminoPrevista;
        this.prioridade = prioridade;
        this.status = StatusTarefa.PENDENTE; // Toda tarefa começa com status PENDENTE
    }

    // Getters: leem os valores dos atributos da tarefa.
    // Alguns podem retornar null se ainda não foram preenchidos pois não incluímos 
    // no construtor (ex: responsavel, dataInicioReal, dataTerminoReal)
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
    public Usuario getResponsavel() {
        return this.responsavel;
    }

    // Setters: alteram apenas os atributos editáveis pelo cliente
    // (titulo, descricao, dataTerminoPrevista, prioridade).
    // Status e datas reais não têm setter — são alterados pelos métodos de negócio.
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
    // Cada método valida se a operação é permitida no estado atual antes de executar.
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
        this.responsavel = responsavel;
    }
    public void removerResponsavel() {
        this.responsavel = null; // Permite desatribuir o responsável, deixando a tarefa sem responsável
    }
}
