package com.gestaoprojetos.exception;

// Lançada quando um campo obrigatório está nulo ou vazio.
public class CampoObrigatorioException extends GestaoProjetosException {
    public CampoObrigatorioException(String mensagem) {
        super(mensagem);
    }
}
