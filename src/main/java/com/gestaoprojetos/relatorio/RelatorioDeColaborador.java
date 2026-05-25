package com.gestaoprojetos.relatorio;

import java.util.List;

import com.gestaoprojetos.model.Usuario;
import com.gestaoprojetos.model.Tarefa;

// RelatorioDeColaborador: relatório do trabalho de um usuário
// (dados + tarefas sob sua responsabilidade).
public class RelatorioDeColaborador implements Relatorio {

    private final Usuario colaborador;
    private final List<Tarefa> tarefas;

    public RelatorioDeColaborador(Usuario colaborador, List<Tarefa> tarefas) {
        this.colaborador = colaborador;
        this.tarefas = tarefas;
    }

    @Override
    public String gerar() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(50)).append("\n");
        sb.append("RELATÓRIO DE COLABORADOR\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append("Nome: ").append(this.colaborador.getNome()).append("\n");
        sb.append("Login: ").append(this.colaborador.getLogin()).append("\n");
        sb.append("Cargo: ").append(this.colaborador.getCargo()).append("\n");
        sb.append("Perfil: ").append(this.colaborador.getClass().getSimpleName()).append("\n");
        sb.append("E-mail: ").append(this.colaborador.getEmail()).append("\n");

        // Tarefas sob responsabilidade
        sb.append("\n--- Tarefas sob responsabilidade (").append(this.tarefas.size()).append(") ---\n");
        for (Tarefa t : this.tarefas) {
            sb.append("- [").append(t.getStatus()).append("] ")
              .append(t.getTitulo())
              .append(" | Equipe: ").append(t.getEquipe().getNome())
              .append(" | Prioridade: ").append(t.getPrioridade())
              .append("\n");
        }

        // Linha de progresso (% de tarefas concluídas) — helper da interface
        sb.append("\n").append(Relatorio.progressoTarefas(this.tarefas)).append("\n");

        sb.append("=".repeat(50)).append("\n");
        return sb.toString();
    }

    @Override
    public void exportar(String formato) {
        ExportadorTxt.salvar(gerar(), "relatorio_colaborador_" + this.colaborador.getId(), formato);
    }
}
