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
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

// Importando layouts
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

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

        // ===== NORTH: saudação =====
        // JPanel wrapper pra dar um respiro (padding) ao redor do label
        JPanel painelSaudacao = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel labelSaudacao = new JLabel(
                "Bem-vindo(a), " + this.usuarioLogado.getNome() + "!",
                SwingConstants.CENTER);
        // Aumenta um pouco a fonte pra dar destaque ao header
        labelSaudacao.setFont(labelSaudacao.getFont().deriveFont(Font.BOLD, 16f));
        painelSaudacao.add(labelSaudacao);
        add(painelSaudacao, BorderLayout.NORTH);

        // ===== CENTER: JTabbedPane com as abas =====
        // (cada aba é construída por um método privado dedicado)
        JTabbedPane abas = new JTabbedPane();

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
        add(abas, BorderLayout.CENTER);
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
        boolean podeGerenciar = (this.usuarioLogado instanceof Administrador)
                || (this.usuarioLogado instanceof Gerente);
        botaoNovo.setEnabled(podeGerenciar);
        botaoExcluir.setEnabled(podeGerenciar);

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

        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);

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
        try {
            for (Projeto p : this.projetoController.listarTodos()) {
                this.modeloProjetos.addRow(new Object[]{
                        p.getId(),
                        p.getNome(),
                        p.getStatus(),               // enum — toString() devolve "PLANEJADO" etc.
                        p.getGerente().getNome(),    // navega o objeto Gerente já carregado pelo DAO
                        p.getDataInicioPrevista(),   // LocalDate — toString() devolve "yyyy-mm-dd"
                        p.getDataTerminoPrevista()
                });
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
                        this.tarefaController.listarPorEquipe(eq.getId()));
            } else { // Colaborador
                Usuario u = (Usuario) entidade;
                this.relatorioAtual = new RelatorioDeColaborador(
                        u,
                        this.tarefaController.listarPorResponsavel(u.getId()));
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

    // ===== Getter herdado do stub anterior (útil pra outras telas) =====
    public Usuario getUsuarioLogado() {
        return this.usuarioLogado;
    }
}
