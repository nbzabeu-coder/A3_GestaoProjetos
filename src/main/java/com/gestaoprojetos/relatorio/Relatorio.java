// Package: pacote dedicado aos relatórios (separa essa responsabilidade do model)
package com.gestaoprojetos.relatorio;

// Imports usados pelo helper estático de progresso
import java.util.List;
import com.gestaoprojetos.model.Tarefa;
import com.gestaoprojetos.model.StatusTarefa;

// Interface Relatorio: o "contrato" que todo relatório do sistema deve cumprir.
// Cada tipo de relatório (projeto, equipe, colaborador) implementa esses 2 métodos
// do seu próprio jeito — isso é POLIMORFISMO: tratamos todos como "Relatorio",
// mas cada um se monta diferente.
public interface Relatorio {

    // Monta o conteúdo do relatório como texto e devolve
    String gerar();

    // Exporta o relatório pra um arquivo no formato indicado (ex: "txt")
    void exportar(String formato);

    // Helper estático compartilhado: monta a linha de progresso (% de tarefas
    // concluídas). Como os 3 relatórios têm uma lista de tarefas, deixar o
    // cálculo aqui evita repetir a mesma conta em cada um (princípio DRY).
    // Método estático em interface: pertence à interface, não a um objeto —
    // chama-se Relatorio.progressoTarefas(...).
    static String progressoTarefas(List<Tarefa> tarefas) {
        int total = tarefas.size();
        int concluidas = 0;
        for (Tarefa t : tarefas) {
            if (t.getStatus() == StatusTarefa.CONCLUIDA) {
                concluidas++;
            }
        }
        // Evita divisão por zero quando não há tarefas
        double pct = (total == 0) ? 0.0 : (concluidas * 100.0) / total;
        return String.format("Progresso: %d de %d tarefas concluídas (%.1f%%)",
                concluidas, total, pct);
    }
}
