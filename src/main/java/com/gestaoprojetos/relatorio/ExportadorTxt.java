// Package: pacote dos relatórios
package com.gestaoprojetos.relatorio;

// Imports de I/O de arquivo
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// Helper de exportação: centraliza a escrita do relatório em arquivo, evitando
// duplicar essa lógica nos 3 relatórios (DRY — Don't Repeat Yourself).
// Salva sempre dentro da pasta "relatorios/" (criada se não existir).
public class ExportadorTxt {

    // Pasta de destino dos relatórios exportados (relativa à raiz do projeto)
    private static final String PASTA = "relatorios";

    // Construtor privado: esta classe é só um "porta de utilidades" estáticas,
    // não faz sentido instanciá-la.
    private ExportadorTxt() {
    }

    // Salva o conteúdo num arquivo dentro da pasta relatorios/.
    // - nomeBase: nome do arquivo sem extensão (ex: "relatorio_projeto_1")
    // - formato: a extensão (ex: "txt")
    // Retorna o caminho do arquivo criado (pra exibir ao usuário).
    public static String salvar(String conteudo, String nomeBase, String formato) {
        // Garante que a pasta exista
        File pasta = new File(PASTA);
        if (!pasta.exists()) {
            pasta.mkdirs();
        }

        // Monta o arquivo dentro da pasta
        File arquivo = new File(pasta, nomeBase + "." + formato.toLowerCase());

        // try-with-resources: o FileWriter fecha sozinho ao terminar
        try (FileWriter writer = new FileWriter(arquivo)) {
            writer.write(conteudo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao exportar relatório: " + e.getMessage(), e);
        }

        return arquivo.getPath(); // ex: "relatorios/relatorio_projeto_1.txt"
    }
}
