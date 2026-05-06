# Diagrama de Classes

> Modelo de domínio do Sistema de Gestão de Projetos e Equipes.
> Diagrama em Mermaid — renderiza nativamente no VS Code (`Cmd+Shift+V`) e no GitHub.

```mermaid

classDiagram
    direction BT

    class Usuario {
        <<abstract>>
        -nome: String
        -cpf: String
        -email: String
        -cargo: String
        -login: String
        -senha: String
        +conferirSenha(tentativa: String) boolean
        +permissoes() String*
    }


    class Administrador {
        +permissoes() String
    }

    class Gerente {
        +permissoes() String
    }

    class Colaborador {
        +permissoes() String
    }

    class Projeto {
        -nome: String
        -descricao: String
        -dataInicioPrevista: LocalDate
        -dataInicioReal: LocalDate
        -dataTerminoPrevista: LocalDate
        -dataTerminoReal: LocalDate
        -status: StatusProjeto
        -gerente: Gerente
        -tarefas: List~Tarefa~
        -equipes: List~Equipe~
        +iniciar() void
        +concluir() void
        +cancelar() void
        +adicionarTarefa(t: Tarefa) void
        +adicionarEquipe(e: Equipe) void
        +gerarRelatorio() String
    }

    class Equipe {
        -nome: String
        -descricao: String
        -membros: List~Usuario~
        +adicionarMembro(u: Usuario) void
        +removerMembro(u: Usuario) void
        +listarMembros() List~Usuario~
    }

    class Tarefa {
        -titulo: String
        -descricao: String
        -dataInicioReal: LocalDateTime
        -dataTerminoPrevista: LocalDate
        -dataTerminoReal: LocalDateTime
        -status: StatusTarefa
        -prioridade: Prioridade
        -equipe: Equipe
        -responsavel: Usuario
        +atribuirResponsavel(u: Usuario) void
        +removerResponsavel() void
        +iniciar() void
        +concluir() void
        +reabrir() void
    }

    class StatusProjeto {
        <<enumeration>>
        PLANEJADO
        EM_ANDAMENTO
        CONCLUIDO
        CANCELADO
    }

    class StatusTarefa {
        <<enumeration>>
        PENDENTE
        EM_ANDAMENTO
        CONCLUIDA
    }

    class Prioridade {
        <<enumeration>>
        BAIXA
        MEDIA
        ALTA
    }

    class Relatorio {
        <<interface>>
        +gerar() String
        +exportar(formato: String) void
    }

    class RelatorioDeProjeto {
        +gerar() String
        +exportar(formato: String) void
    }

    class RelatorioDeEquipe {
        +gerar() String
        +exportar(formato: String) void
    }

    class RelatorioDeColaborador {
        +gerar() String
        +exportar(formato: String) void
    }


Usuario <|-- Administrador
Usuario <|-- Gerente
Usuario <|-- Colaborador
Relatorio <|.. RelatorioDeProjeto
Relatorio <|.. RelatorioDeEquipe
Relatorio <|.. RelatorioDeColaborador
Tarefa "*" --> "0..1" Usuario : responsavel
Tarefa "*" --> "1" Equipe : pertence
Projeto "*" --> "1" Gerente : associação
Projeto "1" *-- "*" Tarefa : composição
Projeto "*" o-- "*" Equipe : agregação
Equipe "*" o-- "*" Usuario : membros
Projeto ..> StatusProjeto
Tarefa ..> StatusTarefa
Tarefa ..> Prioridade

```
