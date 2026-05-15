package com.gestaoprojetos.dao;

// Imports do JDBC
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Imports de coleções
import java.util.ArrayList;
import java.util.List;

// Imports das classes de domínio
import com.gestaoprojetos.model.Equipe;
import com.gestaoprojetos.model.Usuario;

// DAO responsável pela persistência de Equipe no banco MySQL.
// Além do CRUD básico, gerencia a relação N:N entre Equipe e Usuario
// (via tabela auxiliar "equipe_membro").
public class EquipeDAO {

    // CREATE: insere uma nova equipe no banco
    public void inserir(Equipe equipe) {
        String sql = "INSERT INTO equipe (nome, descricao) VALUES (?, ?)";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Preenche os parâmetros do SQL
            ps.setString(1, equipe.getNome());
            ps.setString(2, equipe.getDescricao());

            // Executa o INSERT
            ps.executeUpdate();

            // Pega o id gerado pelo banco e seta no objeto
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    equipe.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir equipe", e);
        }
    }

    // READ: busca uma equipe pelo id, retornando null se não encontrar
    public Equipe buscarPorId(int id) {
        String sql = "SELECT * FROM equipe WHERE id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montarEquipe(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar equipe por id", e);
        }
    }

    // READ: lista todas as equipes do banco
    public List<Equipe> listarTodos() {
        String sql = "SELECT * FROM equipe";
        List<Equipe> equipes = new ArrayList<>();

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                equipes.add(montarEquipe(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar equipes", e);
        }

        return equipes;
    }

    // UPDATE: atualiza nome e descrição de uma equipe existente
    public void atualizar(Equipe equipe) {
        String sql = "UPDATE equipe SET nome = ?, descricao = ? WHERE id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, equipe.getNome());
            ps.setString(2, equipe.getDescricao());
            ps.setInt(3, equipe.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar equipe", e);
        }
    }

    // // DELETE: remove uma equipe pelo id
    public void remover(int id) {
        String sql = "DELETE FROM equipe WHERE id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover equipe", e);
        }
    }

    // ===== Gestão de membros (relação N:N) =====

    // Adiciona um usuário como membro de uma equipe (insere na equipe_membro)
    public void adicionarMembro(int equipeId, int usuarioId) {
        String sql = "INSERT INTO equipe_membro (equipe_id, usuario_id) VALUES (?, ?)";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, equipeId);
            ps.setInt(2, usuarioId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao adicionar membro à equipe", e);
        }
    }

    // Remove um usuário de uma equipe (remove da equipe_membro)
    public void removerMembro(int equipeId, int usuarioId) {
        String sql = "DELETE FROM equipe_membro WHERE equipe_id = ? AND usuario_id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, equipeId);
            ps.setInt(2, usuarioId);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover membro da equipe", e);
        }
    }

    // Lista os usuários que são membros de uma equipe específica
    public List<Usuario> listarMembros(int equipeId) {
        String sql = "SELECT usuario_id FROM equipe_membro WHERE equipe_id = ?";
        List<Usuario> membros = new ArrayList<>();
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, equipeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int usuarioId = rs.getInt("usuario_id");
                    Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
                    if (usuario != null) {
                        membros.add(usuario);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar membros da equipe", e);
        }

        return membros;
    }

    // Método auxiliar (privado): monta um objeto Equipe a partir de uma linha do
    // ResultSet
    private Equipe montarEquipe(ResultSet rs) throws SQLException {
        String nome = rs.getString("nome");
        String descricao = rs.getString("descricao");

        Equipe equipe = new Equipe(nome);
        equipe.setDescricao(descricao);
        equipe.setId(rs.getInt("id"));

        return equipe;
    }

}
