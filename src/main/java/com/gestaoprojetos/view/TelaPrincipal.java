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
// Importando os modelos
import com.gestaoprojetos.model.Equipe;
import com.gestaoprojetos.model.Projeto;
import com.gestaoprojetos.model.Usuario;

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

    // Modelos das tabelas: atributos porque os métodos de Excluir
    // (chamados pelos botões) precisam REMOVER linhas do modelo
    // pra refletir visualmente a mudança no banco.
    private DefaultTableModel modeloUsuarios;
    private DefaultTableModel modeloEquipes;
    private DefaultTableModel modeloProjetos;

    // ===== Construtor =====
    public TelaPrincipal(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;

        // Instancia os controllers
        this.usuarioController = new UsuarioController();
        this.equipeController = new EquipeController();
        this.projetoController = new ProjetoController();

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
                "Bem-vinda, " + this.usuarioLogado.getNome() + "!",
                SwingConstants.CENTER);
        // Aumenta um pouco a fonte pra dar destaque ao header
        labelSaudacao.setFont(labelSaudacao.getFont().deriveFont(Font.BOLD, 16f));
        painelSaudacao.add(labelSaudacao);
        add(painelSaudacao, BorderLayout.NORTH);

        // ===== CENTER: JTabbedPane com as 3 abas =====
        // (cada aba é construída por um método privado dedicado)
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Usuários", construirAbaUsuarios());
        abas.addTab("Equipes", construirAbaEquipes());
        abas.addTab("Projetos", construirAbaProjetos());
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
            TelaEquipe dialog = new TelaEquipe(this, null);
            dialog.setVisible(true);
            carregarEquipes();
        });

        // Editar: busca a equipe selecionada e abre TelaEquipe em modo editar
        botaoAbrir.addActionListener(e -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                JOptionPane.showMessageDialog(this,
                        "Selecione uma equipe pra editar.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) this.modeloEquipes.getValueAt(linha, 0);
            Equipe equipe = this.equipeController.buscarPorId(id);
            TelaEquipe dialog = new TelaEquipe(this, equipe);
            dialog.setVisible(true);
            carregarEquipes();
        });

        botaoExcluir.addActionListener(e -> excluirEquipeSelecionada(tabela));

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
            TelaProjeto dialog = new TelaProjeto(this, null);
            dialog.setVisible(true);
            carregarProjetos();
        });

        // Editar: busca o projeto selecionado e abre TelaProjeto em modo editar
        botaoAbrir.addActionListener(e -> {
            int linha = tabela.getSelectedRow();
            if (linha < 0) {
                JOptionPane.showMessageDialog(this,
                        "Selecione um projeto pra editar.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) this.modeloProjetos.getValueAt(linha, 0);
            Projeto projeto = this.projetoController.buscarPorId(id);
            TelaProjeto dialog = new TelaProjeto(this, projeto);
            dialog.setVisible(true);
            carregarProjetos();
        });

        botaoExcluir.addActionListener(e -> excluirProjetoSelecionado(tabela));

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

    // ===== Getter herdado do stub anterior (útil pra outras telas) =====
    public Usuario getUsuarioLogado() {
        return this.usuarioLogado;
    }
}
