// Package: organiza a classe dentro do pacote view (camada de apresentação)
package com.gestaoprojetos.view;

// Importando classes do Swing
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

// Importando layouts
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;

// Importando data e a exceção de parsing de data
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

// Importando coleções (lista de candidatas a alocação)
import java.util.ArrayList;
import java.util.List;

// Importando os Controllers
import com.gestaoprojetos.controller.ProjetoController;
import com.gestaoprojetos.controller.UsuarioController;
import com.gestaoprojetos.controller.EquipeController;
import com.gestaoprojetos.controller.TarefaController;

// Importando os modelos
import com.gestaoprojetos.model.Projeto;
import com.gestaoprojetos.model.Usuario;
import com.gestaoprojetos.model.Gerente;
import com.gestaoprojetos.model.Administrador;
import com.gestaoprojetos.model.StatusProjeto;
import com.gestaoprojetos.model.Equipe;
import com.gestaoprojetos.model.Tarefa;

// Importando a base das exceções customizadas
import com.gestaoprojetos.exception.GestaoProjetosException;

// TelaProjeto: JDialog MODAL pra criar ou editar um projeto.
// É a tela mais rica do sistema: além dos dados, tem ciclo de vida
// (Iniciar/Concluir/Cancelar — Fase B), equipes alocadas (Fase C) e
// tarefas (Fase D). Esta Fase A cobre a aba Dados.
public class TelaProjeto extends JDialog {

    // ===== Atributos =====
    // Campos do formulário (Aba Dados)
    private JTextField campoNome;
    private JTextArea campoDescricao;
    private JTextField campoDataInicio;   // formato AAAA-MM-DD
    private JTextField campoDataTermino;  // formato AAAA-MM-DD
    private JComboBox<Gerente> comboGerente;
    private JLabel labelStatus;           // status é só exibido (muda via ciclo de vida)

    // Botões alternados entre visualização e edição
    private JButton botaoEditar;
    private JButton botaoSalvar;

    // Botões de ciclo de vida (só modo editar) + o painel que os agrupa
    private JButton botaoIniciar;
    private JButton botaoConcluir;
    private JButton botaoCancelarProjeto;
    private JPanel painelCicloVida;

    // Controllers
    private ProjetoController controller;
    private UsuarioController usuarioController; // pra popular o combo de gerentes
    private EquipeController equipeController;   // pra alocar/listar equipes
    private TarefaController tarefaController;    // pra listar/remover tarefas do projeto

    // Projeto sendo editado: null = criar; existente = editar
    private Projeto projetoEmEdicao;

    // Tabela de equipes alocadas (aba Equipes alocadas)
    private DefaultTableModel modeloEquipes;
    private JTable tabelaEquipes;

    // Tabela de tarefas do projeto (aba Tarefas do projeto)
    private DefaultTableModel modeloTarefas;
    private JTable tabelaTarefas;

    // Usuário logado + permissão. Só Administrador/Gerente gerenciam o projeto
    // (editar dados, ciclo de vida, alocar equipes, criar/remover tarefas).
    // Colaborador vê tudo em leitura, mas pode mudar o STATUS das tarefas
    // (tratado na TelaDetalheTarefa).
    private Usuario usuarioLogado;
    private boolean podeGerenciar;

    // ===== Construtor =====
    public TelaProjeto(JFrame pai, Projeto projetoParaEditar, Usuario usuarioLogado) {
        super(pai, true); // modal

        this.controller = new ProjetoController();
        this.usuarioController = new UsuarioController();
        this.equipeController = new EquipeController();
        this.tarefaController = new TarefaController();
        this.projetoEmEdicao = projetoParaEditar;
        this.usuarioLogado = usuarioLogado;
        this.podeGerenciar = (usuarioLogado instanceof Administrador)
                || (usuarioLogado instanceof Gerente);

        boolean modoEditar = (projetoParaEditar != null);
        setTitle(modoEditar ? "Editar Projeto" : "Novo Projeto");

        setSize(560, 460);
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // CENTER: JTabbedPane com abas Dados e Equipes alocadas
        // (a aba "Tarefas do projeto" entra na Fase D)
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Dados", construirFormulario());
        abas.addTab("Equipes alocadas", construirAbaEquipes());
        abas.addTab("Tarefas do projeto", construirAbaTarefas());
        add(abas, BorderLayout.CENTER);

        // SOUTH: botões
        add(construirBotoes(), BorderLayout.SOUTH);

        if (modoEditar) {
            preencherCampos(projetoParaEditar);
            // Abre travado (visualização); Editar libera
            definirEdicaoHabilitada(false);
            // Habilita os botões de ciclo de vida conforme o status atual
            atualizarStatus();
        } else {
            // Criar: campos liberados, sem botão Editar e sem ciclo de vida
            // (o projeto ainda não existe — nasce sempre PLANEJADO ao salvar)
            this.botaoEditar.setVisible(false);
            this.painelCicloVida.setVisible(false);
        }

        // PERMISSÃO: Colaborador vê o projeto em modo leitura — sem botão Editar
        // (o ciclo de vida e os botões de gestão são tratados nos seus métodos)
        if (!this.podeGerenciar) {
            this.botaoEditar.setVisible(false);
        }
    }

    // ===== Formulário (Aba Dados) =====
    private JPanel construirFormulario() {
        JPanel painel = new JPanel(new BorderLayout(5, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // NORTH: campos de uma linha numa grade (label | campo)
        JPanel grade = new JPanel(new GridLayout(5, 2, 5, 8));

        this.campoNome = new JTextField();
        this.campoDataInicio = new JTextField();
        this.campoDataTermino = new JTextField();
        this.comboGerente = new JComboBox<>();
        this.labelStatus = new JLabel("PLANEJADO"); // default pra modo criar

        grade.add(criarLabel("Nome:"));
        grade.add(this.campoNome);
        grade.add(criarLabel("Início previsto (AAAA-MM-DD):"));
        grade.add(this.campoDataInicio);
        grade.add(criarLabel("Término previsto (AAAA-MM-DD):"));
        grade.add(this.campoDataTermino);
        grade.add(criarLabel("Gerente:"));
        grade.add(this.comboGerente);
        grade.add(criarLabel("Status:"));
        grade.add(this.labelStatus);

        painel.add(grade, BorderLayout.NORTH);

        // CENTER: descrição (JTextArea com rolagem)
        JPanel painelDesc = new JPanel(new BorderLayout(5, 5));
        painelDesc.add(new JLabel("Descrição:"), BorderLayout.NORTH);
        this.campoDescricao = new JTextArea(5, 20);
        this.campoDescricao.setLineWrap(true);
        this.campoDescricao.setWrapStyleWord(true);
        painelDesc.add(new JScrollPane(this.campoDescricao), BorderLayout.CENTER);
        painel.add(painelDesc, BorderLayout.CENTER);

        // SOUTH do form: ciclo de vida (em cima) + ações dos Dados (Editar/Salvar embaixo).
        // Os dois grupos ficam DENTRO da aba Dados — não no rodapé do dialog.
        this.painelCicloVida = construirBotoesCicloVida();
        JPanel painelSul = new JPanel(new BorderLayout());
        painelSul.add(this.painelCicloVida, BorderLayout.CENTER);
        painelSul.add(construirAcoesDados(), BorderLayout.SOUTH);
        painel.add(painelSul, BorderLayout.SOUTH);

        // Popula o combo só com usuários que são Gerente
        carregarGerentes();

        return painel;
    }

    // ===== Botões de ciclo de vida (Iniciar / Concluir / Cancelar Projeto) =====
    private JPanel construirBotoesCicloVida() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        painel.setBorder(BorderFactory.createTitledBorder("Ciclo de vida"));

        this.botaoIniciar = new JButton("Iniciar");
        this.botaoConcluir = new JButton("Concluir");
        // "Cancelar Projeto" (não "Cancelar") pra não confundir com o botão
        // Cancelar do rodapé, que só fecha o dialog
        this.botaoCancelarProjeto = new JButton("Cancelar Projeto");

        // Cada botão dispara a transição correspondente via controller.
        // Uso de um helper que trata erro + atualiza o status na tela.
        // O () -> ... é uma lambda Runnable (sem argumentos).
        this.botaoIniciar.addActionListener(e -> executarCicloVida(
                () -> this.controller.iniciar(this.projetoEmEdicao),
                "Iniciar este projeto?",
                "Projeto iniciado!"));
        this.botaoConcluir.addActionListener(e -> executarCicloVida(
                () -> this.controller.concluir(this.projetoEmEdicao),
                "Concluir este projeto? Esta ação não pode ser revertida.",
                "Projeto concluído!"));
        this.botaoCancelarProjeto.addActionListener(e -> executarCicloVida(
                () -> this.controller.cancelar(this.projetoEmEdicao),
                "Cancelar este projeto? Esta ação não pode ser revertida.",
                "Projeto cancelado!"));

        painel.add(this.botaoIniciar);
        painel.add(this.botaoConcluir);
        painel.add(this.botaoCancelarProjeto);
        return painel;
    }

    // Executa uma ação de ciclo de vida, com CONFIRMAÇÃO antes (evita clique
    // acidental — transições de projeto não têm "desfazer" hoje), atualiza o
    // status na tela e trata erros.
    // 'acao' é um Runnable — o trecho de código a executar (controller.iniciar/etc).
    private void executarCicloVida(Runnable acao, String confirmacaoMsg, String mensagemSucesso) {
        // Pede confirmação; se o usuário não disser "Sim", aborta sem fazer nada
        int resposta = JOptionPane.showConfirmDialog(this, confirmacaoMsg,
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            acao.run();          // dispara a transição (pode lançar TransicaoInvalidaException)
            atualizarStatus();   // reflete o novo status no label e nos botões
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

    // Atualiza o label de status e habilita/desabilita os botões de ciclo de vida
    // conforme o status atual do projeto (regras de transição do domínio).
    private void atualizarStatus() {
        StatusProjeto status = this.projetoEmEdicao.getStatus();
        this.labelStatus.setText(status.toString());

        // PLANEJADO → pode Iniciar ou Cancelar
        // EM_ANDAMENTO → pode Concluir ou Cancelar
        // CONCLUIDO / CANCELADO → estados finais, nada habilitado
        // Só habilita as transições se o usuário pode gerenciar (admin/gerente) E
        // o status atual permite. Colaborador nunca muda o ciclo de vida do projeto.
        this.botaoIniciar.setEnabled(this.podeGerenciar && status == StatusProjeto.PLANEJADO);
        this.botaoConcluir.setEnabled(this.podeGerenciar && status == StatusProjeto.EM_ANDAMENTO);
        this.botaoCancelarProjeto.setEnabled(this.podeGerenciar
                && (status == StatusProjeto.PLANEJADO || status == StatusProjeto.EM_ANDAMENTO));
    }

    // ===== Aba "Equipes alocadas" (gestão N:N entre Projeto e Equipe) =====
    private JPanel construirAbaEquipes() {
        JPanel painel = new JPanel(new BorderLayout());

        // Modo criar: o projeto ainda não tem id, então não dá pra alocar equipes
        if (this.projetoEmEdicao == null) {
            JLabel aviso = new JLabel(
                    "Salve o projeto primeiro para alocar equipes.",
                    SwingConstants.CENTER);
            painel.add(aviso, BorderLayout.CENTER);
            return painel;
        }

        this.modeloEquipes = new DefaultTableModel(
                new String[]{"ID", "Nome", "Descrição"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.tabelaEquipes = new JTable(this.modeloEquipes);
        this.tabelaEquipes.getColumnModel().getColumn(0).setMaxWidth(60);
        this.tabelaEquipes.getColumnModel().getColumn(0).setPreferredWidth(50);
        painel.add(new JScrollPane(this.tabelaEquipes), BorderLayout.CENTER);

        // Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton botaoAlocar = new JButton("Alocar Equipe");
        JButton botaoDesalocar = new JButton("Desalocar Equipe");
        botaoAlocar.addActionListener(e -> alocarEquipe());
        botaoDesalocar.addActionListener(e -> desalocarEquipe());
        // PERMISSÃO: Colaborador só visualiza as equipes alocadas
        botaoAlocar.setEnabled(this.podeGerenciar);
        botaoDesalocar.setEnabled(this.podeGerenciar);
        painelBotoes.add(botaoAlocar);
        painelBotoes.add(botaoDesalocar);
        painel.add(painelBotoes, BorderLayout.SOUTH);

        carregarEquipesAlocadas();

        return painel;
    }

    // Recarrega a tabela de equipes alocadas a partir do banco.
    private void carregarEquipesAlocadas() {
        this.modeloEquipes.setRowCount(0);
        try {
            for (Equipe eq : this.controller.listarEquipes(this.projetoEmEdicao.getId())) {
                this.modeloEquipes.addRow(new Object[]{
                        eq.getId(),
                        eq.getNome(),
                        eq.getDescricao()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar equipes: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Abre um diálogo com as equipes que AINDA NÃO estão alocadas e aloca a escolhida.
    private void alocarEquipe() {
        try {
            List<Equipe> todas = this.equipeController.listarTodos();
            List<Equipe> alocadas = this.controller.listarEquipes(this.projetoEmEdicao.getId());

            // Coleta os ids das equipes já alocadas
            List<Integer> idsAlocadas = new ArrayList<>();
            for (Equipe eq : alocadas) {
                idsAlocadas.add(eq.getId());
            }
            // Candidatas = equipes que ainda não estão alocadas
            List<Equipe> candidatas = new ArrayList<>();
            for (Equipe eq : todas) {
                if (!idsAlocadas.contains(eq.getId())) {
                    candidatas.add(eq);
                }
            }

            if (candidatas.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Todas as equipes já estão alocadas a este projeto.",
                        "Aviso",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Equipe escolhida = (Equipe) JOptionPane.showInputDialog(
                    this,
                    "Selecione a equipe:",
                    "Alocar Equipe",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    candidatas.toArray(),
                    candidatas.get(0));

            if (escolhida != null) {
                this.controller.alocarEquipe(this.projetoEmEdicao.getId(), escolhida.getId());
                carregarEquipesAlocadas();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao alocar equipe: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Desaloca do projeto a equipe selecionada na tabela.
    private void desalocarEquipe() {
        int linha = this.tabelaEquipes.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma equipe pra desalocar.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int equipeId = (int) this.modeloEquipes.getValueAt(linha, 0);
        String nome = (String) this.modeloEquipes.getValueAt(linha, 1);

        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Desalocar a equipe \"" + nome + "\" deste projeto?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                this.controller.desalocarEquipe(this.projetoEmEdicao.getId(), equipeId);
                carregarEquipesAlocadas();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao desalocar equipe: " + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===== Aba "Tarefas do projeto" =====
    private JPanel construirAbaTarefas() {
        JPanel painel = new JPanel(new BorderLayout());

        // Modo criar: o projeto ainda não tem id
        if (this.projetoEmEdicao == null) {
            JLabel aviso = new JLabel(
                    "Salve o projeto primeiro para gerenciar as tarefas.",
                    SwingConstants.CENTER);
            painel.add(aviso, BorderLayout.CENTER);
            return painel;
        }

        this.modeloTarefas = new DefaultTableModel(
                new String[]{"ID", "Título", "Status", "Prioridade", "Equipe", "Responsável"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.tabelaTarefas = new JTable(this.modeloTarefas);
        this.tabelaTarefas.getColumnModel().getColumn(0).setMaxWidth(60);
        this.tabelaTarefas.getColumnModel().getColumn(0).setPreferredWidth(50);
        painel.add(new JScrollPane(this.tabelaTarefas), BorderLayout.CENTER);

        // Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton botaoNova = new JButton("Nova Tarefa");
        JButton botaoEditarTarefa = new JButton("Abrir");
        JButton botaoRemover = new JButton("Remover");

        // Nova: abre TelaDetalheTarefa (modal) em modo criar, no contexto deste projeto
        botaoNova.addActionListener(e -> {
            TelaDetalheTarefa dialog = new TelaDetalheTarefa(
                    this, this.projetoEmEdicao.getId(), this.usuarioLogado);
            dialog.setVisible(true);
            carregarTarefas(); // refresh ao fechar
        });
        // Editar: abre a tarefa selecionada na TelaDetalheTarefa em modo editar
        botaoEditarTarefa.addActionListener(e -> {
            int linha = this.tabelaTarefas.getSelectedRow();
            if (linha < 0) {
                JOptionPane.showMessageDialog(this,
                        "Selecione uma tarefa pra editar.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) this.modeloTarefas.getValueAt(linha, 0);
            Tarefa tarefa = this.tarefaController.buscarPorId(id);
            TelaDetalheTarefa dialog = new TelaDetalheTarefa(this, tarefa, this.usuarioLogado);
            dialog.setVisible(true);
            carregarTarefas(); // refresh ao fechar
        });
        // Remover já é funcional (tarefa é "folha" — sem FKs dependentes)
        botaoRemover.addActionListener(e -> removerTarefaSelecionada());

        // PERMISSÃO: Colaborador não cria nem remove tarefas. Mas "Abrir" fica
        // habilitado pra todos — é por ali que o Colaborador muda o STATUS da
        // tarefa (a TelaDetalheTarefa decide o que ele pode editar).
        botaoNova.setEnabled(this.podeGerenciar);
        botaoRemover.setEnabled(this.podeGerenciar);

        painelBotoes.add(botaoNova);
        painelBotoes.add(botaoEditarTarefa);
        painelBotoes.add(botaoRemover);
        painel.add(painelBotoes, BorderLayout.SOUTH);

        carregarTarefas();

        return painel;
    }

    // Recarrega a tabela de tarefas do projeto a partir do banco.
    private void carregarTarefas() {
        this.modeloTarefas.setRowCount(0);
        try {
            for (Tarefa t : this.tarefaController.listarPorProjeto(this.projetoEmEdicao.getId())) {
                this.modeloTarefas.addRow(new Object[]{
                        t.getId(),
                        t.getTitulo(),
                        t.getStatus(),
                        t.getPrioridade(),
                        t.getEquipe().getNome(),
                        t.getResponsavel() != null
                                ? t.getResponsavel().getNome()
                                : "(sem responsável)"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar tarefas: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Remove a tarefa selecionada na tabela.
    private void removerTarefaSelecionada() {
        int linha = this.tabelaTarefas.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma tarefa pra remover.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) this.modeloTarefas.getValueAt(linha, 0);
        String titulo = (String) this.modeloTarefas.getValueAt(linha, 1);

        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Remover a tarefa \"" + titulo + "\"?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                this.tarefaController.remover(id);
                carregarTarefas();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao remover tarefa: " + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JLabel criarLabel(String texto) {
        return new JLabel(texto, SwingConstants.RIGHT);
    }

    // Popula o comboGerente apenas com usuários do tipo Gerente.
    // (Um projeto exige um Gerente responsável — não faz sentido oferecer
    //  Administradores ou Colaboradores aqui.)
    private void carregarGerentes() {
        for (Usuario u : this.usuarioController.listarTodos()) {
            // Pattern matching do instanceof (Java 16+): testa o tipo E
            // já cria a variável 'g' do tipo Gerente se o teste passar
            if (u instanceof Gerente g) {
                this.comboGerente.addItem(g);
            }
        }
    }

    // Botões de ação dos Dados (Editar/Salvar), exibidos dentro da aba Dados
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

    // ===== Rodapé: só "Fechar" (Editar/Salvar moram na aba Dados) =====
    private JPanel construirBotoes() {
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton botaoFechar = new JButton("Fechar");
        botaoFechar.addActionListener(e -> dispose());
        painelBotoes.add(botaoFechar);
        return painelBotoes;
    }

    // Alterna entre visualização e edição dos campos editáveis.
    private void definirEdicaoHabilitada(boolean habilitada) {
        this.campoNome.setEnabled(habilitada);
        this.campoDescricao.setEnabled(habilitada);
        this.campoDataInicio.setEnabled(habilitada);
        this.campoDataTermino.setEnabled(habilitada);
        this.comboGerente.setEnabled(habilitada);
        this.botaoSalvar.setEnabled(habilitada);
        this.botaoEditar.setEnabled(!habilitada);
    }

    // ===== Preenche o form (modo editar) =====
    private void preencherCampos(Projeto p) {
        this.campoNome.setText(p.getNome());
        this.campoDescricao.setText(p.getDescricao() != null ? p.getDescricao() : "");
        // LocalDate.toString() devolve "AAAA-MM-DD" (ISO 8601)
        this.campoDataInicio.setText(p.getDataInicioPrevista().toString());
        this.campoDataTermino.setText(p.getDataTerminoPrevista().toString());
        this.labelStatus.setText(p.getStatus().toString());
        selecionarGerenteNoCombo(p.getGerente());
    }

    // Seleciona no combo o gerente com o mesmo id (compara por id, não por
    // referência — o objeto do combo e o do projeto são instâncias diferentes).
    private void selecionarGerenteNoCombo(Gerente gerente) {
        if (gerente == null) {
            return;
        }
        for (int i = 0; i < this.comboGerente.getItemCount(); i++) {
            if (this.comboGerente.getItemAt(i).getId() == gerente.getId()) {
                this.comboGerente.setSelectedIndex(i);
                return;
            }
        }
    }

    // ===== Ação do Salvar =====
    private void salvar() {
        try {
            String nome = this.campoNome.getText();
            String descricao = this.campoDescricao.getText();
            // Converte o texto dos campos de data em LocalDate (pode lançar
            // DateTimeParseException se o formato estiver errado)
            LocalDate dataInicio = LocalDate.parse(this.campoDataInicio.getText().trim());
            LocalDate dataTermino = LocalDate.parse(this.campoDataTermino.getText().trim());
            Gerente gerente = (Gerente) this.comboGerente.getSelectedItem();

            if (gerente == null) {
                JOptionPane.showMessageDialog(this,
                        "Selecione um gerente. (Cadastre um usuário com perfil Gerente se não houver.)",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (this.projetoEmEdicao == null) {
                // ===== MODO CRIAR =====
                Projeto novo = new Projeto(nome, dataInicio, dataTermino, gerente);
                novo.setDescricao(descricao);
                this.controller.cadastrar(novo);

                JOptionPane.showMessageDialog(this,
                        "Projeto cadastrado com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // ===== MODO EDITAR =====
                this.projetoEmEdicao.setNome(nome);
                this.projetoEmEdicao.setDescricao(descricao);
                this.projetoEmEdicao.setDataInicioPrevista(dataInicio);
                this.projetoEmEdicao.setDataTerminoPrevista(dataTermino);
                this.projetoEmEdicao.setGerente(gerente);
                this.controller.atualizar(this.projetoEmEdicao);

                JOptionPane.showMessageDialog(this,
                        "Projeto atualizado com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();

        } catch (DateTimeParseException ex) {
            // Erro específico de data mal formatada — mensagem amigável
            JOptionPane.showMessageDialog(this,
                    "Data inválida. Use o formato AAAA-MM-DD (ex: 2026-06-01).",
                    "Erro de validação",
                    JOptionPane.ERROR_MESSAGE);
        } catch (GestaoProjetosException ex) {
            // Erros do domínio (campo obrigatório, regra de negócio das datas)
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
