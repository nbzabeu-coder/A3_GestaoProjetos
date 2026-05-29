# TODOs de evolução (Fase 4)

> Este documento registra os **TODOs** identificados durante o desenvolvimento da A3 — decisões de design tomadas conscientemente, com escopo simplificado para a entrega atual, e a respectiva evolução planejada para a próxima fase (Fase 4 — Ajustes e Autoavaliação).
>
> O objetivo é separar o que **foi simplificado por escolha** (defensável para o escopo atual) do que **ficaria diferente em um sistema de produção**.

Cada TODO segue a mesma estrutura: o estado atual (com a justificativa para o escopo), a limitação reconhecida e a evolução planejada.

---

## TODO 1 — Permissões por perfil via `instanceof`

**Como está hoje.** A camada de view decide o que cada perfil pode fazer com checagens `instanceof Administrador`, `instanceof Gerente`, `instanceof Colaborador` em pontos centralizados (`TelaPrincipal` controla a visibilidade das abas e ações; `TelaProjeto`, `TelaEquipe` e `TelaDetalheTarefa` recebem o usuário logado e decidem o que liberar). O perfil também é descrito em `Usuario.permissoes()` (método abstrato sobrescrito em cada subclasse), usado apenas como rótulo informativo.

**Limitação reconhecida.** As regras de "o que cada perfil pode fazer" ficam espalhadas pela camada de view, não no modelo. Se um quarto perfil for introduzido, ou se uma regra mudar (ex.: gerente também excluir usuários em determinado contexto), seria necessário localizar e ajustar todos os pontos de `instanceof` no código. Esse padrão também não aproveita o polimorfismo já presente na hierarquia.

**Evolução planejada.** Mover as regras para o domínio: cada subclasse de `Usuario` teria seus próprios métodos `podeGerenciarEquipes()`, `podeExcluirUsuario(Usuario alvo)`, `podeMudarStatusTarefa(Projeto p)`, etc. Cada `instanceof` na view viraria uma chamada polimórfica direta, com a regra encapsulada onde ela pertence.

---

## TODO 2 — Senhas em texto puro

**Como está hoje.** As senhas são armazenadas no banco em texto puro (coluna `VARCHAR`) e a verificação no login compara strings diretamente (`usuario.getSenha().equals(tentativa)`).

**Limitação reconhecida.** Um vazamento do banco compromete imediatamente todas as senhas, e a prática contraria recomendações básicas de segurança (OWASP). A escolha foi feita para manter o foco da A3 nas Metas de Compreensão (POO, persistência, estruturas) sem introduzir uma dependência adicional.

**Evolução planejada.** Adicionar a biblioteca **BCrypt** (`org.mindrot:jbcrypt`) ao `pom.xml`. No cadastro, armazenar `BCrypt.hashpw(senha, BCrypt.gensalt())`; no login, comparar com `BCrypt.checkpw(tentativa, hashArmazenado)`. Para o banco já populado, um script único de migração rehasharia as senhas existentes.

---

## TODO 3 — Tradução de erros do banco

**Como está hoje.** Os erros de banco mais frequentes no uso normal são traduzidos em mensagens amigáveis pelos DAOs:

- `errorCode 1062` (violação de constraint `UNIQUE`) → *"Já existe um registro com esse login, e-mail ou CPF."*
- `errorCode 1451` (violação de chave estrangeira ao excluir) → *"Este item está vinculado a outros registros e não pode ser excluído."*

Demais `SQLException` (timeout, conexão perdida, sintaxe inválida) caem em uma mensagem genérica do tipo *"Erro ao [ação]: " + ex.getMessage()*.

**Limitação reconhecida.** Quando ocorre um erro fora dos dois códigos cobertos, o usuário vê uma mensagem técnica que não orienta a próxima ação.

**Evolução planejada.** Centralizar a tradução em uma classe utilitária (ex.: `TratadorErroSQL.amigavel(SQLException)`) cobrindo o conjunto completo de códigos de erro relevantes do MySQL — perda de conexão, deadlock, valor fora do domínio, etc. — com mensagens em português adequadas ao usuário final.

---

## TODO 4 — Consistência cruzada entre Equipe e Tarefa

**Como está hoje.** A regra "o responsável de uma tarefa precisa ser membro da equipe da tarefa" é garantida por dois pontos cooperando: a view `TelaDetalheTarefa` recarrega o combo de responsáveis sempre que a equipe é trocada, e o método `Tarefa.atribuirResponsavel(u)` valida a pertinência do usuário à equipe atual.

**Limitação reconhecida.** A invariante "tarefa coerente" não está totalmente no domínio. Se uma tarefa for criada ou alterada por um caminho que não passe pela view atual (chamada direta de controller, importação em lote, futura API), pode-se entrar em um estado inválido — a tarefa sair atualizada antes do responsável ser revalidado.

**Evolução planejada.** Mover a invariante completa para o domínio: o próprio `Tarefa.setEquipe(novaEquipe)` verificaria se o responsável atual ainda é membro da nova equipe e, caso não seja, ou rejeita a operação ou limpa o responsável automaticamente. Assim a consistência fica garantida independentemente do caminho de chamada.

---

## TODO 5 — Regras de negócio fixas no código

**Como está hoje.** Várias regras de domínio estão hardcoded — projeto sempre nasce com status `PLANEJADO`; a data de início prevista precisa ser anterior ou igual à de término; tarefa exige equipe; apenas Administrador exclui equipes; etc.

**Limitação reconhecida.** Para qualquer ajuste, é necessário recompilar e redeployar. Em um sistema real, gerentes e administradores normalmente teriam preferências configuráveis por organização ou por projeto (ex.: "tarefa sem equipe é permitida em projetos exploratórios"; "qual perfil pode excluir equipe").

**Evolução planejada.** Introduzir uma tabela `configuracao` (ou colunas JSON em uma tabela existente) com as regras editáveis. Telas administrativas permitiriam alterá-las, e a camada de domínio leria as configurações no momento da validação.

---

## Resumo

| TODO | Decisão | Estado atual | Evolução |
|---|---|---|---|
| 1 | Permissões | `instanceof` nas views | Comportamento polimórfico no domínio |
| 2 | Senhas | Texto puro | Hash com BCrypt |
| 3 | Erros de banco | 2 códigos cobertos | Tradução completa em utilitário |
| 4 | Consistência Equipe↔Tarefa | View + parte do domínio | Invariante completa no domínio |
| 5 | Regras de negócio | Hardcoded | Configuráveis em tempo de execução |
