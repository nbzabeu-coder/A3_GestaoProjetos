package com.gestaoprojetos.model;

// Herança: Gerente é um tipo específico de Usuario,
// que herda os atributos e comportamentos comuns (nome, cpf, email, etc.)
public class Gerente extends Usuario {

    public Gerente(String nome, String cpf, String email,
                    String cargo, String login, String senha) {
        // Chama o construtor da superclasse (Usuario) para validar e preencher os atributos comuns
        super(nome, cpf, email, cargo, login, senha);
    }
    
    @Override // Define o comportamento específico do gerente para as permissões
    public String permissoes() {
        return "Gerenciar projetos próprios e equipes alocadas";
    }
}
