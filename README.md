# Sistema de Gestão de Projetos e Equipes

Aplicação desktop em Java para gerenciar projetos, equipes, tarefas e usuários, com persistência em banco MySQL. O sistema cobre todo o ciclo: cadastro e autenticação de usuários com **perfis distintos** (Administrador, Gerente, Colaborador), criação de equipes e projetos, alocação de equipes a projetos (N:N), gestão de tarefas com responsável e ciclo de vida (PENDENTE → EM_ANDAMENTO → CONCLUIDA), e geração de **relatórios** de projeto, equipe e colaborador exportáveis pra `.txt`.

Cada perfil enxerga o sistema de um jeito diferente — o **Administrador** supervisiona tudo, o **Gerente** acompanha os projetos sob sua responsabilidade, e o **Colaborador** trabalha nas suas tarefas dentro dos projetos em que participa.

> Trabalho **A3** da disciplina de Desenvolvimento de Soluções Computacionais (curso de Sistemas de Informação EAD da Anhembi Morumbi, em parceria com Oracle).

---

## 🧰 Tecnologias

- **Java 25** (linguagem)
- **Swing** (interface gráfica)
- **MySQL 8+** (persistência)
- **JDBC** (acesso ao banco)
- **Maven** (build e gerenciamento de dependências)

Arquitetura **MVC + DAO**: Model (`com.gestaoprojetos.model`), Controller (`...controller`), View (`...view`), DAO (`...dao`) e um pacote `relatorio` separado pra a feature de relatórios.

---

## ✅ Pré-requisitos

- **JDK 25** (ou superior) instalado e no `PATH`.
- **Apache Maven 3.9+** instalado.
- **MySQL Server 8+** rodando em `localhost:3306`.

---

## 🚀 Como rodar

### 1. Clonar o repositório

```bash
git clone https://github.com/nbzabeu-coder/A3_GestaoProjetos.git
cd A3_GestaoProjetos
```

### 2. Criar o banco e popular com dados de teste

Os dois scripts SQL estão em `database/`:

```bash
# Cria o banco "gestao_projetos" e as tabelas (rode só na primeira vez)
mysql -u root -p < database/schema.sql

# Popula com 4 usuários, 2 equipes, 2 projetos e 3 tarefas pra teste
mysql -u root -p < database/seed.sql
```

> 💡 O `seed.sql` é **idempotente** — pode ser rodado quantas vezes precisar pra voltar ao estado conhecido.

### 3. Configurar credenciais do banco

O arquivo `src/main/resources/config.properties` é **ignorado pelo Git** (pra não vazar senhas). Pra criá-lo, copie o template e edite com a sua senha do MySQL:

```bash
cp src/main/resources/config.example.properties src/main/resources/config.properties
```

Depois abra o `config.properties` e preencha o campo `db.password=` com a sua senha. Se quiser, ajuste também `db.url` e `db.user`.

### 4. Rodar o sistema

```bash
mvn exec:java
```

A primeira execução vai baixar as dependências (`mysql-connector-j` etc.) — pode demorar um pouco. Depois é instantâneo. Vai abrir a tela de login.

---

## 🔑 Credenciais de teste (do seed)

| Login | Senha | Perfil |
|---|---|---|
| `admin` | `admin` | Administrador |
| `nathalia` | `1234` | Gerente |
| `joao` | `1234` | Colaborador (membro do Backend) |
| `maria` | `1234` | Colaborador (membro do Design UX) |

> 💡 Pra **trocar de usuário** sem reiniciar, clique no botão **Sair** no canto superior direito.

---

## ✨ Funcionalidades principais

- **Autenticação** com diferenciação por perfil (RBAC).
- **CRUD completo** de usuários, equipes, projetos e tarefas.
- **Ciclo de vida** com regras de transição (projeto: Planejado → Em andamento → Concluído / Cancelado; tarefa: Pendente ↔ Em andamento ↔ Concluída).
- **Alocação de equipes a projetos** (relação N:N).
- **Aba Início** que se adapta ao perfil:
  - Administrador → **Resumo do sistema** com totais e porcentagens por status.
  - Gerente → **Meus Projetos** (gerencia ∪ participa) + acompanhamento das tarefas.
  - Colaborador → projetos em que participa + **fila de prioridade** das tarefas a fazer.
- **Relatórios** de projeto, equipe e colaborador, com **exportação `.txt`** pra `relatorios/`.
- **Listagens com ordenação** (clique nos cabeçalhos) e **busca global** (campo Buscar na aba Projetos).
- **Mensagens de erro amigáveis** (login duplicado, FK constraint, datas inválidas).

---

## 📌 Sobre a pilha (estrutura não usada)

As Metas do trabalho mencionam o uso de **pilhas, filas e listas**. No sistema uso:

- **Listas** (`List`/`ArrayList`) em todas as coleções do domínio (membros de equipes, equipes alocadas, tarefas de um projeto, etc.).
- **Fila de prioridade** (`PriorityQueue`) na aba Início, ordenando as tarefas "a fazer" pela prioridade (ALTA → MEDIA → BAIXA).

Para a **pilha** (LIFO — *Last In, First Out*), avaliei o domínio e **não encontrei um uso natural**: projetos, equipes, tarefas e usuários são coleções onde a ordem de chegada não importa (listas) ou onde importa a prioridade (fila). Forçar uma pilha apenas para "marcar presença" seria artificial. Saber **quando não usar** uma estrutura também faz parte de bom projeto — registrei a decisão de forma consciente.

---

## 📁 Estrutura do projeto

```
A3_GestaoProjetos/
├── database/
│   ├── schema.sql               # criação das tabelas
│   └── seed.sql                 # dados de exemplo (idempotente)
├── design/
│   ├── diagrama-er.md           # diagrama entidade-relacionamento (Mermaid)
│   ├── diagrama-classes.md      # diagrama de classes (Mermaid)
│   └── fluxograma-autenticacao.md  # algoritmo de login em 3 formas
├── src/main/
│   ├── java/com/gestaoprojetos/
│   │   ├── App.java             # ponto de entrada
│   │   ├── model/               # entidades de domínio + enums + exceções
│   │   ├── controller/          # orquestração entre View e DAO
│   │   ├── dao/                 # acesso ao banco (JDBC)
│   │   ├── view/                # telas Swing
│   │   └── relatorio/           # interface Relatorio + 3 implementações + ExportadorTxt
│   └── resources/
│       └── config.example.properties
├── pom.xml
└── README.md
```

Os relatórios exportados ficam em `relatorios/` (criada automaticamente; gitignored).

---

## 📐 Diagramas

- [`design/diagrama-er.md`](design/diagrama-er.md) — modelo do banco (tabelas, PKs, FKs, relações N:N).
- [`design/diagrama-classes.md`](design/diagrama-classes.md) — modelo de domínio (herança `Usuario`, interface `Relatorio`, hierarquia de exceções).
- [`design/fluxograma-autenticacao.md`](design/fluxograma-autenticacao.md) — o algoritmo de login em **três representações**: pseudocódigo, fluxograma e código Java.

> 💡 Os diagramas Mermaid renderizam direto no GitHub e no preview do VS Code (`Cmd+Shift+V` no macOS · `Ctrl+Shift+V` no Windows e Linux).

---

## 👤 Autora

**Nathalia Zabeu** — Sistemas de Informação (EAD), Anhembi Morumbi.
