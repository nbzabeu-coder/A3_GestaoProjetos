// Package: organiza a classe dentro do pacote view (camada de apresentação)
package com.gestaoprojetos.view;

// Importando classes do Swing
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;

// Importando layouts e tipos de janela/modalidade
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.Dialog;
import java.awt.Font;

// Importando data e a exceção de parsing
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

// Importando coleções
import java.util.List;

// Importando os Controllers
import com.gestaoprojetos.controller.TarefaController;
import com.gestaoprojetos.controller.ProjetoController;
import com.gestaoprojetos.controller.EquipeController;

// Importando os modelos
import com.gestaoprojetos.model.Tarefa;
import com.gestaoprojetos.model.Equipe;
import com.gestaoprojetos.model.Usuario;
import com.gestaoprojetos.model.Administrador;
import com.gestaoprojetos.model.Gerente;
import com.gestaoprojetos.model.Prioridade;
import com.gestaoprojetos.model.StatusTarefa;

// Importando a base das exceções customizadas
import com.gestaoprojetos.exception.GestaoProjetosException;

// TelaDetalheTarefa: JDialog MODAL pra criar ou editar uma tarefa.
// Estrutura em 2 abas:
//   - "Dados": formulário (título, datas, prioridade, equipe, responsável, descrição)
//              + Editar/Salvar (lock-to-edit)
//   - "Ciclo de vida": status atual + Iniciar/Concluir/Reabrir
// Separar as duas é proposital: editar DADOS é uma ação; mudar STATUS é outra.
public class TelaDetalheTarefa extends JDialog {

    // ===== Atributos =====
    private JTextField campoTitulo;
    private JTextField campoDataTermino;          // AAAA-MM-DD
    private JComboBox<Prioridade> comboPrioridade;
    private JComboBox<Equipe> comboEquipe;
    // Responsável: combo "misto" — item 0 é a String "(nenhum)", os demais são Usuario.
    private JComboBox<Object> comboResponsavel;
    private JTextArea campoDescricao;
    private JLabel labelStatus;                   // exibido na aba Ciclo de vida

    // Botões de edição (lock-to-edit) e de ciclo de vida
    private JButton botaoEditar;
    private JButton botaoSalvar;
    private JButton botaoIniciar;
    private JButton botaoConcluir;
    private JButton botaoReabrir;

    // Controllers
    private TarefaController controller;
    private ProjetoController projetoController;
    private EquipeController equipeController;

    // Contexto
    private int projetoId;          // projeto dono da tarefa
    private Tarefa tarefaEmEdicao;  // null = criar; existente = editar
    private Usuario usuarioLogado;

    // Permissões:
    // - podeEditarDados: editar título/datas/etc (Administrador/Gerente)
    // - podeMudarStatus: usar o ciclo de vida (Admin/Gerente sempre; Colaborador
    //   só se participa do projeto da tarefa)
    private boolean podeEditarDados;
    private boolean podeMudarStatus;

    // ===== Construtor (modo CRIAR) =====
    public TelaDetalheTarefa(Window pai, int projetoId, Usuario usuarioLogado) {
        super(pai, Dialog.ModalityType.APPLICATION_MODAL);
        this.projetoId = projetoId;
        this.tarefaEmEdicao = null;
        this.usuarioLogado = usuarioLogado;
        inicializar();
    }

    // ===== Construtor (modo EDITAR) =====
    public TelaDetalheTarefa(Window pai, Tarefa tarefa, Usuario usuarioLogado) {
        super(pai, Dialog.ModalityType.APPLICATION_MODAL);
        this.tarefaEmEdicao = tarefa;
        this.projetoId = tarefa.getProjetoId();
        this.usuarioLogado = usuarioLogado;
        inicializar();
    }

    // Setup comum aos dois modos
    private void inicializar() {
        this.controller = new TarefaController();
        this.projetoController = new ProjetoController();
        this.equipeController = new EquipeController();

        // Permissões: Administrador/Gerente editam tudo. Colaborador só muda o
        // STATUS, e somente de tarefas de projetos que ele participa.
        this.podeEditarDados = (this.usuarioLogado instanceof Administrador)
                || (this.usuarioLogado instanceof Gerente);
        // Short-circuit: a query de participação só roda pra Colaborador
        this.podeMudarStatus = this.podeEditarDados
                || this.projetoController.listarPorParticipante(this.usuarioLogado.getId())
                        .stream().anyMatch(p -> p.getId() == this.projetoId);

        boolean modoEditar = (this.tarefaEmEdicao != null);
        setTitle(modoEditar ? "Editar Tarefa" : "Nova Tarefa");
        setSize(540, 520);
        setLocationRelativeTo(getOwner()); // centraliza relativo à janela-mãe
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // CENTER: 2 abas — Dados (form) e Ciclo de vida (status)
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Dados", construirAbaDados());
        abas.addTab("Ciclo de vida", construirAbaCicloVida());
        add(abas, BorderLayout.CENTER);

        // SOUTH: rodapé só com Fechar
        add(construirRodape(), BorderLayout.SOUTH);

        // Popula o combo de responsáveis com os membros da equipe selecionada
        atualizarResponsaveis();

        if (modoEditar) {
            preencherCampos(this.tarefaEmEdicao);
            definirEdicaoHabilitada(false); // abre travado; Editar libera
            atualizarStatusTarefa();        // habilita os botões de status conforme estado
            // PERMISSÃO: Colaborador não edita os DADOS da tarefa — esconde o Editar
            // (a aba Ciclo de vida continua disponível conforme podeMudarStatus)
            if (!this.podeEditarDados) {
                this.botaoEditar.setVisible(false);
            }
        } else {
            // Criar: campos liberados, sem botão Editar
            this.botaoEditar.setVisible(false);
        }
    }

    // ===== Aba "Dados" (formulário + Editar/Salvar) =====
    private JPanel construirAbaDados() {
        JPanel painel = new JPanel(new BorderLayout(5, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        // NORTH: campos de uma linha em grade
        JPanel grade = new JPanel(new GridLayout(5, 2, 5, 8));

        this.campoTitulo = new JTextField();
        this.campoDataTermino = new JTextField();
        this.comboPrioridade = new JComboBox<>(Prioridade.values()); // BAIXA/MEDIA/ALTA
        this.comboEquipe = new JComboBox<>();
        this.comboResponsavel = new JComboBox<>();

        // Popula o combo de equipes (mode-aware) ANTES de registrar o listener
        carregarEquipes();
        this.comboEquipe.addActionListener(e -> atualizarResponsaveis());

        grade.add(criarLabel("Título:"));
        grade.add(this.campoTitulo);
        grade.add(criarLabel("Término previsto (AAAA-MM-DD):"));
        grade.add(this.campoDataTermino);
        grade.add(criarLabel("Prioridade:"));
        grade.add(this.comboPrioridade);
        grade.add(criarLabel("Equipe:"));
        grade.add(this.comboEquipe);
        grade.add(criarLabel("Responsável:"));
        grade.add(this.comboResponsavel);

        painel.add(grade, BorderLayout.NORTH);

        // CENTER: descrição
        JPanel painelDesc = new JPanel(new BorderLayout(5, 5));
        painelDesc.add(new JLabel("Descrição:"), BorderLayout.NORTH);
        this.campoDescricao = new JTextArea(4, 20);
        this.campoDescricao.setLineWrap(true);
        this.campoDescricao.setWrapStyleWord(true);
        painelDesc.add(new JScrollPane(this.campoDescricao), BorderLayout.CENTER);
        painel.add(painelDesc, BorderLayout.CENTER);

        // SOUTH: ações dos Dados (Editar/Salvar)
        painel.add(construirAcoesDados(), BorderLayout.SOUTH);

        return painel;
    }

    private JLabel criarLabel(String texto) {
        return new JLabel(texto, SwingConstants.RIGHT);
    }

    // Botões de ação dos Dados (Editar/Salvar), dentro da aba Dados
    private JPanel construirAcoesDados() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        this.botaoEditar = new JButton("Editar");
        this.botaoSalvar = new JButton("Salvar");
        this.botaoEditar.addActionListener(e -> definirEdicaoHabilitada(true));
        this.botaoSalvar.addActionListener(e -> salvar());
        p.add(this.botaoEditar);
        p.add(this.botaoSalvar);
        return p;
    }

    // ===== Aba "Ciclo de vida" (status + transições) =====
    private JPanel construirAbaCicloVida() {
        JPanel painel = new JPanel(new BorderLayout());

        // Modo criar: a tarefa ainda não existe (nasce PENDENTE ao salvar)
        if (this.tarefaEmEdicao == null) {
            JLabel aviso = new JLabel(
                    "Salve a tarefa primeiro para gerenciar o status.",
                    SwingConstants.CENTER);
            painel.add(aviso, BorderLayout.CENTER);
            return painel;
        }

        // NORTH: status atual em destaque
        JPanel painelStatus = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 25));
        painelStatus.add(new JLabel("Status atual:"));
        this.labelStatus = new JLabel();
        this.labelStatus.setFont(this.labelStatus.getFont().deriveFont(Font.BOLD, 16f));
        painelStatus.add(this.labelStatus);
        painel.add(painelStatus, BorderLayout.NORTH);

        // CENTER: botões de transição
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        this.botaoIniciar = new JButton("Iniciar");
        this.botaoConcluir = new JButton("Concluir");
        this.botaoReabrir = new JButton("Reabrir");

        this.botaoIniciar.addActionListener(e -> executarCicloVida(
                () -> this.controller.iniciar(this.tarefaEmEdicao), "Tarefa iniciada!"));
        this.botaoConcluir.addActionListener(e -> executarCicloVida(
                () -> this.controller.concluir(this.tarefaEmEdicao), "Tarefa concluída!"));
        this.botaoReabrir.addActionListener(e -> executarCicloVida(
                () -> this.controller.reabrir(this.tarefaEmEdicao), "Tarefa reaberta!"));

        painelBotoes.add(this.botaoIniciar);
        painelBotoes.add(this.botaoConcluir);
        painelBotoes.add(this.botaoReabrir);
        painel.add(painelBotoes, BorderLayout.CENTER);

        return painel;
    }

    // Executa uma transição de ciclo de vida, atualiza o status e trata erros.
    private void executarCicloVida(Runnable acao, String mensagemSucesso) {
        try {
            acao.run();
            atualizarStatusTarefa();
            JOptionPane.showMessageDialog(this, mensagemSucesso, "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (GestaoProjetosException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Operação inválida",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Atualiza o label de status e habilita os botões conforme as transições válidas.
    // PENDENTE → Iniciar | EM_ANDAMENTO → Concluir | CONCLUIDA → Reabrir
    private void atualizarStatusTarefa() {
        StatusTarefa status = this.tarefaEmEdicao.getStatus();
        this.labelStatus.setText(status.toString());
        // Só habilita a transição se o usuário pode mudar status (admin/gerente,
        // ou colaborador que participa do projeto) E o status atual permite.
        this.botaoIniciar.setEnabled(this.podeMudarStatus && status == StatusTarefa.PENDENTE);
        this.botaoConcluir.setEnabled(this.podeMudarStatus && status == StatusTarefa.EM_ANDAMENTO);
        this.botaoReabrir.setEnabled(this.podeMudarStatus && status == StatusTarefa.CONCLUIDA);
    }

    // ===== Rodapé: só "Fechar" =====
    private JPanel construirRodape() {
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton botaoFechar = new JButton("Fechar");
        botaoFechar.addActionListener(e -> dispose());
        painelBotoes.add(botaoFechar);
        return painelBotoes;
    }

    // Popula o combo de equipes com TODAS as equipes alocadas ao projeto.
    // Em modo editar, pré-seleciona a equipe atual da tarefa — o usuário pode
    // trocar pra outra equipe do mesmo projeto (ex: responsabilidade migrou).
    private void carregarEquipes() {
        for (Equipe eq : this.projetoController.listarEquipes(this.projetoId)) {
            this.comboEquipe.addItem(eq);
        }
        if (this.tarefaEmEdicao != null) {
            // Seleciona a equipe atual da tarefa (compara por id, pois os objetos
            // do combo vêm de uma consulta nova — não são o mesmo "this.equipe")
            int idAtual = this.tarefaEmEdicao.getEquipe().getId();
            for (int i = 0; i < this.comboEquipe.getItemCount(); i++) {
                if (this.comboEquipe.getItemAt(i).getId() == idAtual) {
                    this.comboEquipe.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    // Recarrega o combo de responsáveis com os membros da equipe selecionada.
    // Também popula o objeto Equipe em memória, porque o domínio
    // (Tarefa.atribuirResponsavel) valida contra equipe.listarMembros().
    private void atualizarResponsaveis() {
        this.comboResponsavel.removeAllItems();
        this.comboResponsavel.addItem("(nenhum)"); // sentinel: sem responsável

        Equipe equipe = (Equipe) this.comboEquipe.getSelectedItem();
        if (equipe == null) {
            return; // projeto sem equipes alocadas
        }

        List<Usuario> membros = this.equipeController.listarMembros(equipe.getId());

        if (equipe.listarMembros().isEmpty()) {
            for (Usuario m : membros) {
                equipe.adicionarMembro(m);
            }
        }

        for (Usuario m : membros) {
            this.comboResponsavel.addItem(m);
        }
    }

    // Alterna visualização/edição de todos os campos mutáveis, inclusive a
    // equipe (que pode ser trocada por outra alocada ao mesmo projeto).
    private void definirEdicaoHabilitada(boolean habilitada) {
        this.campoTitulo.setEnabled(habilitada);
        this.campoDataTermino.setEnabled(habilitada);
        this.comboPrioridade.setEnabled(habilitada);
        this.comboEquipe.setEnabled(habilitada);
        this.comboResponsavel.setEnabled(habilitada);
        this.campoDescricao.setEnabled(habilitada);
        this.botaoSalvar.setEnabled(habilitada);
        this.botaoEditar.setEnabled(!habilitada);
    }

    // ===== Preenche o form (modo editar) =====
    private void preencherCampos(Tarefa t) {
        this.campoTitulo.setText(t.getTitulo());
        this.campoDescricao.setText(t.getDescricao() != null ? t.getDescricao() : "");
        this.campoDataTermino.setText(t.getDataTerminoPrevista().toString());
        this.comboPrioridade.setSelectedItem(t.getPrioridade());
        selecionarResponsavel(t.getResponsavel());
    }

    // Seleciona no combo o responsável atual (por id), ou "(nenhum)" se for null.
    private void selecionarResponsavel(Usuario resp) {
        if (resp == null) {
            this.comboResponsavel.setSelectedIndex(0); // "(nenhum)"
            return;
        }
        for (int i = 0; i < this.comboResponsavel.getItemCount(); i++) {
            Object item = this.comboResponsavel.getItemAt(i);
            if (item instanceof Usuario u && u.getId() == resp.getId()) {
                this.comboResponsavel.setSelectedIndex(i);
                return;
            }
        }
    }

    // ===== Ação do Salvar =====
    private void salvar() {
        try {
            String titulo = this.campoTitulo.getText();
            String descricao = this.campoDescricao.getText();
            LocalDate dataTermino = LocalDate.parse(this.campoDataTermino.getText().trim());
            Prioridade prioridade = (Prioridade) this.comboPrioridade.getSelectedItem();
            Equipe equipe = (Equipe) this.comboEquipe.getSelectedItem();
            Object respSelecionado = this.comboResponsavel.getSelectedItem();

            if (equipe == null) {
                JOptionPane.showMessageDialog(this,
                        "Este projeto não tem equipes alocadas. Aloque uma equipe ao projeto primeiro.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (this.tarefaEmEdicao == null) {
                // ===== MODO CRIAR =====
                Tarefa nova = new Tarefa(titulo, dataTermino, prioridade, equipe, this.projetoId);
                nova.setDescricao(descricao);
                if (respSelecionado instanceof Usuario u) {
                    nova.atribuirResponsavel(u);
                }
                this.controller.cadastrar(nova);
                JOptionPane.showMessageDialog(this,
                        "Tarefa cadastrada com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // ===== MODO EDITAR =====
                this.tarefaEmEdicao.setTitulo(titulo);
                this.tarefaEmEdicao.setDescricao(descricao);
                this.tarefaEmEdicao.setDataTerminoPrevista(dataTermino);
                this.tarefaEmEdicao.setPrioridade(prioridade);
                // Equipe pode ter mudado — aplica antes do responsável pois
                // a validação de responsável depende dos membros da equipe.
                this.tarefaEmEdicao.setEquipe(equipe);
                if (respSelecionado instanceof Usuario u) {
                    this.tarefaEmEdicao.atribuirResponsavel(u);
                } else {
                    this.tarefaEmEdicao.removerResponsavel();
                }
                this.controller.atualizar(this.tarefaEmEdicao);
                JOptionPane.showMessageDialog(this,
                        "Tarefa atualizada com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Data inválida. Use o formato AAAA-MM-DD (ex: 2026-08-15).",
                    "Erro de validação",
                    JOptionPane.ERROR_MESSAGE);
        } catch (GestaoProjetosException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Erro de validação",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
