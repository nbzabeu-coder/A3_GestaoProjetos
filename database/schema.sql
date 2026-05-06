-- =====================================================
-- Schema: gestao_projetos
-- Sistema de Gestão de Projetos e Equipes (A3)
-- =====================================================

-- Apaga o banco se já existir (útil em desenvolvimento)
DROP DATABASE IF EXISTS gestao_projetos;

-- Cria o banco com charset que suporta acentos e emojis
CREATE DATABASE gestao_projetos
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- Seleciona o banco pra os próximos comandos
USE gestao_projetos;

-- =====================================================
-- Tabelas
-- =====================================================


-- =====================================================
-- Tabela: usuario
-- Descrição: Armazena informações dos usuários do sistema
-- =====================================================

CREATE TABLE usuario (
    id    INT           PRIMARY KEY AUTO_INCREMENT,
    nome  VARCHAR(100)  NOT NULL,
    cpf   CHAR(11)      NOT NULL UNIQUE,
    email VARCHAR(150)  NOT NULL UNIQUE,
    cargo VARCHAR(80)   NOT NULL,
    login VARCHAR(100)  NOT NULL UNIQUE,
    senha VARCHAR(255)  NOT NULL,
    perfil ENUM('ADMINISTRADOR', 'GERENTE', 'COLABORADOR') NOT NULL
);

-- =====================================================
-- Tabela: projeto
-- Descrição: Armazena informações dos projetos
-- =====================================================

CREATE TABLE projeto (
    id                      INT           PRIMARY KEY AUTO_INCREMENT,
    nome                    VARCHAR(100)  NOT NULL,
    descricao               TEXT          NULL,
    data_inicio_prevista    DATE          NOT NULL,
    data_inicio_real        DATE          NULL,
    data_termino_prevista   DATE          NOT NULL,
    data_termino_real       DATE          NULL,
    status ENUM('PLANEJADO', 'EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO') NOT NULL,
    gerente_id              INT           NOT NULL,
    FOREIGN KEY (gerente_id) REFERENCES usuario(id)
);

-- =====================================================
-- Tabela: equipe
-- Descrição: Armazena informações das equipes
-- =====================================================

CREATE TABLE equipe (
    id          INT          PRIMARY KEY AUTO_INCREMENT,
    nome        VARCHAR(100) NOT NULL,
    descricao   TEXT         NULL
);

-- =====================================================
-- Tabela: tarefa
-- Descrição: Armazena informações das tarefas
-- =====================================================

CREATE TABLE tarefa (
    id                    INT           PRIMARY KEY AUTO_INCREMENT,
    titulo                VARCHAR(100)  NOT NULL,
    descricao             TEXT          NULL,
    data_inicio_real      DATETIME      NULL,
    data_termino_prevista DATE          NOT NULL,
    data_termino_real     DATETIME      NULL,
    status       ENUM('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA') NOT NULL,
    prioridade   ENUM('BAIXA', 'MEDIA', 'ALTA') NOT NULL,
    projeto_id            INT           NOT NULL,
    equipe_id             INT           NOT NULL,
    responsavel_id        INT           NULL,
    FOREIGN KEY (projeto_id)     REFERENCES projeto(id),
    FOREIGN KEY (equipe_id)      REFERENCES equipe(id),
    FOREIGN KEY (responsavel_id) REFERENCES usuario(id)
);

-- =====================================================
-- Tabela Auxiliar: equipe_membro
-- Descrição: Tabela de associação entre equipes e usuários (membros)
-- =====================================================

CREATE TABLE equipe_membro (
    equipe_id  INT NOT NULL,
    usuario_id INT NOT NULL,
    PRIMARY KEY (equipe_id, usuario_id),
    FOREIGN KEY (equipe_id) REFERENCES equipe(id),
    FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- =====================================================
-- Tabela Auxiliar: projeto_equipe
-- Descrição: Tabela de associação entre projetos e equipes
-- =====================================================

CREATE TABLE projeto_equipe (
    projeto_id INT NOT NULL,
    equipe_id  INT NOT NULL,
    PRIMARY KEY (projeto_id, equipe_id),
    FOREIGN KEY (projeto_id) REFERENCES projeto(id),
    FOREIGN KEY (equipe_id) REFERENCES equipe(id)
);
