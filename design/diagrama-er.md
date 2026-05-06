# Diagrama Entidade-Relacionamento (ER)

> Modelo do banco de dados do Sistema de Gestão de Projetos e Equipes.
> Diagrama em Mermaid — renderiza nativamente no VS Code (`Cmd+Shift+V`) e no GitHub.

```mermaid
erDiagram
    
    USUARIO {
        int id PK
        varchar nome
        char cpf UK
        varchar email UK
        varchar cargo
        varchar login UK
        varchar senha
        enum perfil
    }

    PROJETO {
        int id PK
        varchar nome
        text descricao
        date data_inicio_prevista
        date data_inicio_real
        date data_termino_prevista
        date data_termino_real
        enum status
        int gerente_id FK
    }

    EQUIPE {
        int id PK
        varchar nome
        text descricao
    }

    TAREFA {
        int id PK
        varchar titulo
        text descricao
        datetime data_inicio_real
        date data_termino_prevista
        datetime data_termino_real
        enum status
        enum prioridade
        int projeto_id FK
        int equipe_id FK
        int responsavel_id FK
    }

    EQUIPE_MEMBRO {
        int equipe_id PK,FK
        int usuario_id PK,FK
    }

    PROJETO_EQUIPE {
        int projeto_id PK,FK
        int equipe_id PK,FK
    }

    USUARIO ||--o{ PROJETO : "gerencia"
    PROJETO ||--o{ TAREFA : "contém"
    EQUIPE ||--o{ TAREFA : "pertence"
    USUARIO |o--o{ TAREFA : "responsável"
    EQUIPE ||--o{ EQUIPE_MEMBRO : "tem"
    USUARIO ||--o{ EQUIPE_MEMBRO : "participa"
    PROJETO ||--o{ PROJETO_EQUIPE : "aloca"
    EQUIPE ||--o{ PROJETO_EQUIPE : "atua em"


```
