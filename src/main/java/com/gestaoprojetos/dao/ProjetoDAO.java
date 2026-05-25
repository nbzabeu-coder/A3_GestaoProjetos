package com.gestaoprojetos.dao;

// Imports do JDBC
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
// Imports de coleções
import java.util.ArrayList;
import java.util.List;

// Imports das classes de domínio
import com.gestaoprojetos.model.Equipe;
import com.gestaoprojetos.model.Gerente;
import com.gestaoprojetos.model.Projeto;
import com.gestaoprojetos.model.StatusProjeto;
import com.gestaoprojetos.model.Usuario;

// DAO responsável pela persistência de Projeto no banco MySQL.
// Além do CRUD básico, gerencia a relação N:N com Equipe (via tabela
// auxiliar "projeto_equipe"). As tarefas do projeto são gerenciadas
// pelo TarefaDAO (relação 1:N via FK "projeto_id" na tabela tarefa).
public class ProjetoDAO {

    // CREATE: insere um novo projeto no banco
    public void inserir(Projeto projeto) {
        String sql = "INSERT INTO projeto (nome, descricao, data_inicio_prevista, "
                + "data_inicio_real, data_termino_prevista, data_termino_real, "
                + "status, gerente_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, projeto.getNome());
            ps.setString(2, projeto.getDescricao());
            ps.setObject(3, projeto.getDataInicioPrevista());
            ps.setObject(4, projeto.getDataInicioReal());
            ps.setObject(5, projeto.getDataTerminoPrevista());
            ps.setObject(6, projeto.getDataTerminoReal());
            ps.setString(7, projeto.getStatus().name());
            ps.setInt(8, projeto.getGerente().getId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    projeto.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir projeto", e);
        }
    }

    // READ: busca um projeto pelo id (com gerente carregado),
    // retornando null se não encontrar
    public Projeto buscarPorId(int id) {
        String sql = "SELECT * FROM projeto WHERE id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montarProjeto(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar projeto por id", e);
        }
    }

    // READ: lista todos os projetos do banco
    public List<Projeto> listarTodos() {
        String sql = "SELECT * FROM projeto";
        List<Projeto> projetos = new ArrayList<>();

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                projetos.add(montarProjeto(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar projetos", e);
        }

        return projetos;
    }

    // READ: lista os projetos em que um usuário PARTICIPA — ou seja, projetos
    // que têm alguma equipe da qual o usuário é membro.
    // JOIN duplo: projeto -> projeto_equipe -> equipe_membro.
    // DISTINCT porque o usuário pode estar em várias equipes do mesmo projeto.
    public List<Projeto> listarPorParticipante(int usuarioId) {
        String sql = "SELECT DISTINCT p.* FROM projeto p "
                + "JOIN projeto_equipe pe ON p.id = pe.projeto_id "
                + "JOIN equipe_membro em ON pe.equipe_id = em.equipe_id "
                + "WHERE em.usuario_id = ?";
        List<Projeto> projetos = new ArrayList<>();

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    projetos.add(montarProjeto(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar projetos do participante", e);
        }

        return projetos;
    }

    // UPDATE: atualiza os dados de um projeto existente
    public void atualizar(Projeto projeto) {
        String sql = "UPDATE projeto SET nome = ?, descricao = ?, "
                + "data_inicio_prevista = ?, data_inicio_real = ?, "
                + "data_termino_prevista = ?, data_termino_real = ?, "
                + "status = ?, gerente_id = ? WHERE id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, projeto.getNome());
            ps.setString(2, projeto.getDescricao());
            ps.setObject(3, projeto.getDataInicioPrevista());
            ps.setObject(4, projeto.getDataInicioReal());
            ps.setObject(5, projeto.getDataTerminoPrevista());
            ps.setObject(6, projeto.getDataTerminoReal());
            ps.setString(7, projeto.getStatus().name());
            ps.setInt(8, projeto.getGerente().getId());
            ps.setInt(9, projeto.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar projeto", e);
        }
    }

    // DELETE: remove um projeto pelo id
    public void remover(int id) {
        String sql = "DELETE FROM projeto WHERE id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover projeto", e);
        }
    }

    // ===== Gestão de equipes alocadas (relação N:N) =====

    // Aloca uma equipe ao projeto (insere na projeto_equipe)
    public void alocarEquipe(int projetoId, int equipeId) {
        String sql = "INSERT INTO projeto_equipe (projeto_id, equipe_id) VALUES (?, ?)";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projetoId);
            ps.setInt(2, equipeId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao alocar equipe ao projeto", e);
        }
    }

    // Desaloca uma equipe do projeto (remove da projeto_equipe)
    public void desalocarEquipe(int projetoId, int equipeId) {
        String sql = "DELETE FROM projeto_equipe WHERE projeto_id = ? AND equipe_id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projetoId);
            ps.setInt(2, equipeId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desalocar equipe do projeto", e);
        }
    }

    // Lista todas as equipes alocadas a um projeto
    public List<Equipe> listarEquipes(int projetoId) {
        String sql = "SELECT equipe_id FROM projeto_equipe WHERE projeto_id = ?";
        List<Equipe> equipes = new ArrayList<>();
        EquipeDAO equipeDAO = new EquipeDAO();

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projetoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int equipeId = rs.getInt("equipe_id");
                    Equipe equipe = equipeDAO.buscarPorId(equipeId);
                    if (equipe != null) {
                        equipes.add(equipe);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar equipes do projeto", e);
        }

        return equipes;
    }

    // Método auxiliar (privado): monta um objeto Projeto a partir de uma
    // linha do ResultSet, carregando o gerente via UsuarioDAO.
    private Projeto montarProjeto(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String descricao = rs.getString("descricao");
        LocalDate dataInicioPrevista = rs.getObject("data_inicio_prevista", LocalDate.class);
        LocalDate dataInicioReal = rs.getObject("data_inicio_real", LocalDate.class);
        LocalDate dataTerminoPrevista = rs.getObject("data_termino_prevista", LocalDate.class);
        LocalDate dataTerminoReal = rs.getObject("data_termino_real", LocalDate.class);
        StatusProjeto status = StatusProjeto.valueOf(rs.getString("status"));
        int gerenteId = rs.getInt("gerente_id");

        // Carrega o gerente pelo id, usando o UsuarioDAO
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        Usuario usuario = usuarioDAO.buscarPorId(gerenteId);

        // O gerente_id sempre aponta para um Gerente — verificação defensiva
        if (!(usuario instanceof Gerente)) {
            throw new IllegalStateException(
                    "O gerente_id do projeto não aponta para um Gerente válido");
        }
        Gerente gerente = (Gerente) usuario;

        return new Projeto(id, nome, descricao, dataInicioPrevista, dataInicioReal,
                dataTerminoPrevista, dataTerminoReal, status, gerente);
    }

}
