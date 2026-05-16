package com.gestaoprojetos.dao;

// Imports do JDBC
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

// Imports de data/hora
import java.time.LocalDate;
import java.time.LocalDateTime;

// Imports de coleções
import java.util.ArrayList;
import java.util.List;

// Imports das classes de domínio
import com.gestaoprojetos.model.Equipe;
import com.gestaoprojetos.model.Prioridade;
import com.gestaoprojetos.model.StatusTarefa;
import com.gestaoprojetos.model.Tarefa;
import com.gestaoprojetos.model.Usuario;

// DAO responsável pela persistência de Tarefa no banco MySQL.
// Cada tarefa pertence a um projeto (FK projeto_id) e a uma equipe (FK equipe_id);
// pode ter um responsável (FK responsavel_id, opcional).
public class TarefaDAO {

    // CREATE: insere uma nova tarefa no banco
    public void inserir(Tarefa tarefa) {
        String sql = "INSERT INTO tarefa (titulo, descricao, data_inicio_real, "
                + "data_termino_prevista, data_termino_real, status, prioridade, "
                + "projeto_id, equipe_id, responsavel_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tarefa.getTitulo());
            ps.setString(2, tarefa.getDescricao());
            ps.setObject(3, tarefa.getDataInicioReal());
            ps.setObject(4, tarefa.getDataTerminoPrevista());
            ps.setObject(5, tarefa.getDataTerminoReal());
            ps.setString(6, tarefa.getStatus().name());
            ps.setString(7, tarefa.getPrioridade().name());
            ps.setInt(8, tarefa.getProjetoId());
            ps.setInt(9, tarefa.getEquipe().getId());

            // responsavel_id pode ser null (responsável é opcional)
            if (tarefa.getResponsavel() != null) {
                ps.setInt(10, tarefa.getResponsavel().getId());
            } else {
                ps.setNull(10, Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tarefa.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir tarefa", e);
        }
    }

    // READ: busca uma tarefa pelo id, retornando null se não encontrar
    public Tarefa buscarPorId(int id) {
        String sql = "SELECT * FROM tarefa WHERE id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montarTarefa(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar tarefa por id", e);
        }
    }

    // READ: lista todas as tarefas do banco
    public List<Tarefa> listarTodos() {
        String sql = "SELECT * FROM tarefa";
        List<Tarefa> tarefas = new ArrayList<>();

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tarefas.add(montarTarefa(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar tarefas", e);
        }

        return tarefas;
    }

    // READ: lista as tarefas de um projeto específico
    public List<Tarefa> listarPorProjeto(int projetoId) {
        String sql = "SELECT * FROM tarefa WHERE projeto_id = ?";
        List<Tarefa> tarefas = new ArrayList<>();

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projetoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tarefas.add(montarTarefa(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar tarefas do projeto", e);
        }

        return tarefas;
    }

    // UPDATE: atualiza os dados de uma tarefa existente
    public void atualizar(Tarefa tarefa) {
        String sql = "UPDATE tarefa SET titulo = ?, descricao = ?, "
                + "data_inicio_real = ?, data_termino_prevista = ?, "
                + "data_termino_real = ?, status = ?, prioridade = ?, "
                + "responsavel_id = ? WHERE id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tarefa.getTitulo());
            ps.setString(2, tarefa.getDescricao());
            ps.setObject(3, tarefa.getDataInicioReal());
            ps.setObject(4, tarefa.getDataTerminoPrevista());
            ps.setObject(5, tarefa.getDataTerminoReal());
            ps.setString(6, tarefa.getStatus().name());
            ps.setString(7, tarefa.getPrioridade().name());

            // responsavel_id pode ser null (responsável é opcional)
            if (tarefa.getResponsavel() != null) {
                ps.setInt(8, tarefa.getResponsavel().getId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }

            ps.setInt(9, tarefa.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar tarefa", e);
        }
    }

    // DELETE: remove uma tarefa pelo id
    public void remover(int id) {
        String sql = "DELETE FROM tarefa WHERE id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover tarefa", e);
        }
    }

    // Método auxiliar (privado): monta um objeto Tarefa a partir de uma linha
    // do ResultSet, carregando a equipe e o responsável via seus DAOs.
    private Tarefa montarTarefa(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String titulo = rs.getString("titulo");
        String descricao = rs.getString("descricao");
        LocalDateTime dataInicioReal = rs.getObject("data_inicio_real", LocalDateTime.class);
        LocalDateTime dataTerminoReal = rs.getObject("data_termino_real", LocalDateTime.class);
        LocalDate dataTerminoPrevista = rs.getObject("data_termino_prevista", LocalDate.class);
        StatusTarefa status = StatusTarefa.valueOf(rs.getString("status"));
        Prioridade prioridade = Prioridade.valueOf(rs.getString("prioridade"));
        int projetoId = rs.getInt("projeto_id");
        int equipeId = rs.getInt("equipe_id");
        int responsavelId = rs.getInt("responsavel_id");

        // Carrega a equipe (obrigatória)
        EquipeDAO equipeDAO = new EquipeDAO();
        Equipe equipe = equipeDAO.buscarPorId(equipeId);

        // Carrega o responsável (opcional — pode não existir)
        Usuario responsavel = null;
        if (responsavelId != 0) {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            responsavel = usuarioDAO.buscarPorId(responsavelId);
        }

        return new Tarefa(id, titulo, descricao, dataInicioReal, dataTerminoReal,
                dataTerminoPrevista, status, prioridade, equipe, responsavel, projetoId);
    }

}
