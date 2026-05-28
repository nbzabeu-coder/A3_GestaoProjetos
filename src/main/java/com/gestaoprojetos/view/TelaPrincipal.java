// Package: organiza a classe dentro do pacote view (camada de apresentação)
package com.gestaoprojetos.view;

// Importando classes do Swing
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

// Importando layouts
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Font;

// Coleções: ordenação (Collections.sort + Comparator), fila de prioridade
// (PriorityQueue) e listas (List/ArrayList) pra montar "Meus Projetos"
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

// Importando os Controllers — cada aba consulta o controller correspondente
import com.gestaoprojetos.controller.UsuarioController;
import com.gestaoprojetos.controller.EquipeController;
import com.gestaoprojetos.controller.ProjetoController;
import com.gestaoprojetos.controller.TarefaController;
// Importando os modelos
import com.gestaoprojetos.model.Equipe;
import com.gestaoprojetos.model.Projeto;
import com.gestaoprojetos.model.Usuario;
import com.gestaoprojetos.model.Administrador;
import com.gestaoprojetos.model.Gerente;
import com.gestaoprojetos.model.Tarefa;
import com.gestaoprojetos.model.StatusTarefa;

// Importando os relatórios (interface + implementações) — aba Relatórios
import com.gestaoprojetos.relatorio.Relatorio;
import com.gestaoprojetos.relatorio.RelatorioDeProjeto;
import com.gestaoprojetos.relatorio.RelatorioDeEquipe;
import com.gestaoprojetos.relatorio.RelatorioDeColaborador;

// Classe TelaPrincipal: hub do sistema após login.
// Estrutura (Opção C): JTabbedPane com 3 abas (Usuários | Equipes | Projetos).
// Cada aba tem JTable read-only + botões Novo / Editar / Excluir.
// Tarefas aparecem dentro das telas das entidades — não há aba "Tarefas" global.
public class TelaPrincipal extends JFrame {

    // ===== Atributos =====

    // Usuário autenticado (preservado pra exibir saudação e, no futuro,
    // decidir quais abas mostrar conforme permissões — TODO Fase 4)
    private Usuario usuarioLogado;

    // Controllers: instanciados no construtor, usados pelas abas pra
    // carregar/atualizar dados
    private UsuarioController usuarioController;
    private EquipeController equipeController;
    private ProjetoController projetoController;
    private TarefaController tarefaController; // usado pela aba Relatórios

    // Modelos das tabelas: atributos porque os métodos de Excluir
    // (chamados pelos botões) precisam REMOVER linhas do modelo
    // pra refletir visualmente a mudança no banco.
    private DefaultTableModel modeloUsuarios;
    private DefaultTableModel modeloEquipes;
    private DefaultTableModel modeloProjetos;

    // Campo de busca da aba Projetos (filtro global — qualquer coluna)
    private JTextField campoBuscaProjetos;

    // ===== Componentes da aba Relatórios =====
    private JComboBox<String> comboTipoRelatorio;   // Projeto / Equipe / Colaborador
    private JComboBox<Object> comboEntidadeRelatorio; // a entidade escolhida (objeto)
    private JTextArea areaRelatorio;                 // mostra o texto gerado
    private JButton botaoExportarRelatorio;          // só habilita após gerar
    private Relatorio relatorioAtual;                // último relatório gerado (pra exportar)

    // ===== Construtor =====
    public TelaPrincipal(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;

        // Instancia os controllers
        this.usuarioController = new UsuarioController();
        this.equipeController = new EquipeController();
        this.projetoController = new ProjetoController();
        this.tarefaController = new TarefaController();

        // Configurações da janela
        setTitle("Sistema de Gestão de Projetos — Principal");
        setSize(900, 600);
        setLocationRelativeTo(null); // centraliza na tela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== NORTH: saudação + botão Sair =====
        // BorderLayout pro topo: saudação centralizada, botão Sair à direita.
        JPanel painelSaudacao = new JPanel(new BorderLayout(10, 10));
        painelSaudacao.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel labelSaudacao = new JLabel(
                "Bem-vindo(a), " + this.usuarioLogado.getNome() + "!",
                SwingConstants.CENTER);
        // Aumenta um pouco a fonte pra dar destaque ao header
        labelSaudacao.setFont(labelSaudacao.getFont().deriveFont(Font.BOLD, 16f));
        painelSaudacao.add(labelSaudacao, BorderLayout.CENTER);

        JButton botaoSair = new JButton("Sair");
        botaoSair.setToolTipText("Sair desta sessão e voltar à tela de login");
        botaoSair.addActionListener(e -> sairDaSessao());
        // Wrapper FlowLayout pra alinhar o botão à direita sem esticar
        JPanel painelBotaoSair = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        painelBotaoSair.add(botaoSair);
        painelSaudacao.add(painelBotaoSair, BorderLayout.EAST);

        add(painelSaudacao, BorderLayout.NORTH);

        // ===== CENTER: JTabbedPane com as abas =====
        // (cada aba é construída por um método privado dedicado)
        JTabbedPane abas = new JTabbedPane();

        // Aba Início (painel pessoal do usuário logado) — primeira aba
        abas.addTab("Início", construirAbaInicio());

        // PERMISSÃO (versão básica via instanceof — TODO 1/8 prevê fazer isso
        // como comportamento do domínio): a aba Usuários só aparece pra
        // Administrador e Gerente. Colaborador não a vê.
        boolean podeVerUsuarios = (this.usuarioLogado instanceof Administrador)
                || (this.usuarioLogado instanceof Gerente);
        if (podeVerUsuarios) {
            abas.addTab("Usuários", construirAbaUsuarios());
        }

        abas.addTab("Equipes", construirAbaEquipes());
        abas.addTab("Projetos", construirAbaProjetos());
        abas.addTab("Relatórios", construirAbaRelatorios());

        // Refresca as abas dinâmicas (Início e Relatórios) ao serem selecionadas.
        // Outras abas podem ter alterado dados (criar projeto, mudar status, etc.)
        // que essas exibem — sem este listener elas ficariam mostrando estado antigo.
        abas.addChangeListener(e -> {
            int idx = abas.getSelectedIndex();
            String titulo = abas.getTitleAt(idx);
            if ("Início".equals(titulo)) {
                abas.setComponentAt(idx, construirAbaInicio());
            } else if ("Relatórios".equals(titulo)) {
                popularEntidadesRelatorio();
            }
        });

        add(abas, BorderLayout.CENTER);
    }

    // Logout: confirma com o usuário, abre uma nova TelaLogin e dispõe esta.
    // O app continua rodando (a nova TelaLogin é o único JFrame ativo).
    private void sairDaSessao() {
        int resp = JOptionPane.showConfirmDialog(this,
                "Sair desta sessão e voltar à tela de login?",
                "Confirmar saída",
                JOptionPane.YES_NO_OPTION);
        if (resp != JOptionPane.YES_OPTION) {
            return;
        }
        new TelaLogin().setVisible(true);
        this.dispose();
    }

    // ===== Métodos de construção das abas (stubs por enquanto — serão preenchidos nas fases B/C/D) =====

    // ===== Aba Usuários =====
    // Constrói o painel da aba: tabela (CENTER) + botões (SOUTH).
    private JPanel construirAbaUsuarios() {
        JPanel painel = new JPanel(new BorderLayout());

        // ----- Modelo da tabela (read-only) -----
        // Colunas que vão aparecer no cabeçalho
        String[] colunas = {"ID", "Nome", "Login", "Perfil"};
        // O '0' significa zero linhas iniciais — vamos preencher com carregarUsuarios()
        // O override de isCellEditable trava a edição direta nas células
        this.modeloUsuarios = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // ----- Tabela visual (vê o modelo, mostra na tela) -----
        // JTable SEMPRE dentro de JScrollPane pra ter rolagem e cabeçalho visível
        JTable tabela = new JTable(this.modeloUsuarios);

        // Larguras de coluna: ID estreito (só números, ~6 dígitos cabem em 60px)
        // setMaxWidth impede que ele cresça mesmo com a janela larga
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);

        // ORDENAÇÃO: clicar no cabeçalho ordena por aquela coluna
        tabela.setAutoCreateRowSorter(true);

        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);

        // ----- Botões (SOUTH) -----
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton botaoNovo = new JButton("Novo");
        JButton botaoAbrir = new JButton("Abrir");
        JButton botaoExcluir = new JButton("Excluir");

        // Novo: abre TelaUsuario (JDialog modal) em modo CRIAR.
        // setVisible(true) bloqueia até o dialog fechar; depois carregarUsuarios()
        // recarrega a tabela refletindo o novo registro (se foi salvo).
        botaoNovo.addActionListener(e -> {
            TelaUsuario dialog = new TelaUsuario(this, null);
            dialog.setVisible(true);
            carregarUsuarios();
        });

        // Editar: pega o usuário selecionado, busca o objeto completo no banco
        // e abre a TelaUsuario em modo editar (com os campos preenchidos).
        botaoAbrir.addActionListener(e -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                JOptionPane.showMessageDialog(this,
                        "Selecione um usuário pra editar.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) this.modeloUsuarios.getValueAt(linha, 0);
            Usuario usuario = this.usuarioController.buscarPorId(id);
            TelaUsuario dialog = new TelaUsuario(this, usuario);
            dialog.setVisible(true);
            carregarUsuarios(); // refresh após fechar
        });

        // Excluir é funcional — chama um método dedicado
        botaoExcluir.addActionListener(e -> excluirUsuarioSelecionado(tabela));

        // PERMISSÃO: só Administrador pode criar/abrir/excluir usuários.
        // Pro Gerente, a aba fica só leitura (vê a tabela, botões desabilitados).
        boolean podeEditarUsuarios = (this.usuarioLogado instanceof Administrador);
        botaoNovo.setEnabled(podeEditarUsuarios);
        botaoAbrir.setEnabled(podeEditarUsuarios);
        botaoExcluir.setEnabled(podeEditarUsuarios);

        painelBotoes.add(botaoNovo);
        painelBotoes.add(botaoAbrir);
        painelBotoes.add(botaoExcluir);
        painel.add(painelBotoes, BorderLayout.SOUTH);

        // ----- Carrega os dados iniciais -----
        carregarUsuarios();

        return painel;
    }

    // Recarrega a tabela de usuários do banco.
    // Chamado na construção da aba E após cada Excluir bem-sucedido.
    private void carregarUsuarios() {
        // Limpa o modelo (remove todas as linhas atuais antes de re-popular)
        this.modeloUsuarios.setRowCount(0);
        try {
            for (Usuario u : this.usuarioController.listarTodos()) {
                this.modeloUsuarios.addRow(new Object[]{
                        u.getId(),
                        u.getNome(),
                        u.getLogin(),
                        // getClass().getSimpleName() devolve "Administrador"/"Gerente"/"Colaborador"
                        // (o nome da subclasse de Usuario que foi instanciada — polimorfismo!)
                        u.getClass().getSimpleName()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar usuários: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Excluir usuário selecionado: lida com seleção vazia, autodeleção,
    // confirmação e tratamento de erro (ex: FK constraint do banco).
    private void excluirUsuarioSelecionado(JTable tabela) {
        // 1. Pega a linha selecionada (-1 se nenhuma)
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um usuário pra excluir.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Lê o id e o nome da linha selecionada (cols 0 e 1)
        int id = (int) this.modeloUsuarios.getValueAt(linhaSelecionada, 0);
        String nome = (String) this.modeloUsuarios.getValueAt(linhaSelecionada, 1);

        // 3. Bloqueia autodeleção
        if (id == this.usuarioLogado.getId()) {
            JOptionPane.showMessageDialog(this,
                    "Você não pode excluir o usuário com o qual está logado.",
                    "Operação inválida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 4. Confirmação (sim/não)
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Excluir o usuário \"" + nome + "\"?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);

        // 5. Se confirmou, executa a exclusão e atualiza a tabela
        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                this.usuarioController.remover(id);
                carregarUsuarios(); // re-popula refletindo a remoção
            } catch (Exception ex) {
                // Erro comum: o usuário é gerente de algum projeto (FK constraint)
                JOptionPane.showMessageDialog(this,
                        "Erro ao excluir: " + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // ===== Aba Equipes =====
    private JPanel construirAbaEquipes() {
        JPanel painel = new JPanel(new BorderLayout());

        // Colunas específicas da Equipe (não tem perfil/login — é entidade simples)
        String[] colunas = {"ID", "Nome", "Descrição"};
        this.modeloEquipes = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabela = new JTable(this.modeloEquipes);

        // Larguras de coluna:
        // - ID estreito (setMaxWidth trava o crescimento)
        // - Nome compacto (~150px)
        // - Descrição absorve o espaço restante (preferred maior + sem maxWidth)
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(150);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(500);

        // ORDENAÇÃO: clicar no cabeçalho ordena por aquela coluna
        tabela.setAutoCreateRowSorter(true);

        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton botaoNovo = new JButton("Novo");
        JButton botaoAbrir = new JButton("Abrir");
        JButton botaoExcluir = new JButton("Excluir");

        // Novo: abre TelaEquipe (modal) em modo criar; refresh ao fechar
        botaoNovo.addActionListener(e -> {
            TelaEquipe dialog = new TelaEquipe(this, null, this.usuarioLogado);
            dialog.setVisible(true);
            carregarEquipes();
        });

        // Abrir: busca a equipe selecionada e abre TelaEquipe (editar ou só leitura,
        // conforme o perfil — a própria TelaEquipe decide o que liberar)
        botaoAbrir.addActionListener(e -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                JOptionPane.showMessageDialog(this,
                        "Selecione uma equipe pra abrir.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) this.modeloEquipes.getValueAt(linha, 0);
            Equipe equipe = this.equipeController.buscarPorId(id);
            TelaEquipe dialog = new TelaEquipe(this, equipe, this.usuarioLogado);
            dialog.setVisible(true);
            carregarEquipes();
        });

        botaoExcluir.addActionListener(e -> excluirEquipeSelecionada(tabela));

        // PERMISSÃO: Colaborador só visualiza equipes — Novo/Excluir desabilitados.
        // "Abrir" fica disponível (a TelaEquipe abre em modo leitura pra ele).
        // Excluir equipe é operação destrutiva permanente — só Admin (TODO 8 item 3).
        boolean podeGerenciar = (this.usuarioLogado instanceof Administrador)
                || (this.usuarioLogado instanceof Gerente);
        botaoNovo.setEnabled(podeGerenciar);
        botaoExcluir.setEnabled(this.usuarioLogado instanceof Administrador);

        painelBotoes.add(botaoNovo);
        painelBotoes.add(botaoAbrir);
        painelBotoes.add(botaoExcluir);
        painel.add(painelBotoes, BorderLayout.SOUTH);

        carregarEquipes();

        return painel;
    }

    private void carregarEquipes() {
        this.modeloEquipes.setRowCount(0);
        try {
            for (Equipe e : this.equipeController.listarTodos()) {
                this.modeloEquipes.addRow(new Object[]{
                        e.getId(),
                        e.getNome(),
                        e.getDescricao()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar equipes: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirEquipeSelecionada(JTable tabela) {
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione uma equipe pra excluir.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) this.modeloEquipes.getValueAt(linhaSelecionada, 0);
        String nome = (String) this.modeloEquipes.getValueAt(linhaSelecionada, 1);

        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Excluir a equipe \"" + nome + "\"?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                this.equipeController.remover(id);
                carregarEquipes();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao excluir: " + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===== Aba Projetos =====
    private JPanel construirAbaProjetos() {
        JPanel painel = new JPanel(new BorderLayout());

        // Colunas: mais ricas que as outras 2 abas porque Projeto tem mais atributos
        // úteis em "vista de listagem" (status, gerente, datas).
        String[] colunas = {"ID", "Nome", "Status", "Gerente", "Início Prev.", "Término Prev."};
        this.modeloProjetos = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabela = new JTable(this.modeloProjetos);

        // Larguras de coluna:
        // - ID estreito (setMaxWidth trava o crescimento)
        // - Status com tamanho moderado (enum cabe em ~120px)
        // - Datas com tamanho moderado (~110px cabe "yyyy-mm-dd")
        // - Nome e Gerente absorvem o espaço restante
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(220); // Nome
        tabela.getColumnModel().getColumn(2).setPreferredWidth(120); // Status
        tabela.getColumnModel().getColumn(3).setPreferredWidth(160); // Gerente
        tabela.getColumnModel().getColumn(4).setPreferredWidth(110); // Início Prev.
        tabela.getColumnModel().getColumn(5).setPreferredWidth(110); // Término Prev.

        // ORDENAÇÃO: habilita ordenar clicando no cabeçalho de qualquer coluna
        // (o Swing usa TimSort internamente — O(n log n))
        tabela.setAutoCreateRowSorter(true);

        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);

        // ===== NORTH: campo de busca global (filtra por qualquer coluna) =====
        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        painelBusca.add(new JLabel("Buscar:"));
        this.campoBuscaProjetos = new JTextField(25);
        painelBusca.add(this.campoBuscaProjetos);
        painel.add(painelBusca, BorderLayout.NORTH);

        // Re-filtra a cada mudança no texto (busca "ao digitar"). DocumentListener
        // tem 3 métodos (inserir/remover/alterar texto); os 3 fazem a mesma coisa.
        this.campoBuscaProjetos.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { carregarProjetos(); }
            @Override public void removeUpdate(DocumentEvent e) { carregarProjetos(); }
            @Override public void changedUpdate(DocumentEvent e) { carregarProjetos(); }
        });

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton botaoNovo = new JButton("Novo");
        JButton botaoAbrir = new JButton("Abrir");
        JButton botaoExcluir = new JButton("Excluir");

        // Novo: abre TelaProjeto (modal) em modo criar; refresh ao fechar
        botaoNovo.addActionListener(e -> {
            TelaProjeto dialog = new TelaProjeto(this, null, this.usuarioLogado);
            dialog.setVisible(true);
            carregarProjetos();
        });

        // Abrir: busca o projeto selecionado e abre TelaProjeto (editar ou leitura,
        // conforme o perfil — a TelaProjeto decide o que liberar)
        botaoAbrir.addActionListener(e -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                JOptionPane.showMessageDialog(this,
                        "Selecione um projeto pra abrir.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) this.modeloProjetos.getValueAt(linha, 0);
            Projeto projeto = this.projetoController.buscarPorId(id);
            TelaProjeto dialog = new TelaProjeto(this, projeto, this.usuarioLogado);
            dialog.setVisible(true);
            carregarProjetos();
        });

        botaoExcluir.addActionListener(e -> excluirProjetoSelecionado(tabela));

        // PERMISSÃO: Colaborador só visualiza projetos — Novo/Excluir desabilitados.
        // "Abrir" fica disponível (TelaProjeto abre em leitura; tarefas mudáveis lá).
        boolean podeGerenciarProjetos = (this.usuarioLogado instanceof Administrador)
                || (this.usuarioLogado instanceof Gerente);
        botaoNovo.setEnabled(podeGerenciarProjetos);
        botaoExcluir.setEnabled(podeGerenciarProjetos);

        painelBotoes.add(botaoNovo);
        painelBotoes.add(botaoAbrir);
        painelBotoes.add(botaoExcluir);
        painel.add(painelBotoes, BorderLayout.SOUTH);

        carregarProjetos();

        return painel;
    }

    private void carregarProjetos() {
        this.modeloProjetos.setRowCount(0);

        // Texto digitado no campo de busca (vazio = sem filtro)
        String filtro = (this.campoBuscaProjetos != null)
                ? this.campoBuscaProjetos.getText().trim().toLowerCase()
                : "";

        try {
            var projetos = this.projetoController.listarTodos();

            // ORDENAÇÃO explícita: por nome, ignorando maiúsculas/minúsculas.
            // Collections.sort usa TimSort (O(n log n)) — eficiente.
            Collections.sort(projetos,
                    Comparator.comparing(Projeto::getNome, String.CASE_INSENSITIVE_ORDER));

            for (Projeto p : projetos) {
                String gerente = p.getGerente().getNome();

                // BUSCA global: junta TODOS os campos numa string e checa se o
                // filtro aparece nela (busca linear por substring — O(n)).
                String textoBusca = (p.getId() + " " + p.getNome() + " " + p.getStatus()
                        + " " + gerente + " " + p.getDataInicioPrevista()
                        + " " + p.getDataTerminoPrevista()).toLowerCase();

                if (filtro.isEmpty() || textoBusca.contains(filtro)) {
                    this.modeloProjetos.addRow(new Object[]{
                            p.getId(),
                            p.getNome(),
                            p.getStatus(),
                            gerente,
                            p.getDataInicioPrevista(),
                            p.getDataTerminoPrevista()
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar projetos: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirProjetoSelecionado(JTable tabela) {
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecione um projeto pra excluir.",
                    "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) this.modeloProjetos.getValueAt(linhaSelecionada, 0);
        String nome = (String) this.modeloProjetos.getValueAt(linhaSelecionada, 1);

        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Excluir o projeto \"" + nome + "\"?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                this.projetoController.remover(id);
                carregarProjetos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Erro ao excluir: " + ex.getMessage(),
                        "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===== Aba Relatórios =====
    // Permite escolher o TIPO de relatório (Projeto/Equipe/Colaborador) e a
    // ENTIDADE específica, gerar o texto (mostrado num JTextArea) e exportar pra .txt.
    private JPanel construirAbaRelatorios() {
        JPanel painel = new JPanel(new BorderLayout(5, 5));

        // NORTH: controles (tipo, item, gerar, exportar)
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        this.comboTipoRelatorio = new JComboBox<>(
                new String[]{"Projeto", "Equipe", "Colaborador"});
        this.comboEntidadeRelatorio = new JComboBox<>();
        JButton botaoGerar = new JButton("Gerar");
        this.botaoExportarRelatorio = new JButton("Exportar (.txt)");
        this.botaoExportarRelatorio.setEnabled(false); // só habilita depois de gerar

        // Quando o tipo muda, recarrega a lista de entidades
        this.comboTipoRelatorio.addActionListener(e -> popularEntidadesRelatorio());
        botaoGerar.addActionListener(e -> gerarRelatorio());
        this.botaoExportarRelatorio.addActionListener(e -> exportarRelatorio());

        controles.add(new JLabel("Tipo:"));
        controles.add(this.comboTipoRelatorio);
        controles.add(new JLabel("Item:"));
        controles.add(this.comboEntidadeRelatorio);
        controles.add(botaoGerar);
        controles.add(this.botaoExportarRelatorio);
        painel.add(controles, BorderLayout.NORTH);

        // CENTER: área de texto (só leitura) onde o relatório aparece
        this.areaRelatorio = new JTextArea();
        this.areaRelatorio.setEditable(false);
        // Fonte monoespaçada: alinha colunas/separadores do relatório
        this.areaRelatorio.setFont(new Font("Monospaced", Font.PLAIN, 12));
        painel.add(new JScrollPane(this.areaRelatorio), BorderLayout.CENTER);

        // Carga inicial do combo de entidades (tipo padrão = Projeto)
        popularEntidadesRelatorio();

        return painel;
    }

    // Recarrega o combo de entidades conforme o tipo escolhido.
    // PERMISSÃO: Administrador/Gerente veem tudo; Colaborador vê só os
    // projetos/equipes que participa, e só gera relatório do PRÓPRIO usuário.
    private void popularEntidadesRelatorio() {
        this.comboEntidadeRelatorio.removeAllItems();
        String tipo = (String) this.comboTipoRelatorio.getSelectedItem();

        boolean veTudo = (this.usuarioLogado instanceof Administrador)
                || (this.usuarioLogado instanceof Gerente);
        int meuId = this.usuarioLogado.getId();

        try {
            if ("Projeto".equals(tipo)) {
                // 'var' infere List<Projeto> dos dois ramos do ternário
                var projetos = veTudo
                        ? this.projetoController.listarTodos()
                        : this.projetoController.listarPorParticipante(meuId);
                for (Projeto p : projetos) {
                    this.comboEntidadeRelatorio.addItem(p);
                }
            } else if ("Equipe".equals(tipo)) {
                var equipes = veTudo
                        ? this.equipeController.listarTodos()
                        : this.equipeController.listarPorMembro(meuId);
                for (Equipe eq : equipes) {
                    this.comboEntidadeRelatorio.addItem(eq);
                }
            } else { // Colaborador
                if (veTudo) {
                    for (Usuario u : this.usuarioController.listarTodos()) {
                        this.comboEntidadeRelatorio.addItem(u);
                    }
                } else {
                    // Colaborador só pode gerar relatório de si mesmo
                    this.comboEntidadeRelatorio.addItem(this.usuarioLogado);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao carregar itens: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Cria o relatório CERTO conforme o tipo (Opção A: passamos os dados prontos),
    // gera o texto e mostra na tela. Aqui o POLIMORFISMO acontece: criamos uma das
    // 3 implementações, mas a partir daí tratamos tudo como 'Relatorio'.
    private void gerarRelatorio() {
        String tipo = (String) this.comboTipoRelatorio.getSelectedItem();
        Object entidade = this.comboEntidadeRelatorio.getSelectedItem();
        if (entidade == null) {
            JOptionPane.showMessageDialog(this, "Selecione um item.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if ("Projeto".equals(tipo)) {
                Projeto p = (Projeto) entidade;
                this.relatorioAtual = new RelatorioDeProjeto(
                        p,
                        this.projetoController.listarEquipes(p.getId()),
                        this.tarefaController.listarPorProjeto(p.getId()));
            } else if ("Equipe".equals(tipo)) {
                Equipe eq = (Equipe) entidade;
                this.relatorioAtual = new RelatorioDeEquipe(
                        eq,
                        this.equipeController.listarMembros(eq.getId()),
                        this.tarefaController.listarPorEquipe(eq.getId()),
                        construirMapaProjetos());
            } else { // Colaborador
                Usuario u = (Usuario) entidade;
                this.relatorioAtual = new RelatorioDeColaborador(
                        u,
                        this.tarefaController.listarPorResponsavel(u.getId()),
                        construirMapaProjetos());
            }

            // Polimorfismo: gerar() sem saber qual das 3 é
            this.areaRelatorio.setText(this.relatorioAtual.gerar());
            this.areaRelatorio.setCaretPosition(0); // volta a rolagem pro topo
            this.botaoExportarRelatorio.setEnabled(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao gerar relatório: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Exporta o último relatório gerado pra um arquivo .txt.
    private void exportarRelatorio() {
        if (this.relatorioAtual == null) {
            return;
        }
        try {
            this.relatorioAtual.exportar("txt"); // polimorfismo de novo
            JOptionPane.showMessageDialog(this,
                    "Relatório exportado para a pasta 'relatorios/' (na raiz do projeto).",
                    "Exportado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao exportar: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== Aba Início (painel pessoal do usuário logado) =====
    // 3 seções: Meus Projetos (participa), Minhas Equipes (é membro) e
    // Minhas Tarefas (é responsável) — esta última é uma FILA DE PRIORIDADE.
    private JPanel construirAbaInicio() {
        // Administrador supervisiona o sistema todo (não participa de projetos):
        // a Início dele é uma VISÃO GERAL, não um painel pessoal.
        if (this.usuarioLogado instanceof Administrador) {
            return construirInicioAdmin();
        }

        JPanel painel = new JPanel(new GridLayout(3, 1, 5, 5));
        int meuId = this.usuarioLogado.getId();
        // Gerente tem visão de "meus projetos" (monitoramento); os demais, visão pessoal
        boolean ehGerente = (this.usuarioLogado instanceof Gerente);

        // ----- Meus Projetos: os que GERENCIO + os que PARTICIPO (sem repetir) -----
        // Assim um Gerente vê os projetos sob sua responsabilidade, e um
        // Colaborador vê os projetos das equipes que participa.
        DefaultTableModel modeloMeusProjetos = modeloSomenteLeitura(
                new String[]{"ID", "Nome", "Status"});
        try {
            List<Projeto> meusProjetos = new ArrayList<>();
            List<Integer> idsVistos = new ArrayList<>();
            // 1º os que gerencio
            for (Projeto p : this.projetoController.listarPorGerente(meuId)) {
                if (!idsVistos.contains(p.getId())) {
                    meusProjetos.add(p);
                    idsVistos.add(p.getId());
                }
            }
            // 2º os que participo (que ainda não estejam na lista)
            for (Projeto p : this.projetoController.listarPorParticipante(meuId)) {
                if (!idsVistos.contains(p.getId())) {
                    meusProjetos.add(p);
                    idsVistos.add(p.getId());
                }
            }
            for (Projeto p : meusProjetos) {
                modeloMeusProjetos.addRow(new Object[]{p.getId(), p.getNome(), p.getStatus()});
            }
        } catch (Exception ex) {
            // silencioso aqui — erro de banco já aparece nas outras abas
        }
        painel.add(secaoComTabela("Meus Projetos", modeloMeusProjetos));

        // ----- Seção de equipes: depende do perfil -----
        if (ehGerente) {
            // GERENTE: equipes alocadas aos projetos que gerencia (sem repetir)
            DefaultTableModel modeloEquipesGerente = modeloSomenteLeitura(
                    new String[]{"ID", "Nome"});
            try {
                List<Integer> idsVistos = new ArrayList<>();
                for (Projeto p : this.projetoController.listarPorGerente(meuId)) {
                    for (Equipe eq : this.projetoController.listarEquipes(p.getId())) {
                        if (!idsVistos.contains(eq.getId())) {
                            idsVistos.add(eq.getId());
                            modeloEquipesGerente.addRow(new Object[]{eq.getId(), eq.getNome()});
                        }
                    }
                }
            } catch (Exception ex) {
            }
            painel.add(secaoComTabela("Equipes dos meus projetos", modeloEquipesGerente));
        } else {
            // COLABORADOR: equipes que é membro
            DefaultTableModel modeloMinhasEquipes = modeloSomenteLeitura(
                    new String[]{"ID", "Nome"});
            try {
                for (Equipe eq : this.equipeController.listarPorMembro(meuId)) {
                    modeloMinhasEquipes.addRow(new Object[]{eq.getId(), eq.getNome()});
                }
            } catch (Exception ex) {
            }
            painel.add(secaoComTabela("Minhas Equipes", modeloMinhasEquipes));
        }

        // ----- Seção de tarefas: depende do perfil -----
        if (ehGerente) {
            // GERENTE: acompanha TODAS as tarefas dos projetos que gerencia
            // (visão de monitoramento — pra ver se está tudo conforme o planejado).
            DefaultTableModel modeloTarefasGerente = modeloSomenteLeitura(
                    new String[]{"Projeto", "Título", "Status", "Prioridade", "Responsável"});
            try {
                for (Projeto p : this.projetoController.listarPorGerente(meuId)) {
                    for (Tarefa t : this.tarefaController.listarPorProjeto(p.getId())) {
                        String resp = (t.getResponsavel() != null)
                                ? t.getResponsavel().getNome()
                                : "(sem responsável)";
                        modeloTarefasGerente.addRow(new Object[]{
                                p.getNome(), t.getTitulo(), t.getStatus(),
                                t.getPrioridade(), resp
                        });
                    }
                }
            } catch (Exception ex) {
            }
            painel.add(secaoComTabela(
                    "Tarefas dos meus projetos (acompanhamento)", modeloTarefasGerente));
        } else {
            // COLABORADOR (e Admin): fila pessoal de tarefas A FAZER, por prioridade.
            // PriorityQueue ordenada por prioridade DECRESCENTE (ALTA primeiro):
            // poll() sempre retira a de MAIOR prioridade — "fila de trabalho".
            DefaultTableModel modeloMinhasTarefas = modeloSomenteLeitura(
                    new String[]{"Prioridade", "Título", "Status", "Equipe"});
            try {
                PriorityQueue<Tarefa> fila = new PriorityQueue<>(
                        Comparator.comparing(Tarefa::getPrioridade).reversed());
                for (Tarefa t : this.tarefaController.listarPorResponsavel(meuId)) {
                    if (t.getStatus() != StatusTarefa.CONCLUIDA) {
                        fila.add(t);
                    }
                }
                while (!fila.isEmpty()) {
                    Tarefa t = fila.poll();
                    modeloMinhasTarefas.addRow(new Object[]{
                            t.getPrioridade(), t.getTitulo(), t.getStatus(), t.getEquipe().getNome()
                    });
                }
            } catch (Exception ex) {
            }
            painel.add(secaoComTabela(
                    "Minhas Tarefas (a fazer, por prioridade)", modeloMinhasTarefas));
        }

        return painel;
    }

    // ===== Início do Administrador: SÓ o Resumo do sistema (rico) =====
    private JPanel construirInicioAdmin() {
        return construirResumoSistema();
    }

    // Resumo do sistema: dados GERAIS (com %) + dados POR PROJETO (com %).
    // Aqui entra o "fator aritmético": cálculos de totais e porcentagens.
    private JPanel construirResumoSistema() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Resumo do sistema"));

        StringBuilder sb = new StringBuilder();
        try {
            var projetos = this.projetoController.listarTodos();
            int totalProj = projetos.size();

            // ---- Contagem de projetos por status ----
            int planejados = 0, andamento = 0, concluidos = 0, cancelados = 0;
            for (Projeto pr : projetos) {
                switch (pr.getStatus()) {
                    case PLANEJADO -> planejados++;
                    case EM_ANDAMENTO -> andamento++;
                    case CONCLUIDO -> concluidos++;
                    case CANCELADO -> cancelados++;
                }
            }

            // ---- Tarefas (total geral; distribuição por status vem do helper) ----
            var todasTarefas = this.tarefaController.listarTodos();
            int totalTarefas = todasTarefas.size();

            // ===== GERAL =====
            sb.append("=========== GERAL ===========\n");
            sb.append("Projetos: ").append(totalProj).append("\n");
            sb.append("   Planejados:   ").append(planejados).append(" (").append(porcentagem(planejados, totalProj)).append(")\n");
            sb.append("   Em andamento: ").append(andamento).append(" (").append(porcentagem(andamento, totalProj)).append(")\n");
            sb.append("   Concluídos:   ").append(concluidos).append(" (").append(porcentagem(concluidos, totalProj)).append(")\n");
            sb.append("   Cancelados:   ").append(cancelados).append(" (").append(porcentagem(cancelados, totalProj)).append(")\n");
            sb.append("Equipes:  ").append(this.equipeController.listarTodos().size()).append("\n");
            sb.append("Usuários: ").append(this.usuarioController.listarTodos().size()).append("\n");
            sb.append("Tarefas:  ").append(totalTarefas).append("\n");
            sb.append(Relatorio.distribuicaoPorStatus(todasTarefas));

            // ===== POR PROJETO =====
            sb.append("\n======= POR PROJETO =======\n");
            for (Projeto pr : projetos) {
                int nEquipes = this.projetoController.listarEquipes(pr.getId()).size();
                var tarefasProj = this.tarefaController.listarPorProjeto(pr.getId());
                sb.append("\n").append(pr.getNome()).append("  [").append(pr.getStatus()).append("]\n");
                sb.append("   Equipes alocadas: ").append(nEquipes).append("\n");
                sb.append("   Tarefas: ").append(tarefasProj.size()).append("\n");
                // % de cada status das tarefas do projeto (reaproveita o helper do relatório)
                sb.append(Relatorio.distribuicaoPorStatus(tarefasProj));
            }
        } catch (Exception ex) {
            sb.append("Erro ao calcular o resumo: ").append(ex.getMessage());
        }

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setCaretPosition(0); // começa no topo
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }

    // Calcula porcentagem (parte/total) formatada como "xx.x%". Guarda divisão por zero.
    // Constrói o mapa projetoId → nome do projeto, usado pelos relatórios de
    // equipe/colaborador pra mostrar a qual projeto cada tarefa pertence
    // (a Tarefa só carrega projetoId, não o objeto Projeto).
    private Map<Integer, String> construirMapaProjetos() {
        Map<Integer, String> mapa = new HashMap<>();
        for (Projeto p : this.projetoController.listarTodos()) {
            mapa.put(p.getId(), p.getNome());
        }
        return mapa;
    }

    private String porcentagem(int parte, int total) {
        double pct = (total == 0) ? 0.0 : (parte * 100.0) / total;
        return String.format("%.1f%%", pct);
    }

    // Helper: cria um DefaultTableModel só-leitura com as colunas dadas
    private DefaultTableModel modeloSomenteLeitura(String[] colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // Helper: monta uma seção (borda com título + JTable em JScrollPane)
    private JPanel secaoComTabela(String titulo, DefaultTableModel modelo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        p.add(new JScrollPane(new JTable(modelo)), BorderLayout.CENTER);
        return p;
    }

    // ===== Getter herdado do stub anterior (útil pra outras telas) =====
    public Usuario getUsuarioLogado() {
        return this.usuarioLogado;
    }
}
