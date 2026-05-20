// Package: organiza a classe dentro do pacote view (camada de apresentação)
package com.gestaoprojetos.view;

// Importando classes do Swing
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

// Importando o modelo — a tela principal precisa saber qual usuário está logado
import com.gestaoprojetos.model.Usuario;

// Classe TelaPrincipal: janela principal do sistema, exibida após login bem-sucedido
// STUB (sub-passo 9.1): por enquanto só uma saudação.
// Conteúdo real (JTabbedPane com abas de CRUD) virá no sub-passo 9.2.
public class TelaPrincipal extends JFrame {

    // Atributo: guarda o usuário autenticado durante toda a vida da janela
    // (no sub-passo 9.2 será usado pra decidir quais abas exibir conforme permissões)
    private Usuario usuarioLogado;

    // Construtor: recebe o usuário autenticado vindo da TelaLogin
    public TelaPrincipal(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;

        // Configurações básicas da janela
        setTitle("Sistema de Gestão de Projetos — Principal");
        setSize(800, 600);
        setLocationRelativeTo(null); // centraliza na tela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Saudação temporária (STUB)
        // SwingConstants.CENTER alinha o texto no centro do label
        JLabel saudacao = new JLabel(
                "Bem-vinda, " + this.usuarioLogado.getNome() + "!",
                SwingConstants.CENTER);
        add(saudacao);
    }

    // Getter — útil pra outras telas (CRUDs do 9.2) consultarem quem está logado
    public Usuario getUsuarioLogado() {
        return this.usuarioLogado;
    }
}
