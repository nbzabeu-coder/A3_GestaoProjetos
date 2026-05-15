// Package: organiza a classe dentro da estrutura de pastas do projeto 
// (domínio reverso)
package com.gestaoprojetos.model;

//Importando do java.util para usar as classes de coleção, como List e ArrayList
import java.util.ArrayList;
import java.util.List;

//Classe Equipe: representa a equipe de um (ou mais) projeto(s),
// composta por membros (usuários)
public class Equipe {

    //Declarando os atributos da classe Equipe
    private int id;
    private String nome;
    private String descricao;
    private List<Usuario> membros; // Lista de membros da equipe

    //Construtor: inicializa os atributos essenciais da equipe
    public Equipe(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da equipe é obrigatório");
        }
        this.nome = nome;
        this.membros = new ArrayList<>(); // Inicializa a lista de membros vazia
    }

    //Getters: leem os valores dos atributos da equipe
    // O getter de membros retorna a lista completa de membros da equipe
    // (pode ser vazia se ainda não houver membros adicionados)
    public int getId() {
        return this.id;
    }
    public String getNome() {
        return this.nome;
    }
    public String getDescricao() {
        return this.descricao;
    }
    public List<Usuario> listarMembros() {
        return new ArrayList<>(this.membros); // Retorna uma cópia da lista de  membros
                                              // para evitar modificações externas
    }

    //Setters: alteram apenas os atributos editáveis pelo cliente 
    //(no caso, nome e descrição da equipe)
    public void setId(int id) {
        this.id = id;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da equipe é obrigatório");
        }
        this.nome = nome;
    }

    //Métodos de domínio: ações que a equipe pode realizar, como adicionar ou 
    // remover membros
    public void adicionarMembro(Usuario membro) {
        if (membro == null) {
            throw new IllegalArgumentException("Membro não pode ser nulo");
        }
        if (this.membros.contains(membro)) {
            throw new IllegalStateException("Membro já está na equipe");
        }
        this.membros.add(membro);
    }
    public void removerMembro(Usuario membro) {
        if (membro == null) {
            throw new IllegalArgumentException("Membro não pode ser nulo");
        }
        this.membros.remove(membro);
    }
}
