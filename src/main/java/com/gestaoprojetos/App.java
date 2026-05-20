// Package raiz do projeto — é onde fica a classe App, o ponto de entrada
package com.gestaoprojetos;

// Importando a tela de login — primeira janela que o usuário vê ao abrir o app
import com.gestaoprojetos.view.TelaLogin;

// SwingUtilities: utilitário do Swing pra agendar código na thread correta
import javax.swing.SwingUtilities;

// Classe App: ponto de entrada do programa
// (o método main é o que a JVM executa quando você roda `mvn exec:java`)
public class App {

    public static void main(String[] args) {
        // invokeLater agenda a criação da janela pra rodar na thread do Swing
        // (Event Dispatch Thread — EDT). É boa prática em qualquer app Swing.
        SwingUtilities.invokeLater(() -> {
            TelaLogin tela = new TelaLogin();
            tela.setVisible(true);
        });
    }
}
