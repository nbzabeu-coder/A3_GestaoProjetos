// Package: organiza a classe dentro do pacote view (camada de apresentação do MVC)
package com.gestaoprojetos.view;

// Importando classes do Swing — componentes visuais e gerenciamento de janela
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JOptionPane;

// Importando layouts — definem como os componentes são posicionados na tela
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;

// Importando o Controller — a TelaLogin pede a ele que autentique
import com.gestaoprojetos.controller.UsuarioController;

// Importando o modelo — o usuário autenticado é devolvido pelo controller
import com.gestaoprojetos.model.Usuario;

// Classe TelaLogin: estende JFrame, ou seja, ela É uma janela
// (herda tudo de JFrame: setTitle, setSize, setVisible, etc.)
public class TelaLogin extends JFrame {

    // Atributos da tela
    // - Campos de texto: precisam ser atributos porque o método autenticar()
    // (chamado pelo botão) precisa LER o que o usuário digitou neles
    private JTextField loginField;
    private JPasswordField senhaField;

    // Controller: a tela não conversa com o DAO direto — passa pelo controller
    private UsuarioController controller;

    // Construtor: monta a janela inteira
    public TelaLogin() {
        // Instancia o controller que será usado na autenticação
        this.controller = new UsuarioController();

        // Configurações da janela (herdadas de JFrame)
        setTitle("Login — Sistema de Gestão de Projetos");
        setSize(400, 220); // largura x altura em pixels
        setLocationRelativeTo(null); // centraliza na tela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // fechar X encerra o app
        setLayout(new BorderLayout()); // a janela vai usar BorderLayout

        // ===== NORTH: painel do título =====
        // JPanel simples (FlowLayout padrão centraliza o conteúdo)
        JPanel painelTitulo = new JPanel();
        JLabel labelTitulo = new JLabel("Sistema de Gestão de Projetos");
        painelTitulo.add(labelTitulo);

        // ===== CENTER: painel do formulário =====
        // Estrutura: 2 linhas empilhadas verticalmente (GridLayout 2 linhas, 1 coluna).
        // Cada linha é um SUB-painel com FlowLayout — que respeita o tamanho preferido
        // do JTextField (não estica), deixando label + campo visualmente juntos
        // e o par centralizado horizontalmente.
        JPanel painelFormulario = new JPanel(new GridLayout(2, 1, 5, 5));

        // Instancia os campos (declarados como atributos lá em cima)
        // - 15 = quantidade aproximada de colunas visíveis no campo
        this.loginField = new JTextField(15);
        this.senhaField = new JPasswordField(15);

        // Linha 1: "Login:" + campo de texto, centralizados horizontalmente
        JPanel linhaLogin = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        linhaLogin.add(new JLabel("Login:"));
        linhaLogin.add(this.loginField);

        // Linha 2: "Senha:" + campo de senha, centralizados horizontalmente
        JPanel linhaSenha = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        linhaSenha.add(new JLabel("Senha:"));
        linhaSenha.add(this.senhaField);

        // Adiciona as 2 linhas ao painel formulário
        painelFormulario.add(linhaLogin);
        painelFormulario.add(linhaSenha);

        // ===== SOUTH: painel do botão =====
        // FlowLayout com alinhamento ao centro (constante FlowLayout.CENTER)
        JPanel painelBotao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton botaoEntrar = new JButton("Entrar");
        painelBotao.add(botaoEntrar);

        // ===== Encaixa os 3 sub-painéis nas regiões do BorderLayout =====
        add(painelTitulo, BorderLayout.NORTH);

        // Wrapper: JPanel com FlowLayout (padrão) respeita o tamanho preferido
        // do painelFormulario, em vez de esticá-lo verticalmente como o CENTER faria.
        // Resultado: as 2 linhas do form ficam justas, sem o "abismo" vertical entre elas.
        JPanel wrapperFormulario = new JPanel();
        wrapperFormulario.add(painelFormulario);
        add(wrapperFormulario, BorderLayout.CENTER);

        add(painelBotao, BorderLayout.SOUTH);

        // ===== Amarra a ação do botão Entrar =====
        // Lambda: quando o botão for clicado, chama o método privado autenticar()
        botaoEntrar.addActionListener(e -> autenticar());
    }

    // Método privado: contém a lógica de autenticação
    // Chamado pelo ActionListener do botão Entrar
    private void autenticar() {
        try {
            // 1. Lê o que foi digitado nos campos
            String login = this.loginField.getText();
            // getPassword() retorna char[] (por segurança, em vez de String)
            // — convertemos pra String pra passar ao controller
            String senha = new String(this.senhaField.getPassword());

            // 2. Pede ao controller que autentique
            Usuario usuarioAutenticado = this.controller.autenticar(login, senha);

            // 3. Reage ao resultado
            if (usuarioAutenticado != null) {
                // Sucesso: abre a tela principal e fecha esta janela de login
                new TelaPrincipal(usuarioAutenticado).setVisible(true);
                dispose(); // fecha apenas esta janela (não encerra o app)
            } else {
                // Falha: credenciais inválidas (login não existe OU senha errada)
                // Mensagem genérica por segurança — não revelar qual dos dois falhou
                JOptionPane.showMessageDialog(this,
                        "Login ou senha inválidos.",
                        "Erro de autenticação",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            // Captura qualquer erro técnico (ex: banco fora do ar)
            // Login não dispara exceções do domínio (controller.autenticar nunca lança),
            // por isso aqui basta um catch genérico
            JOptionPane.showMessageDialog(this,
                    "Erro técnico: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
