// Package: organiza a classe dentro da estrutura de pastas do projeto 
// (domínio reverso)
package com.gestaoprojetos.controller;

// Import de coleções (List, para retornar listas de tarefas)
import java.util.List;

// Imports das classes do projeto (DAO de persistência e entidades)
import com.gestaoprojetos.dao.TarefaDAO;
import com.gestaoprojetos.model.Tarefa;
import com.gestaoprojetos.model.Usuario;

// Controller da entidade Tarefa: camada intermediária do padrão MVC.
// Recebe as ações vindas da View, orquestra as chamadas ao TarefaDAO
// (e aos métodos de negócio do domínio) e devolve as respostas adequadas.
public class TarefaController {

    // Atributo: instância do DAO usada para acessar a persistência de tarefas
    private TarefaDAO tarefaDAO = new TarefaDAO();

    // CREATE: cadastra uma nova tarefa no sistema (delega a inserção ao DAO)
    public void cadastrar(Tarefa tarefa) {
        tarefaDAO.inserir(tarefa);
    }

    // READ: busca uma tarefa pelo seu id, retornando null se não encontrar
    public Tarefa buscarPorId(int id) {
        return tarefaDAO.buscarPorId(id);
    }

    // READ: lista todas as tarefas cadastradas no sistema
    public List<Tarefa> listarTodos() {
        return tarefaDAO.listarTodos();
    }

    // READ: lista as tarefas de um projeto específico
    public List<Tarefa> listarPorProjeto(int projetoId) {
        return tarefaDAO.listarPorProjeto(projetoId);
    }

    // UPDATE: atualiza os dados de uma tarefa existente
    public void atualizar(Tarefa tarefa) {
        tarefaDAO.atualizar(tarefa);
    }

    // DELETE: remove uma tarefa do sistema pelo seu id
    public void remover(int id) {
        tarefaDAO.remover(id);
    }

    // ===== Ciclo de vida da tarefa =====

    // Inicia a tarefa: aplica a regra de negócio (método de domínio)
    // e persiste a mudança de estado no banco.
    public void iniciar(Tarefa tarefa) {
        tarefa.iniciar();
        tarefaDAO.atualizar(tarefa);
    }

    // Conclui a tarefa: aplica a regra de negócio e persiste a mudança.
    public void concluir(Tarefa tarefa) {
        tarefa.concluir();
        tarefaDAO.atualizar(tarefa);
    }

    // Reabre a tarefa: aplica a regra de negócio e persiste a mudança.
    public void reabrir(Tarefa tarefa) {
        tarefa.reabrir();
        tarefaDAO.atualizar(tarefa);
    }

    // ===== Gestão do responsável =====

    // Atribui um responsável à tarefa: aplica a regra de negócio
    // (o responsável precisa ser membro da equipe) e persiste a mudança.
    public void atribuirResponsavel(Tarefa tarefa, Usuario responsavel) {
        tarefa.atribuirResponsavel(responsavel);
        tarefaDAO.atualizar(tarefa);
    }

    // Remove o responsável da tarefa e persiste a mudança.
    public void removerResponsavel(Tarefa tarefa) {
        tarefa.removerResponsavel();
        tarefaDAO.atualizar(tarefa);
    }
}
