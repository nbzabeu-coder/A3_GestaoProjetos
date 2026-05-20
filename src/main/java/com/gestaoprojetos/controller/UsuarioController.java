// Package: organiza a classe dentro da estrutura de pastas do projeto 
// (domínio reverso)
package com.gestaoprojetos.controller;

// Import de coleções (List, para retornar listas de usuários)
import java.util.List;

// Imports das classes do projeto (DAO de persistência e entidade Usuario)
import com.gestaoprojetos.dao.UsuarioDAO;
import com.gestaoprojetos.model.Usuario;

// Controller da entidade Usuario: camada intermediária do padrão MVC.
// Recebe as ações vindas da View, orquestra as chamadas ao UsuarioDAO
// e devolve as respostas adequadas.
public class UsuarioController {

    // Atributo: instância do DAO usada para acessar a persistência de usuários
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    // CREATE: cadastra um novo usuário no sistema (delega a inserção ao DAO)
    public void cadastrar(Usuario usuario) {
        usuarioDAO.inserir(usuario);
    }

    // READ: busca um usuário pelo seu id, retornando null se não encontrar
    public Usuario buscarPorId(int id) {
        return usuarioDAO.buscarPorId(id);
    }

    // READ: lista todos os usuários cadastrados no sistema
    public List<Usuario> listarTodos() {
        return usuarioDAO.listarTodos();
    }

    // UPDATE: atualiza os dados de um usuário existente
    public void atualizar(Usuario usuario) {
        usuarioDAO.atualizar(usuario);
    }

    // DELETE: remove um usuário do sistema pelo seu id
    public void remover(int id) {
        usuarioDAO.remover(id);
    }

    // Autenticação: busca o usuário pelo login e confere a senha.
    // Retorna o objeto Usuario se as credenciais baterem, ou null caso contrário.
    public Usuario autenticar(String login, String senha) {
        Usuario usuario = usuarioDAO.buscarPorLogin(login);
        if (usuario != null && usuario.conferirSenha(senha)) {
            return usuario;
        }
        return null;
    }
}
