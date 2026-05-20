// Package: organiza a classe dentro da estrutura de pastas do projeto 
// (domínio reverso)
package com.gestaoprojetos.controller;

// Import de coleções (List, para retornar listas de equipes e de membros)
import java.util.List;

// Imports das classes do projeto (DAO de persistência e entidades)
import com.gestaoprojetos.dao.EquipeDAO;
import com.gestaoprojetos.model.Equipe;
import com.gestaoprojetos.model.Usuario;

// Controller da entidade Equipe: camada intermediária do padrão MVC.
// Recebe as ações vindas da View, orquestra as chamadas ao EquipeDAO
// e devolve as respostas adequadas.
public class EquipeController {

    // Atributo: instância do DAO usada para acessar a persistência de equipes
    private EquipeDAO equipeDAO = new EquipeDAO();

    // CREATE: cadastra uma nova equipe no sistema (delega a inserção ao DAO)
    public void cadastrar(Equipe equipe) {
        equipeDAO.inserir(equipe);
    }

    // READ: busca uma equipe pelo seu id, retornando null se não encontrar
    public Equipe buscarPorId(int id) {
        return equipeDAO.buscarPorId(id);
    }

    // READ: lista todas as equipes cadastradas no sistema
    public List<Equipe> listarTodos() {
        return equipeDAO.listarTodos();
    }

    // UPDATE: atualiza os dados de uma equipe existente
    public void atualizar(Equipe equipe) {
        equipeDAO.atualizar(equipe);
    }

    // DELETE: remove uma equipe do sistema pelo seu id
    public void remover(int id) {
        equipeDAO.remover(id);
    }

    // ===== Gestão de membros (relação N:N) =====

    // Adiciona um usuário como membro de uma equipe
    public void adicionarMembro(int equipeId, int usuarioId) {
        equipeDAO.adicionarMembro(equipeId, usuarioId);
    }

    // Remove um usuário da lista de membros de uma equipe
    public void removerMembro(int equipeId, int usuarioId) {
        equipeDAO.removerMembro(equipeId, usuarioId);
    }

    // Lista os usuários que são membros de uma equipe específica
    public List<Usuario> listarMembros(int equipeId) {
        return equipeDAO.listarMembros(equipeId);
    }
}
