package com.gestaoprojetos.exception;

// Lançada quando uma transição de estado não é permitida
// (ex: concluir uma tarefa que não está em andamento).
public class TransicaoInvalidaException extends GestaoProjetosException {
    public TransicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
