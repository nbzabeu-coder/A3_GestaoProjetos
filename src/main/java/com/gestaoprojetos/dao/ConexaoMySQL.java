package com.gestaoprojetos.dao;

// Exceção lançada quando há erro de leitura/escrita em arquivos ou streams
import java.io.IOException;
// Representa um fluxo de bytes pra leitura (usado pra ler o config.properties)
import java.io.InputStream;
// Interface JDBC que representa uma conexão aberta com o banco de dados
import java.sql.Connection;
// Classe utilitária do JDBC que sabe abrir conexões com bancos
import java.sql.DriverManager;
// Exceção lançada quando há erro relacionado ao banco de dados
import java.sql.SQLException;
// Classe pra ler/manipular arquivos no formato chave=valor (.properties)
import java.util.Properties;

// Classe utilitária que gerencia a conexão com o banco MySQL.
// As credenciais são lidas de config.properties (em src/main/resources).
public class ConexaoMySQL {

    // Carrega o config.properties uma única vez quando a classe é usada
    private static final Properties CONFIG = carregarConfig();

    // Lê o arquivo config.properties que está em src/main/resources
    private static Properties carregarConfig() {
        Properties props = new Properties();
        try (InputStream in = ConexaoMySQL.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                throw new RuntimeException(
                    "Arquivo config.properties não encontrado. " +
                    "Copie config.example.properties para config.properties e configure suas credenciais.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar config.properties", e);
        }
        return props;
    }

    // Abre uma nova conexão com o banco
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            CONFIG.getProperty("db.url"),
            CONFIG.getProperty("db.user"),
            CONFIG.getProperty("db.password")
        );
    }
}
