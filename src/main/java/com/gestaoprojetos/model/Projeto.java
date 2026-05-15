// Package: organiza a classe dentro da estrutura de pastas do projeto 
// (domínio reverso)
package com.gestaoprojetos.model;

//Importando do java.util para usar as classes de coleção, como List e ArrayList
import java.util.ArrayList;
import java.util.List;

// Importando classes para manipulação de datas
import java.time.LocalDate;

//Classe Projeto: representa um projeto, que pode ter uma equipe associada,
// um gerente responsável, e outras informações relevantes como nome, descrição, etc.
public class Projeto {
    private String nome;
    private String descricao;
    private LocalDate dataInicioPrevista;
    private LocalDate dataInicioReal;
    private LocalDate dataTerminoPrevista;
    private LocalDate dataTerminoReal;
    private StatusProjeto status;
    private Gerente gerente;
    private List<Tarefa> tarefas;
    private List<Equipe> equipes;
    
    //Construtor: inicializa os atributos essenciais do projeto
    public Projeto(String nome, LocalDate dataInicioPrevista, LocalDate dataTerminoPrevista, Gerente gerente) {
        //Validações de null - tendo certeza que os dados essenciais para criar um projeto estão presentes
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do projeto é obrigatório");
        }
        if (dataInicioPrevista == null) {
            throw new IllegalArgumentException("Data de início prevista é obrigatória");
        }
        if (dataTerminoPrevista == null) {
            throw new IllegalArgumentException("Data de término prevista é obrigatória");
        }
        if (gerente == null) {
            throw new IllegalArgumentException("Gerente é obrigatório");
        }
        // Validação de lógica de datas: a data de início prevista não pode ser posterior à data de término prevista
        if (dataInicioPrevista.isAfter(dataTerminoPrevista)) {
            throw new IllegalArgumentException("Data de início prevista não pode ser posterior à data de término prevista");
        }
        // Atribuição dos valores aos atributos do projeto
        this.nome = nome;
        this.dataInicioPrevista = dataInicioPrevista;
        this.dataTerminoPrevista = dataTerminoPrevista;
        this.gerente = gerente;
        this.status = StatusProjeto.PLANEJADO; // Todo projeto começa com status PLANEJADO
        this.tarefas = new ArrayList<>(); // Inicializa a lista de tarefas vazia
        this.equipes = new ArrayList<>(); // Inicializa a lista de equipes vazia
    }

    // Getters: leem os valores dos atributos do projeto
    public String getNome() {
        return this.nome;
    }
    public String getDescricao() {
        return this.descricao;
    }
    public LocalDate getDataInicioPrevista() {
        return this.dataInicioPrevista;
    }
    public LocalDate getDataInicioReal() {
        return this.dataInicioReal;
    }
    public LocalDate getDataTerminoPrevista() {
        return this.dataTerminoPrevista;
    }
    public LocalDate getDataTerminoReal() {
        return this.dataTerminoReal;
    }
    public StatusProjeto getStatus() {
        return this.status;
    }
    public Gerente getGerente() {
        return this.gerente;
    }
    public List<Tarefa> listarTarefas() {
        return new ArrayList<>(this.tarefas); // Retorna uma cópia da lista de tarefas para evitar modificações externas
    }
    public List<Equipe> listarEquipes() {
        return new ArrayList<>(this.equipes); // Retorna uma cópia da lista de equipes para evitar modificações externas
    }

    // Setters: alteram apenas os atributos editáveis pelo cliente
    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do projeto é obrigatório");
        }
        this.nome = nome;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao; // A descrição é opcional, então não tem validação de null ou blank
    }
    public void setDataInicioPrevista(LocalDate dataInicioPrevista) {
        if (dataInicioPrevista == null) {
            throw new IllegalArgumentException("Data de início prevista é obrigatória");
        }
        if (this.dataTerminoPrevista != null 
        && dataInicioPrevista.isAfter(this.dataTerminoPrevista)) {
            throw new IllegalArgumentException("Data de início prevista não pode ser posterior à data de término prevista");
        }
        this.dataInicioPrevista = dataInicioPrevista;
    }
    public void setDataTerminoPrevista(LocalDate dataTerminoPrevista) {
        if (dataTerminoPrevista == null) {
            throw new IllegalArgumentException("Data de término prevista é obrigatória");
        }
        if (this.dataInicioPrevista != null 
        && dataTerminoPrevista.isBefore(this.dataInicioPrevista)) {
            throw new IllegalArgumentException("Data de término prevista não pode ser anterior à data de início prevista");
        }
        this.dataTerminoPrevista = dataTerminoPrevista;
    }
    public void setGerente(Gerente gerente) {
        if (gerente == null) {
            throw new IllegalArgumentException("Gerente é obrigatório");
        }
        this.gerente = gerente;
    }

    //Métodos de domínio: ações que o projeto pode realizar, como adicionar tarefas ou equipes
    public void iniciar() {
        if (this.status != StatusProjeto.PLANEJADO) {
            throw new IllegalStateException("Só é possível iniciar um projeto que esteja PLANEJADO");
        }
        this.status = StatusProjeto.EM_ANDAMENTO;
        this.dataInicioReal = LocalDate.now(); // Define a data de início real como a data atual
    }
    public void concluir() {
        if (this.status != StatusProjeto.EM_ANDAMENTO) {
            throw new IllegalStateException("Só é possível concluir um projeto que esteja EM_ANDAMENTO");
        }
        this.status = StatusProjeto.CONCLUIDO;
        this.dataTerminoReal = LocalDate.now(); // Define a data de término real como a data atual
    }
    public void cancelar() {
        if (this.status != StatusProjeto.PLANEJADO
            && this.status != StatusProjeto.EM_ANDAMENTO) {
            throw new IllegalStateException("Só é possível cancelar um projeto PLANEJADO ou EM_ANDAMENTO");
        }
        this.status = StatusProjeto.CANCELADO;
        this.dataTerminoReal = LocalDate.now(); // Define a data de término real como a data atual
    }
    public void adicionarEquipe(Equipe equipe) {
        if (equipe == null) {
            throw new IllegalArgumentException("Equipe não pode ser nula");
        }
        if (this.equipes.contains(equipe)) {
            throw new IllegalStateException("Equipe já está associada a este projeto");
        }
        this.equipes.add(equipe); // Adiciona a equipe à LISTA de equipes associadas ao projeto
    }
    public void adicionarTarefa(Tarefa tarefa) {
        if (tarefa == null) {
            throw new IllegalArgumentException("Tarefa não pode ser nula");
        }
        // Validação importante: a equipe responsável pela tarefa deve estar associada a este projeto
        if (!this.equipes.contains(tarefa.getEquipe())) {
            throw new IllegalStateException("A equipe da tarefa não está alocada a este projeto");
        }
        if (this.tarefas.contains(tarefa)) {
            throw new IllegalStateException("Tarefa já está adicionada ao projeto");
        }
        this.tarefas.add(tarefa); // Adiciona a tarefa à LISTA de tarefas do projeto
    }
    public String gerarRelatorio() {
    return "=== Relatório do Projeto ===\n" +
           "Nome: " + this.nome + "\n" +
           "Status: " + this.status + "\n" +
           "Gerente: " + this.gerente.getNome() + "\n" +
           "Data início prevista: " + this.dataInicioPrevista + "\n" +
           "Data término prevista: " + this.dataTerminoPrevista + "\n" +
           "Total de tarefas: " + this.tarefas.size() + "\n" +
           "Total de equipes: " + this.equipes.size();
    }
}