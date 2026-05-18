package com.gestaoprojetos.exception;

// Lançada quando uma regra de negócio é violada
// (ex: responsável não é membro da equipe, membro já está na equipe).
public class RegraDeNegocioException extends GestaoProjetosException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
