-- =====================================================
-- Seed: dados de exemplo pra testes manuais
-- gestao_projetos
-- =====================================================
-- ATENÇÃO: senhas em texto puro (ver TODO 2 em design/todos-de-evolucao.md — hash com BCrypt)
-- Este arquivo é IDEMPOTENTE: limpa as tabelas antes de inserir,
-- então pode ser rodado várias vezes sem duplicar dados.
-- =====================================================

USE gestao_projetos;

-- ----- Limpeza (na ordem certa por causa das FKs) -----
DELETE FROM equipe_membro;
DELETE FROM projeto_equipe;
DELETE FROM tarefa;
DELETE FROM projeto;
DELETE FROM equipe;
DELETE FROM usuario;

-- Reseta o AUTO_INCREMENT pra IDs ficarem previsíveis (sempre começam em 1)
ALTER TABLE usuario AUTO_INCREMENT = 1;
ALTER TABLE equipe  AUTO_INCREMENT = 1;
ALTER TABLE projeto AUTO_INCREMENT = 1;
ALTER TABLE tarefa  AUTO_INCREMENT = 1;

-- =====================================================
-- Usuários (4 — um de cada cenário útil de teste)
-- =====================================================
INSERT INTO usuario (nome, cpf, email, cargo, login, senha, perfil) VALUES
('Nathalia Zabeu', '12345678901', 'nathalia@exemplo.com', 'Gerente de Projetos', 'nathalia', '1234',  'GERENTE'),
('Admin Sistema',  '00000000000', 'admin@exemplo.com',    'Administrador',       'admin',    'admin', 'ADMINISTRADOR'),
('João Silva',     '98765432109', 'joao@exemplo.com',     'Desenvolvedor',       'joao',     '1234',  'COLABORADOR'),
('Maria Costa',    '11122233344', 'maria@exemplo.com',    'Designer',            'maria',    '1234',  'COLABORADOR');

-- =====================================================
-- Equipes (2)
-- =====================================================
INSERT INTO equipe (nome, descricao) VALUES
('Backend',   'Equipe responsável pelo desenvolvimento backend'),
('Design UX', 'Equipe de design de interface e experiência');

-- Membros das equipes
INSERT INTO equipe_membro (equipe_id, usuario_id) VALUES
(1, 3),  -- Backend: João
(2, 4);  -- Design UX: Maria

-- =====================================================
-- Projetos (2 — em status diferentes pra ver os 2 estados)
-- =====================================================
INSERT INTO projeto (nome, descricao, data_inicio_prevista, data_termino_prevista, status, gerente_id) VALUES
('Sistema de Vendas', 'Reformulação completa do sistema de vendas', '2026-06-01', '2026-12-31', 'PLANEJADO',    1),
('Portal do Cliente', 'Novo portal de autoatendimento',             '2026-05-15', '2026-09-30', 'EM_ANDAMENTO', 1);

-- Equipes alocadas aos projetos
INSERT INTO projeto_equipe (projeto_id, equipe_id) VALUES
(1, 1),  -- Sistema de Vendas → Backend
(2, 1),  -- Portal do Cliente → Backend
(2, 2);  -- Portal do Cliente → Design UX

-- =====================================================
-- Tarefas (3 — em status e prioridades variadas)
-- =====================================================
INSERT INTO tarefa (titulo, descricao, data_termino_prevista, status, prioridade, projeto_id, equipe_id, responsavel_id) VALUES
('Setup do backend',          'Configurar projeto inicial',          '2026-07-15', 'PENDENTE',     'ALTA',  1, 1, 3),
('Modelagem do banco',        'Criar diagrama ER e DDL',             '2026-08-01', 'PENDENTE',     'ALTA',  1, 1, NULL),
('Wireframes login',          'Mockups do fluxo de autenticação',    '2026-06-01', 'EM_ANDAMENTO', 'MEDIA', 2, 2, 4);

-- =====================================================
-- Credenciais pra teste manual:
--   nathalia / 1234   (Gerente)
--   admin    / admin  (Administrador)
--   joao     / 1234   (Colaborador, membro do Backend)
--   maria    / 1234   (Colaborador, membro do Design UX)
-- =====================================================
