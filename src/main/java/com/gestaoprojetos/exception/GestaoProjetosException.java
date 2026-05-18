package com.gestaoprojetos.exception;

// Exceção base de todas as exceções de regra de negócio do sistema.
// É abstrata: nunca é lançada diretamente — sempre via uma subclasse específica.
// Estende RuntimeException, portanto é unchecked (não obriga try/catch).
public abstract class GestaoProjetosException extends RuntimeException {

    public GestaoProjetosException(String mensagem) {
        super(mensagem);
    }
}
