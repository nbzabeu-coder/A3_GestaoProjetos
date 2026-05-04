// Package: organiza a classe dentro da estrutura de pastas do projeto 
// (domínio reverso)
package com.gestaoprojetos.model;

// Classe abstrata: define a base comum dos usuários do sistema
// (não pode ser instanciada diretamente — só via subclasses
//  Administrador, Gerente, Colaborador)
public abstract class Usuario {

    // Declaração dos atributos privados (encapsulamento — só a classe enxerga)
    private String nome;
    private String cpf;
    private String email;
    private String cargo;
    private String login;
    private String senha;

    // Construtor: só é executado quando `new Usuario(...)` é chamado.
    // Verifica se cada valor recebido é válido (não nulo, não vazio)
    // e só então preenche os atributos do objeto que está sendo criado.
    public Usuario(String nome, String cpf, String email,
                   String cargo, String login, String senha) {
        // Estrutura condicional: testa se o valor recebido é inválido (nulo ou vazio)
        if (nome == null || nome.isBlank()) {
            // Dispara um erro: aborta o construtor com a mensagem indicada
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório");
        }
        if (cargo == null || cargo.isBlank()) {
            throw new IllegalArgumentException("Cargo é obrigatório");
        }
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login é obrigatório");
        }
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }

        // Atribuição: copia o valor de cada parâmetro para o
        // atributo correspondente do objeto que está sendo criado
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.cargo = cargo;
        this.login = login;
        this.senha = senha;
    }

    // Getters: dão acesso de leitura aos atributos privados,
    // mantendo o encapsulamento (os atributos continuam protegidos)
    public String getNome() {
        return this.nome;
    }
    public String getCpf() {
        return this.cpf;
    }
    public String getEmail() {
        return this.email;
    }
    public String getCargo() {
        return this.cargo;
    }
    public String getLogin() {
        return this.login;
    }

    // Setters: alteram os valores dos atributos depois da criação,
    // sempre validando o novo valor (mesma regra do construtor)
    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        this.nome = nome; // Atribuição: copia o valor recebido para o atributo do objeto
    }
    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório");
        }
        this.email = email;
    }
    public void setCargo(String cargo) {
        if (cargo == null || cargo.isBlank()) {
            throw new IllegalArgumentException("Cargo é obrigatório");
        }
        this.cargo = cargo;
    }
    public void setSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        this.senha = senha;
    }

    // Compara a senha informada com a senha armazenada e retorna true/false.
    // Não existe getSenha por questão de segurança — só permite verificar, não ler.
    public boolean conferirSenha(String tentativa) {
        return this.senha.equals(tentativa);
    }

    // Método abstrato — é o que justifica a classe ser declarada abstract.
    // Cada subclasse (Administrador, Gerente, Colaborador) é obrigada a implementar.
    public abstract String permissoes();

}
