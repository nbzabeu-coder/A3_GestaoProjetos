// Package: organiza a classe dentro da estrutura de pastas do projeto 
// (domínio reverso)
package com.gestaoprojetos.controller;

// Import de coleções (List, para retornar listas de projetos e de equipes)
import java.util.List;

// Imports das classes do projeto (DAO de persistência e entidades)
import com.gestaoprojetos.dao.ProjetoDAO;
import com.gestaoprojetos.model.Equipe;
import com.gestaoprojetos.model.Projeto;

// Controller da entidade Projeto: camada intermediária do padrão MVC.
// Recebe as ações vindas da View, orquestra as chamadas ao ProjetoDAO
// (e aos métodos de negócio do domínio) e devolve as respostas adequadas.
public class ProjetoController {

    // Atributo: instância do DAO usada para acessar a persistência de projetos
    private ProjetoDAO projetoDAO = new ProjetoDAO();

    // CREATE: cadastra um novo projeto no sistema (delega a inserção ao DAO)
    public void cadastrar(Projeto projeto) {
        projetoDAO.inserir(projeto);
    }

    // READ: busca um projeto pelo seu id, retornando null se não encontrar
    public Projeto buscarPorId(int id) {
        return projetoDAO.buscarPorId(id);
    }

    // READ: lista todos os projetos cadastrados no sistema
    public List<Projeto> listarTodos() {
        return projetoDAO.listarTodos();
    }

    // UPDATE: atualiza os dados de um projeto existente
    public void atualizar(Projeto projeto) {
        projetoDAO.atualizar(projeto);
    }

    // DELETE: remove um projeto do sistema pelo seu id
    public void remover(int id) {
        projetoDAO.remover(id);
    }

    // ===== Ciclo de vida do projeto =====

    // Inicia o projeto: aplica a regra de negócio (método de domínio)
    // e persiste a mudança de estado no banco.
    public void iniciar(Projeto projeto) {
        projeto.iniciar();
        projetoDAO.atualizar(projeto);
    }

    // Conclui o projeto: aplica a regra de negócio e persiste a mudança.
    public void concluir(Projeto projeto) {
        projeto.concluir();
        projetoDAO.atualizar(projeto);
    }

    // Cancela o projeto: aplica a regra de negócio e persiste a mudança.
    public void cancelar(Projeto projeto) {
        projeto.cancelar();
        projetoDAO.atualizar(projeto);
    }

    // ===== Gestão de equipes alocadas (relação N:N) =====

    // Aloca uma equipe ao projeto
    public void alocarEquipe(int projetoId, int equipeId) {
        projetoDAO.alocarEquipe(projetoId, equipeId);
    }

    // Desaloca uma equipe do projeto
    public void desalocarEquipe(int projetoId, int equipeId) {
        projetoDAO.desalocarEquipe(projetoId, equipeId);
    }

    // Lista todas as equipes alocadas a um projeto
    public List<Equipe> listarEquipes(int projetoId) {
        return projetoDAO.listarEquipes(projetoId);
    }
}
