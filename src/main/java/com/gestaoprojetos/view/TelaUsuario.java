// Package: organiza a classe dentro do pacote view (camada de apresentação)
package com.gestaoprojetos.view;

// Importando classes do Swing
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

// Importando layouts
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;

// Importando os Controllers
import com.gestaoprojetos.controller.UsuarioController;
import com.gestaoprojetos.controller.TarefaController;

// Importando os modelos — Usuario (abstrato) + 3 subclasses concretas pra polimorfismo
import com.gestaoprojetos.model.Usuario;
import com.gestaoprojetos.model.Administrador;
import com.gestaoprojetos.model.Gerente;
import com.gestaoprojetos.model.Colaborador;
import com.gestaoprojetos.model.Tarefa;

// Importando a base das exceções customizadas pra catch específico
import com.gestaoprojetos.exception.GestaoProjetosException;

// TelaUsuario: JDialog MODAL pra criar ou editar um usuário do sistema.
// - JDialog (não JFrame) porque quero comportamento MODAL — bloqueia
//   a TelaPrincipal até o usuário decidir Salvar ou Cancelar.
// - Construtor recebe um Usuario opcional:
//     null      → modo CRIAR (form em branco)
//     existente → modo EDITAR (campos preenchidos com os valores atuais)
// - Após salvar (sucesso) ou cancelar, o dialog fecha e o controle volta
//   pra TelaPrincipal, que pode dar refresh na tabela.
public class TelaUsuario extends JDialog {

    // ===== Atributos =====
    // Campos do formulário — atributos porque o método salvar() precisa lê-los
    private JTextField campoNome;
    private JTextField campoCpf;
    private JTextField campoEmail;
    private JTextField campoCargo;
    private JTextField campoLogin;
    private JPasswordField campoSenha;
    private JComboBox<String> comboPerfil;

    // Controllers (instanciados no construtor)
    private UsuarioController controller;
    private TarefaController tarefaController;

    // Botões cujo estado é alternado entre visualização e edição
    private JButton botaoEditar;
    private JButton botaoSalvar;

    // Usuário sendo editado: null = modo criar; objeto existente = modo editar
    private Usuario usuarioEmEdicao;

    // ===== Construtor =====
    // pai: a janela "dona" (tipicamente TelaPrincipal) — necessária pro modal
    // usuarioParaEditar: null = modo criar; objeto = modo editar
    public TelaUsuario(JFrame pai, Usuario usuarioParaEditar) {
        // super(pai, true): true torna o dialog MODAL (bloqueia o pai até fechar)
        super(pai, true);

        this.controller = new UsuarioController();
        this.tarefaController = new TarefaController();
        this.usuarioEmEdicao = usuarioParaEditar;

        // Título e tamanho dependem do modo
        boolean modoEditar = (usuarioParaEditar != null);
        setTitle(modoEditar ? "Editar Usuário" : "Novo Usuário");

        setSize(560, 420);
        setLocationRelativeTo(pai); // centraliza relativo ao pai
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // só fecha o dialog (não o app)
        setLayout(new BorderLayout());

        // CENTER: JTabbedPane com 2 abas — Dados (form) e Minhas Tarefas (lista)
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Dados", construirFormulario());
        abas.addTab("Minhas Tarefas", construirAbaTarefas());
        add(abas, BorderLayout.CENTER);

        // SOUTH: botões Salvar / Cancelar
        add(construirBotoes(), BorderLayout.SOUTH);

        // Se for modo editar, preenche os campos com os valores existentes
        if (modoEditar) {
            preencherCamposDoUsuario(usuarioParaEditar); // também desabilita cpf/login/perfil (imutáveis)
            // Abre em modo VISUALIZAÇÃO: campos mutáveis travados até clicar em Editar.
            // Evita alterações acidentais.
            definirEdicaoHabilitada(false);
        } else {
            // Modo CRIAR: campos já editáveis; botão Editar não faz sentido aqui
            this.botaoEditar.setVisible(false);
        }
    }

    // ===== Constrói o painel da aba Dados (form + ações) =====
    private JPanel construirFormulario() {
        // GridLayout 7 linhas × 2 colunas (label | campo), espaçamentos 5px
        JPanel grade = new JPanel(new GridLayout(7, 2, 5, 5));

        // Margem ao redor do formulário (top, left, bottom, right)
        grade.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Cria os campos (preenchidos em modo editar)
        this.campoNome = new JTextField();
        this.campoCpf = new JTextField();
        this.campoEmail = new JTextField();
        this.campoCargo = new JTextField();
        this.campoLogin = new JTextField();
        this.campoSenha = new JPasswordField();
        this.comboPerfil = new JComboBox<>(
                new String[]{"Administrador", "Gerente", "Colaborador"});

        // Adiciona pares (label, campo) na ordem da grade (esquerda-direita, cima-baixo)
        // Labels alinhados à direita encostam no campo correspondente
        grade.add(criarLabel("Nome:"));
        grade.add(this.campoNome);
        grade.add(criarLabel("CPF:"));
        grade.add(this.campoCpf);
        grade.add(criarLabel("E-mail:"));
        grade.add(this.campoEmail);
        grade.add(criarLabel("Cargo:"));
        grade.add(this.campoCargo);
        grade.add(criarLabel("Login:"));
        grade.add(this.campoLogin);
        grade.add(criarLabel("Senha:"));
        grade.add(this.campoSenha);
        grade.add(criarLabel("Perfil:"));
        grade.add(this.comboPerfil);

        // Wrapper: grade no CENTER + ações dos Dados (Editar/Salvar) no SOUTH.
        // Os botões moram aqui DENTRO da aba Dados (não no rodapé) porque só
        // dizem respeito ao formulário. A aba "Minhas Tarefas" é só leitura.
        JPanel painel = new JPanel(new BorderLayout());
        painel.add(grade, BorderLayout.CENTER);
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

    // Helper: cria JLabel alinhado à direita (pra ficar visualmente próximo do campo)
    private JLabel criarLabel(String texto) {
        return new JLabel(texto, SwingConstants.RIGHT);
    }

    // ===== Aba "Minhas Tarefas" =====
    // Lista (só leitura) das tarefas em que este usuário é responsável.
    // Em modo criar, o usuário ainda não existe (sem id) — mostra um aviso.
    private JPanel construirAbaTarefas() {
        JPanel painel = new JPanel(new BorderLayout());

        // Modo criar: ainda não há usuário salvo, então não há tarefas pra listar
        if (this.usuarioEmEdicao == null) {
            JLabel aviso = new JLabel(
                    "Salve o usuário primeiro para ver as tarefas atribuídas a ele.",
                    SwingConstants.CENTER);
            painel.add(aviso, BorderLayout.CENTER);
            return painel;
        }

        // Modo editar: monta a tabela com as tarefas do responsável
        String[] colunas = {"ID", "Título", "Status", "Prioridade", "Equipe"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabela = new JTable(modelo);
        // Coluna ID estreita (mesmo padrão da TelaPrincipal)
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);

        // Popula com as tarefas em que este usuário é responsável
        try {
            for (Tarefa t : this.tarefaController.listarPorResponsavel(this.usuarioEmEdicao.getId())) {
                modelo.addRow(new Object[]{
                        t.getId(),
                        t.getTitulo(),
                        t.getStatus(),                 // enum → toString()
                        t.getPrioridade(),             // enum → toString()
                        t.getEquipe().getNome()        // navega o objeto Equipe carregado pelo DAO
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

    // ===== Rodapé: só "Fechar" (Editar/Salvar moram na aba Dados) =====
    private JPanel construirBotoes() {
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton botaoFechar = new JButton("Fechar");
        botaoFechar.addActionListener(e -> dispose());
        painelBotoes.add(botaoFechar);
        return painelBotoes;
    }

    // Alterna entre VISUALIZAÇÃO e EDIÇÃO dos campos MUTÁVEIS (nome/email/cargo/senha).
    // Os campos imutáveis (cpf/login/perfil) permanecem desabilitados em modo editar
    // independente disso — são tratados em preencherCamposDoUsuario().
    private void definirEdicaoHabilitada(boolean habilitada) {
        this.campoNome.setEnabled(habilitada);
        this.campoCpf.setEnabled(habilitada);
        this.campoEmail.setEnabled(habilitada);
        this.campoCargo.setEnabled(habilitada);
        this.campoLogin.setEnabled(habilitada);
        this.campoSenha.setEnabled(habilitada);
        this.comboPerfil.setEnabled(habilitada);
        this.botaoSalvar.setEnabled(habilitada);
        this.botaoEditar.setEnabled(!habilitada);
    }

    // ===== Preenche o form com dados de um usuário existente (modo editar) =====
    private void preencherCamposDoUsuario(Usuario u) {
        // Preenche todos os campos com os valores atuais
        this.campoNome.setText(u.getNome());
        this.campoCpf.setText(u.getCpf());
        this.campoEmail.setText(u.getEmail());
        this.campoCargo.setText(u.getCargo());
        this.campoLogin.setText(u.getLogin());
        this.campoSenha.setText(u.getSenha());
        // getClass().getSimpleName() devolve "Administrador"/"Gerente"/"Colaborador"
        // que casa exatamente com os itens do comboPerfil
        this.comboPerfil.setSelectedItem(u.getClass().getSimpleName());
        // Travamento dos campos é feito pelo definirEdicaoHabilitada(false)
        // chamado em seguida (lock-to-edit). Admin pode editar TODOS os campos.
    }

    // ===== Ação do botão Salvar =====
    private void salvar() {
        try {
            // 1. Lê os valores dos campos
            String nome   = this.campoNome.getText();
            String cpf    = this.campoCpf.getText();
            String email  = this.campoEmail.getText();
            String cargo  = this.campoCargo.getText();
            String login  = this.campoLogin.getText();
            String senha  = new String(this.campoSenha.getPassword());
            String perfil = (String) this.comboPerfil.getSelectedItem();

            // 2. Despacha pelo modo
            if (this.usuarioEmEdicao == null) {
                // ===== MODO CRIAR =====
                // Cria a subclasse certa de Usuario com base no perfil escolhido.
                // (Polimorfismo: cada subclasse implementa permissoes() do seu jeito.)
                Usuario novo = criarUsuarioComPerfil(
                        nome, cpf, email, cargo, login, senha, perfil);
                this.controller.cadastrar(novo);

                JOptionPane.showMessageDialog(this,
                        "Usuário cadastrado com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // ===== MODO EDITAR =====
                // Admin pode editar TUDO, inclusive o perfil. Como mudar o perfil
                // implica trocar a subclasse de Usuario, reconstruo o objeto
                // pela factory (criarUsuarioComPerfil) e preservo o id original.
                Usuario atualizado = criarUsuarioComPerfil(
                        nome, cpf, email, cargo, login, senha, perfil);
                atualizado.setId(this.usuarioEmEdicao.getId());
                this.controller.atualizar(atualizado);

                JOptionPane.showMessageDialog(this,
                        "Usuário atualizado com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            // Sucesso: fecha o dialog (controle volta pra TelaPrincipal)
            dispose();

        } catch (GestaoProjetosException ex) {
            // Erros do domínio (campo obrigatório, regra de negócio)
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Erro de validação",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            // Qualquer outro erro (ex: banco, login duplicado pego pelo MySQL)
            JOptionPane.showMessageDialog(this,
                    "Erro: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== Factory: cria a subclasse certa de Usuario com base no perfil escolhido =====
    // Switch expression (Java 14+) — mais conciso que switch tradicional.
    private Usuario criarUsuarioComPerfil(String nome, String cpf, String email,
                                          String cargo, String login, String senha,
                                          String perfil) {
        return switch (perfil) {
            case "Administrador" -> new Administrador(nome, cpf, email, cargo, login, senha);
            case "Gerente"       -> new Gerente(nome, cpf, email, cargo, login, senha);
            case "Colaborador"   -> new Colaborador(nome, cpf, email, cargo, login, senha);
            default -> throw new IllegalArgumentException("Perfil inválido: " + perfil);
        };
    }
}
