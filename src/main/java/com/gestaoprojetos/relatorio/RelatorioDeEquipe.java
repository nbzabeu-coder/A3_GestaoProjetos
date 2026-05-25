package com.gestaoprojetos.relatorio;

import java.util.List;

import com.gestaoprojetos.model.Equipe;
import com.gestaoprojetos.model.Usuario;
import com.gestaoprojetos.model.Tarefa;

// RelatorioDeEquipe: relatório de uma equipe (dados + membros + tarefas).
public class RelatorioDeEquipe implements Relatorio {

    private final Equipe equipe;
    private final List<Usuario> membros;
    private final List<Tarefa> tarefas;

    public RelatorioDeEquipe(Equipe equipe, List<Usuario> membros, List<Tarefa> tarefas) {
        this.equipe = equipe;
        this.membros = membros;
        this.tarefas = tarefas;
    }

    @Override
    public String gerar() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(50)).append("\n");
        sb.append("RELATÓRIO DE EQUIPE\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append("Equipe: ").append(this.equipe.getNome())
          .append(" (id ").append(this.equipe.getId()).append(")\n");
        if (this.equipe.getDescricao() != null && !this.equipe.getDescricao().isBlank()) {
            sb.append("Descrição: ").append(this.equipe.getDescricao()).append("\n");
        }

        // Membros
        sb.append("\n--- Membros (").append(this.membros.size()).append(") ---\n");
        for (Usuario m : this.membros) {
            sb.append("- ").append(m.getNome())
              .append(" (").append(m.getLogin()).append(") - ")
              .append(m.getClass().getSimpleName()) // perfil: Administrador/Gerente/Colaborador
              .append("\n");
        }

        // Tarefas
        sb.append("\n--- Tarefas da equipe (").append(this.tarefas.size()).append(") ---\n");
        for (Tarefa t : this.tarefas) {
            String responsavel = (t.getResponsavel() != null)
                    ? t.getResponsavel().getNome()
                    : "(sem responsável)";
            sb.append("- [").append(t.getStatus()).append("] ")
              .append(t.getTitulo())
              .append(" | Prioridade: ").append(t.getPrioridade())
              .append(" | Responsável: ").append(responsavel)
              .append("\n");
        }

        // Distribuição das tarefas por status (% de cada) — helper da interface
        sb.append("\n--- Tarefas por status ---\n");
        sb.append(Relatorio.distribuicaoPorStatus(this.tarefas));

        sb.append("=".repeat(50)).append("\n");
        return sb.toString();
    }

    @Override
    public void exportar(String formato) {
        ExportadorTxt.salvar(gerar(), "relatorio_equipe_" + this.equipe.getId(), formato);
    }
}
