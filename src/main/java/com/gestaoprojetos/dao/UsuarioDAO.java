package com.gestaoprojetos.dao;

// Imports do JDBC
import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Imports de coleções
import java.util.ArrayList;
import java.util.List;

// Imports das classes de domínio (estão em outro package, por isso precisa importar)
import com.gestaoprojetos.model.Administrador;
import com.gestaoprojetos.model.Colaborador;
import com.gestaoprojetos.model.Gerente;
import com.gestaoprojetos.model.Usuario;

// DAO responsável pela persistência de Usuario (e suas subclasses) no banco MySQL.
// Faz o mapeamento entre a hierarquia Java (Usuario abstract → Administrador/Gerente/Colaborador)
// e a tabela única "usuario" (com coluna "perfil" indicando o tipo de cada registro).
public class UsuarioDAO {

    // CREATE: insere um novo usuário no banco
    public void inserir(Usuario usuario) {
        // Define o comando SQL de inserção, com placeholders ("?") que
        // serão preenchidos com os valores reais nos passos seguintes
        String sql = "INSERT INTO usuario (nome, cpf, email, cargo, login, senha, perfil) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        // Abre a conexão com o banco (conn) e prepara o comando SQL (ps).
        // A bandeira RETURN_GENERATED_KEYS permite recuperar o id gerado pelo banco.
        // Tudo é fechado automaticamente ao final do bloco.
        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1. Detecta o tipo do usuário (subclasse)
            String perfil;
            if (usuario instanceof Administrador) {
                perfil = "ADMINISTRADOR";
            } else if (usuario instanceof Gerente) {
                perfil = "GERENTE";
            } else if (usuario instanceof Colaborador) {
                perfil = "COLABORADOR";
            } else {
                throw new IllegalArgumentException("Tipo de usuário desconhecido");
            }

            // 2. Preenche os parâmetros do SQL (substitui os "?")
            // JDBC começa em 1, não em 0
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getCpf());
            ps.setString(3, usuario.getEmail());
            ps.setString(4, usuario.getCargo());
            ps.setString(5, usuario.getLogin());
            ps.setString(6, usuario.getSenha());
            ps.setString(7, perfil);

            // 3. Executa o INSERT
            ps.executeUpdate();

            // 4. Pega o id gerado pelo banco e seta no objeto
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir usuário", e);
        }
    }

    // READ: busca um usuário pelo id, retornando null se não encontrar
    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Preenche o "?" com o id procurado
            ps.setInt(1, id);

            // Executa a consulta e percorre o resultado (ResultSet)
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Encontrou — monta e devolve o objeto correspondente
                    return montarUsuario(rs);
                }
                // Não encontrou — retorna null
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por id", e);
        }
    }

    // READ: lista todos os usuários do banco
    public List<Usuario> listarTodos() {
        String sql = "SELECT * FROM usuario";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            // Percorre todas as linhas retornadas
            while (rs.next()) {
                usuarios.add(montarUsuario(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar usuários", e);
        }

        return usuarios;
    }

    // UPDATE: atualiza os dados de um usuário existente.
    // Atributos imutáveis (cpf, login, perfil) NÃO são alterados aqui — só
    // os que fazem sentido evoluir ao longo do tempo.
    public void atualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nome = ?, email = ?, cargo = ?, senha = ? WHERE id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Preenche os campos atualizáveis
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getCargo());
            ps.setString(4, usuario.getSenha());
            // O id na cláusula WHERE — identifica qual linha atualizar
            ps.setInt(5, usuario.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar usuário", e);
        }
    }

    // DELETE: remove um usuário pelo id
    public void remover(int id) {
        String sql = "DELETE FROM usuario WHERE id = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover usuário", e);
        }
    }

    // Busca auxiliar: encontra um usuário pelo login (útil pra fluxo de
    // autenticação)
    public Usuario buscarPorLogin(String login) {
        String sql = "SELECT * FROM usuario WHERE login = ?";

        try (Connection conn = ConexaoMySQL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, login);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return montarUsuario(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuário por login", e);
        }
    }

    // Método auxiliar (privado, uso interno): monta um objeto Usuario a partir
    // de uma linha do ResultSet, identificando a subclasse correta pelo campo "perfil".
    private Usuario montarUsuario(ResultSet rs) throws SQLException {
        // Lê os valores da linha atual do ResultSet
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String cpf = rs.getString("cpf");
        String email = rs.getString("email");
        String cargo = rs.getString("cargo");
        String login = rs.getString("login");
        String senha = rs.getString("senha");
        String perfil = rs.getString("perfil");

        // Instancia a subclasse correta com base no perfil
        Usuario usuario;
        switch (perfil) {
            case "ADMINISTRADOR":
                usuario = new Administrador(nome, cpf, email, cargo, login, senha);
                break;
            case "GERENTE":
                usuario = new Gerente(nome, cpf, email, cargo, login, senha);
                break;
            case "COLABORADOR":
                usuario = new Colaborador(nome, cpf, email, cargo, login, senha);
                break;
            default:
                throw new IllegalStateException("Perfil desconhecido no banco: " + perfil);
        }

        // Define o id (preenchido após a criação porque não está no construtor)
        usuario.setId(id);
        return usuario;
    }
}
