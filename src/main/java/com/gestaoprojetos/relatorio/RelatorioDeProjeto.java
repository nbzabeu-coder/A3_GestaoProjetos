package com.gestaoprojetos.relatorio;

// Import de coleções
import java.util.List;

// Imports dos modelos que o relatório formata
import com.gestaoprojetos.model.Projeto;
import com.gestaoprojetos.model.Equipe;
import com.gestaoprojetos.model.Tarefa;

// RelatorioDeProjeto: implementação concreta de Relatorio pra um projeto.
// Opção A: recebe os dados PRONTOS (projeto + equipes + tarefas) — só formata,
// não acessa banco/controller.
public class RelatorioDeProjeto implements Relatorio {

    private final Projeto projeto;
    private final List<Equipe> equipes;
    private final List<Tarefa> tarefas;

    // Construtor recebe tudo que o relatório precisa, já buscado pela tela
    public RelatorioDeProjeto(Projeto projeto, List<Equipe> equipes, List<Tarefa> tarefas) {
        this.projeto = projeto;
        this.equipes = equipes;
        this.tarefas = tarefas;
    }

    // Monta o texto do relatório usando um StringBuilder (eficiente pra concatenar)
    @Override
    public String gerar() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(50)).append("\n");
        sb.append("RELATÓRIO DE PROJETO\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append("Projeto: ").append(this.projeto.getNome())
          .append(" (id ").append(this.projeto.getId()).append(")\n");
        sb.append("Status: ").append(this.projeto.getStatus()).append("\n");
        sb.append("Gerente: ").append(this.projeto.getGerente().getNome()).append("\n");
        sb.append("Início previsto: ").append(this.projeto.getDataInicioPrevista()).append("\n");
        sb.append("Término previsto: ").append(this.projeto.getDataTerminoPrevista()).append("\n");
        if (this.projeto.getDescricao() != null && !this.projeto.getDescricao().isBlank()) {
            sb.append("Descrição: ").append(this.projeto.getDescricao()).append("\n");
        }

        // Seção de equipes alocadas
        sb.append("\n--- Equipes alocadas (").append(this.equipes.size()).append(") ---\n");
        for (Equipe e : this.equipes) {
            sb.append("- ").append(e.getNome()).append("\n");
        }

        // Seção de tarefas
        sb.append("\n--- Tarefas (").append(this.tarefas.size()).append(") ---\n");
        for (Tarefa t : this.tarefas) {
            String responsavel = (t.getResponsavel() != null)
                    ? t.getResponsavel().getNome()
                    : "(sem responsável)";
            sb.append("- [").append(t.getStatus()).append("] ")
              .append(t.getTitulo())
              .append(" | Prioridade: ").append(t.getPrioridade())
              .append(" | Equipe: ").append(t.getEquipe().getNome())
              .append(" | Responsável: ").append(responsavel)
              .append("\n");
        }

        sb.append("=".repeat(50)).append("\n");
        return sb.toString();
    }

    // Exporta o relatório — delega a escrita pro helper compartilhado
    @Override
    public void exportar(String formato) {
        ExportadorTxt.salvar(gerar(), "relatorio_projeto_" + this.projeto.getId(), formato);
    }
}
