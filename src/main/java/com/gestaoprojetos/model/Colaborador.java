package com.gestaoprojetos.model;

// Herança: Colaborador é um tipo específico de Usuario,
// que herda os atributos e comportamentos comuns (nome, cpf, email, etc.)
public class Colaborador extends Usuario {
    
    public Colaborador(String nome, String cpf, String email,
                        String cargo, String login, String senha) {
        // Chama o construtor da superclasse (Usuario) para validar e preencher os atributos comuns
        super(nome, cpf, email, cargo, login, senha);
    }

    @Override // Define o comportamento específico do colaborador para as permissões
    public String permissoes() {
        return "Visualizar e atualizar tarefas atribuídas";
    }
}
