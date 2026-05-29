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
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

// Importando layouts
import java.awt.BorderLayout;
import java.awt.FlowLayout;

// Importando coleções (lista de candidatos a membro)
import java.util.ArrayList;
import java.util.List;

// Importando os Controllers
import com.gestaoprojetos.controller.EquipeController;
import com.gestaoprojetos.controller.UsuarioController;
import com.gestaoprojetos.controller.TarefaController;
import com.gestaoprojetos.controller.ProjetoController;

// Importando os modelos
import com.gestaoprojetos.model.Equipe;
import com.gestaoprojetos.model.Usuario;
import com.gestaoprojetos.model.Tarefa;
import com.gestaoprojetos.model.Projeto;
import com.gestaoprojetos.model.Administrador;
import com.gestaoprojetos.model.Gerente;

// Importando a base das exceções customizadas
import com.gestaoprojetos.exception.GestaoProjetosException;

// TelaEquipe: JDialog MODAL pra criar ou editar uma equipe.
// Mesmo padrão da TelaUsuario:
//   construtor recebe Equipe null (criar) ou existente (editar).
// A Equipe é mais simples (só nome + descrição), mas tem gestão de
// membros (N:N) que vai entrar na aba "Membros" (Fase B).
public class TelaEquipe extends JDialog {

    // ===== Atributos =====
    // Campos do formulário (Aba Dados)
    private JTextField campoNome;
    private JTextArea campoDescricao;

    // Botões cujo estado (habilitado/desabilitado) é alternado entre
    // modo visualização e modo edição
    private JButton botaoEditar;
    private JButton botaoSalvar;

    // Controllers
    private EquipeController controller;
    private UsuarioController usuarioController; // pra listar candidatos a membro
    private TarefaController tarefaController;   // pra listar tarefas da equipe
    private ProjetoController projetoController; // pra resolver nome do projeto de cada tarefa

    // Equipe sendo editada: null = criar; existente = editar
    private Equipe equipeEmEdicao;

    // Tabela de membros (aba Membros) — atributos porque adicionar/remover
    // membro precisam acessar o modelo e a seleção
    private DefaultTableModel modeloMembros;
    private JTable tabelaMembros;

    // Permissão: só Administrador/Gerente podem editar equipe e gerenciar membros.
    // Colaborador vê tudo em modo leitura.
    private boolean podeGerenciar;

    // ===== Construtor =====
    public TelaEquipe(JFrame pai, Equipe equipeParaEditar, Usuario usuarioLogado) {
        super(pai, true); // modal

        this.controller = new EquipeController();
        this.usuarioController = new UsuarioController();
        this.tarefaController = new TarefaController();
        this.projetoController = new ProjetoController();
        this.equipeEmEdicao = equipeParaEditar;

        // Define a permissão com base no perfil de quem está logado
        this.podeGerenciar = (usuarioLogado instanceof Administrador)
                || (usuarioLogado instanceof Gerente);

        boolean modoEditar = (equipeParaEditar != null);
        setTitle(modoEditar ? "Editar Equipe" : "Nova Equipe");

        setSize(820, 460);
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // CENTER: JTabbedPane com abas Dados, Membros e Tarefas da equipe
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Dados", construirFormulario());
        abas.addTab("Membros", construirAbaMembros());
        abas.addTab("Tarefas da equipe", construirAbaTarefas());
        add(abas, BorderLayout.CENTER);

        // SOUTH: botões
        add(construirBotoes(), BorderLayout.SOUTH);

        if (modoEditar) {
            preencherCampos(equipeParaEditar);
            // Abre em modo VISUALIZAÇÃO: campos travados até clicar em Editar.
            // Evita alterações acidentais no nome/descrição.
            definirEdicaoHabilitada(false);
        } else {
            // Modo CRIAR: campos já editáveis; o botão Editar não faz sentido aqui
            this.botaoEditar.setVisible(false);
        }

        // PERMISSÃO: Colaborador vê a equipe em modo leitura — sem botão Editar
        if (!this.podeGerenciar) {
            this.botaoEditar.setVisible(false);
        }
    }

    // ===== Formulário (Aba Dados) =====
    private JPanel construirFormulario() {
        // BorderLayout: nome em cima (NORTH), descrição preenche o resto (CENTER)
        JPanel painel = new JPanel(new BorderLayout(5, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // NORTH: linha do nome (label + campo, alinhados à esquerda)
        JPanel linhaNome = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        linhaNome.add(new JLabel("Nome:"));
        this.campoNome = new JTextField(25);
        linhaNome.add(this.campoNome);
        painel.add(linhaNome, BorderLayout.NORTH);

        // CENTER: descrição — label em cima + JTextArea (multilinhas) com rolagem
        JPanel painelDescricao = new JPanel(new BorderLayout(5, 5));
        painelDescricao.add(new JLabel("Descrição:"), BorderLayout.NORTH);
        this.campoDescricao = new JTextArea(6, 25);
        this.campoDescricao.setLineWrap(true);      // quebra de linha automática
        this.campoDescricao.setWrapStyleWord(true); // quebra em palavras inteiras (não no meio)
        // JTextArea SEMPRE dentro de JScrollPane pra ter rolagem quando o texto cresce
        painelDescricao.add(new JScrollPane(this.campoDescricao), BorderLayout.CENTER);
        painel.add(painelDescricao, BorderLayout.CENTER);

        // SOUTH: ações dos DADOS (Editar/Salvar) — moram aqui DENTRO da aba Dados,
        // porque só dizem respeito ao formulário. Membros/Tarefas têm seus próprios
        // botões e persistem na hora, então não dependem deste Salvar.
        painel.add(construirAcoesDados(), BorderLayout.SOUTH);

        return painel;
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

    // Alterna entre modo VISUALIZAÇÃO (campos travados) e EDIÇÃO (campos liberados).
    // - habilitada=false → campos bloqueados, Salvar off, Editar disponível
    // - habilitada=true  → campos liberados, Salvar on, Editar indisponível
    private void definirEdicaoHabilitada(boolean habilitada) {
        this.campoNome.setEnabled(habilitada);
        this.campoDescricao.setEnabled(habilitada);
        this.botaoSalvar.setEnabled(habilitada);
        this.botaoEditar.setEnabled(!habilitada);
    }

    // ===== Preenche o form (modo editar) =====
    private void preencherCampos(Equipe e) {
        this.campoNome.setText(e.getNome());
        // Descrição é opcional — pode vir null do banco; evita exibir "null"
        this.campoDescricao.setText(e.getDescricao() != null ? e.getDescricao() : "");
    }

    // ===== Aba "Membros" (gestão N:N entre Equipe e Usuario) =====
    private JPanel construirAbaMembros() {
        JPanel painel = new JPanel(new BorderLayout());

        // Modo criar: a equipe ainda não tem id, então não dá pra associar membros
        if (this.equipeEmEdicao == null) {
            JLabel aviso = new JLabel(
                    "Salve a equipe primeiro para gerenciar os membros.",
                    SwingConstants.CENTER);
            painel.add(aviso, BorderLayout.CENTER);
            return painel;
        }

        // Modo editar: tabela de membros + botões adicionar/remover
        this.modeloMembros = new DefaultTableModel(
                new String[]{"ID", "Nome", "Login", "Perfil"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.tabelaMembros = new JTable(this.modeloMembros);
        this.tabelaMembros.getColumnModel().getColumn(0).setMaxWidth(60);
        this.tabelaMembros.getColumnModel().getColumn(0).setPreferredWidth(50);
        painel.add(new JScrollPane(this.tabelaMembros), BorderLayout.CENTER);

        // Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton botaoAdicionar = new JButton("Adicionar Membro");
        JButton botaoRemover = new JButton("Remover Membro");
        botaoAdicionar.addActionListener(e -> adicionarMembro());
        botaoRemover.addActionListener(e -> removerMembro());
        // PERMISSÃO: Colaborador só visualiza os membros (não gerencia)
        botaoAdicionar.setEnabled(this.podeGerenciar);
        botaoRemover.setEnabled(this.podeGerenciar);
        painelBotoes.add(botaoAdicionar);
        painelBotoes.add(botaoRemover);
        painel.add(painelBotoes, BorderLayout.SOUTH);

        // Carga inicial
        carregarMembros();

        return painel;
    }

    // Recarrega a tabela de membros da equipe a partir do banco.
    private void carregarMembros() {
        this.modeloMembros.setRowCount(0);
        try {
            for (Usuario m : this.controller.listarMembros(this.equipeEmEdicao.getId())) {
                this.modeloMembros.addRow(new Object[]{
                        m.getId(),
                        m.getNome(),
                        m.getLogin(),
                        m.getClass().getSimpleName()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar membros: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Abre um diálogo de seleção com os usuários que AINDA NÃO são membros,
    // e adiciona o escolhido à equipe.
    private void adicionarMembro() {
        try {
            // Monta a lista de candidatos: todos os usuários menos os que já são membros
            List<Usuario> todos = this.usuarioController.listarTodos();
            List<Usuario> membros = this.controller.listarMembros(this.equipeEmEdicao.getId());

            // Coleta os ids de quem já é membro
            List<Integer> idsMembros = new ArrayList<>();
            for (Usuario m : membros) {
                idsMembros.add(m.getId());
            }
            // Candidatos = quem não está na lista de membros
            List<Usuario> candidatos = new ArrayList<>();
            for (Usuario u : todos) {
                if (!idsMembros.contains(u.getId())) {
                    candidatos.add(u);
                }
            }

            if (candidatos.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Todos os usuários já são membros desta equipe.",
                        "Aviso",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Diálogo de seleção — usa o toString() do Usuario pra exibir "nome (login)"
            Usuario escolhido = (Usuario) JOptionPane.showInputDialog(
                    this,
                    "Selecione o usuário:",
                    "Adicionar Membro",
                    JOptionPane.PLAIN_MESSAGE,
                    null,                       // sem ícone
                    candidatos.toArray(),       // opções
                    candidatos.get(0));         // seleção inicial

            // null = usuário fechou/cancelou o diálogo
            if (escolhido != null) {
                this.controller.adicionarMembro(this.equipeEmEdicao.getId(), escolhido.getId());
                carregarMembros();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao adicionar membro: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Remove da equipe o membro selecionado na tabela.
    private void removerMembro() {
        int linha = this.tabelaMembros.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um membro pra remover.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int usuarioId = (int) this.modeloMembros.getValueAt(linha, 0);
        String nome = (String) this.modeloMembros.getValueAt(linha, 1);

        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Remover \"" + nome + "\" desta equipe?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                this.controller.removerMembro(this.equipeEmEdicao.getId(), usuarioId);
                carregarMembros();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao remover membro: " + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===== Aba "Tarefas da equipe" (só leitura) =====
    // Lista as tarefas atribuídas a esta equipe (via tarefaController.listarPorEquipe).
    private JPanel construirAbaTarefas() {
        JPanel painel = new JPanel(new BorderLayout());

        // Modo criar: equipe ainda não existe (sem id)
        if (this.equipeEmEdicao == null) {
            JLabel aviso = new JLabel(
                    "Salve a equipe primeiro para ver as tarefas atribuídas a ela.",
                    SwingConstants.CENTER);
            painel.add(aviso, BorderLayout.CENTER);
            return painel;
        }

        // Modo editar: tabela com as tarefas da equipe
        String[] colunas = {"ID", "Título", "Projeto", "Status", "Prioridade", "Responsável"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabela = new JTable(modelo);
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);

        try {
            for (Tarefa t : this.tarefaController.listarPorEquipe(this.equipeEmEdicao.getId())) {
                // A Tarefa guarda só o projetoId (int), não o objeto Projeto.
                // Busco o projeto pra exibir o NOME em vez do número.
                Projeto projeto = this.projetoController.buscarPorId(t.getProjetoId());
                String nomeProjeto = (projeto != null) ? projeto.getNome() : "—";

                modelo.addRow(new Object[]{
                        t.getId(),
                        t.getTitulo(),
                        nomeProjeto,
                        t.getStatus(),       // enum → toString()
                        t.getPrioridade(),   // enum → toString()
                        // Responsável é opcional — pode ser null
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

        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return painel;
    }

    // ===== Ação do Salvar =====
    private void salvar() {
        try {
            String nome = this.campoNome.getText();
            String descricao = this.campoDescricao.getText();

            if (this.equipeEmEdicao == null) {
                // ===== MODO CRIAR =====
                // O construtor de Equipe só recebe nome; descrição vem via setter
                Equipe nova = new Equipe(nome);
                nova.setDescricao(descricao);
                this.controller.cadastrar(nova);

                JOptionPane.showMessageDialog(this,
                        "Equipe cadastrada com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // ===== MODO EDITAR =====
                this.equipeEmEdicao.setNome(nome);
                this.equipeEmEdicao.setDescricao(descricao);
                this.controller.atualizar(this.equipeEmEdicao);

                JOptionPane.showMessageDialog(this,
                        "Equipe atualizada com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();

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
