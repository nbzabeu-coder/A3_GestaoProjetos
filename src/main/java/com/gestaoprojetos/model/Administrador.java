// Package: organiza a classe dentro da estrutura de pastas do projeto 
// (domínio reverso)
package com.gestaoprojetos.model;

// Herança: Administrador é um tipo específico de Usuario,
// que herda os atributos e comportamentos comuns (nome, cpf, email, etc.)
public class Administrador extends Usuario {
    
    public Administrador(String nome, String cpf, String email,
                        String cargo, String login, String senha) {
        // Chama o construtor da superclasse (Usuario) para validar e preencher os atributos comuns
        super(nome, cpf, email, cargo, login, senha);
    }
    
    @Override // Define o comportamento específico do administrador para as permissões
    public String permissoes() {
        return "Acesso total ao sistema";
    }
}
