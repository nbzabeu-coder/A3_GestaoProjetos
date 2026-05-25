// Package: pacote dedicado aos relatórios (separa essa responsabilidade do model)
package com.gestaoprojetos.relatorio;

// Imports usados pelo helper estático de distribuição por status
import java.util.List;
import com.gestaoprojetos.model.Tarefa;

// Interface Relatorio: o "contrato" que todo relatório do sistema deve cumprir.
// Cada tipo de relatório (projeto, equipe, colaborador) implementa esses 2 métodos
// do seu próprio jeito — isso é POLIMORFISMO: tratamos todos como "Relatorio",
// mas cada um se monta diferente.
public interface Relatorio {

    // Monta o conteúdo do relatório como texto e devolve
    String gerar();

    // Exporta o relatório pra um arquivo no formato indicado (ex: "txt")
    void exportar(String formato);

    // Helper estático compartilhado: monta a distribuição das tarefas POR STATUS
    // (quantas e qual a % de cada status: pendentes, em andamento, concluídas).
    // Como os 3 relatórios têm uma lista de tarefas, deixar o cálculo aqui evita
    // repetir a mesma conta em cada um (princípio DRY). Método estático em
    // interface: pertence à interface, não a um objeto — chama-se
    // Relatorio.distribuicaoPorStatus(...). Devolve 3 linhas já formatadas
    // (com indentação) — quem chama coloca o cabeçalho que quiser.
    static String distribuicaoPorStatus(List<Tarefa> tarefas) {
        int total = tarefas.size();

        // Conta quantas tarefas há em cada status (switch sobre o enum)
        int pendentes = 0, emAndamento = 0, concluidas = 0;
        for (Tarefa t : tarefas) {
            switch (t.getStatus()) {
                case PENDENTE -> pendentes++;
                case EM_ANDAMENTO -> emAndamento++;
                case CONCLUIDA -> concluidas++;
            }
        }

        // % de cada status = (parte / total) * 100, com guarda de divisão por zero
        double pctPend = (total == 0) ? 0.0 : (pendentes * 100.0) / total;
        double pctAnd  = (total == 0) ? 0.0 : (emAndamento * 100.0) / total;
        double pctConc = (total == 0) ? 0.0 : (concluidas * 100.0) / total;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("   Pendentes:    %d (%.1f%%)\n", pendentes, pctPend));
        sb.append(String.format("   Em andamento: %d (%.1f%%)\n", emAndamento, pctAnd));
        sb.append(String.format("   Concluídas:   %d (%.1f%%)\n", concluidas, pctConc));
        return sb.toString();
    }
}
