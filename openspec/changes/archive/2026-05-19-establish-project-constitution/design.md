## Context

O backend do IFConnected ja opera com uma arquitetura brownfield relevante, mas o repositorio ainda nao possui uma constituicao de projeto explicita para orientar futuras changes. O estado atual esta distribuido entre codigo, README e configuracoes, com algumas lacunas de escopo, especialmente porque o README cita um frontend que nao esta presente neste repositorio.

A mudanca e transversal porque define a base documental que sera usada por todas as proximas propostas, designs, specs e implementacoes. Ela nao altera comportamento funcional do backend, mas introduz uma camada de governanca para reduzir ambiguidade de escopo e estabilizar o contexto compartilhado.

## Goals / Non-Goals

**Goals:**
- Registrar o contexto estavel minimo do projeto sem tentar reconstruir todas as funcionalidades existentes.
- Definir documentos de referencia para missao, stack, roadmap, dominio, arquitetura e convencoes.
- Atualizar o contexto do OpenSpec para refletir o escopo real do repositorio e o processo esperado de exploracao antes de implementar.
- Tornar explicitas as decisoes centrais ja visiveis no codigo, incluindo o backend Spring Boot e a persistencia poliglota.

**Non-Goals:**
- Nao documentar exaustivamente todos os endpoints, entidades e fluxos existentes.
- Nao criar specs funcionais completas para auth, users, posts, events ou outros dominios.
- Nao alterar o design de runtime do backend nesta change.
- Nao deduzir ou inventar detalhes do frontend ausente.

## Decisions

### 1. Criar uma constituicao documental leve, nao um manual completo

Decisao:
- A constituicao sera composta por poucos arquivos estaveis: `mission.md`, `tech-stack.md`, `roadmap.md`, `CLAUDE.md`, `docs/domain.md`, `docs/architecture.md`, `docs/conventions.md` e `openspec/config.yaml`.

Racional:
- Isso oferece base suficiente para orientar futuras changes sem impor engenharia reversa completa do sistema.

Alternativas consideradas:
- Documentar tudo em um unico arquivo: rejeitado por dificultar manutencao e mistura de temas.
- Criar specs completas por dominio agora: rejeitado por inflar escopo e conflitar com a regra de nao especar o sistema inteiro em uma rodada.

### 2. Tratar o codigo atual como fonte de verdade operacional

Decisao:
- Os documentos devem registrar comportamento e limites observaveis hoje, mesmo que existam incoerencias com o README.

Racional:
- Em brownfield, a confiabilidade da governanca depende de refletir o sistema que existe, nao o sistema idealizado.

Alternativas consideradas:
- Priorizar a narrativa do README: rejeitado porque o repositorio nao contem o frontend citado e algumas descricoes sao mais amplas do que o estado confirmado no codigo.

### 3. Registrar o escopo do repositorio como backend-only

Decisao:
- A constituicao deve declarar explicitamente que este ZIP/repositorio contem o backend Spring Boot apenas.

Racional:
- Isso evita specs futuras com dependencia implicita de um frontend ausente.

Alternativas consideradas:
- Manter escopo implicito: rejeitado por perpetuar ambiguidade.

### 4. Elevar a persistencia poliglota a decisao arquitetural central

Decisao:
- A arquitetura deve registrar formalmente o uso de PostgreSQL/PostGIS, MongoDB, Redis e MinIO como uma escolha central do projeto.

Racional:
- Essa decisao influencia modelagem, testes, integracao e o desenho de qualquer change futura.

Alternativas consideradas:
- Tratar cada datastore como detalhe isolado: rejeitado porque isso esconde a natureza sistêmica da solucao atual.

### 5. Formalizar `/opsx:explore` como porta de entrada para futuras mudancas

Decisao:
- A constituicao e o `openspec/config.yaml` devem registrar que mudancas futuras comecam por exploracao antes de qualquer implementacao.

Racional:
- O projeto e brownfield, com riscos de autorizacao, acoplamento e lacunas de documentacao. A exploracao inicial reduz erro de escopo.

Alternativas consideradas:
- Permitir specs ou implementacao direta sem exploracao: rejeitado para este contexto brownfield.

## Risks / Trade-offs

- [Risco] A constituicao ficar generica demais e nao orientar decisoes futuras.
  -> Mitigacao: limitar os documentos ao contexto estavel observavel e incluir convencoes operacionais claras.

- [Risco] A documentacao envelhecer rapidamente.
  -> Mitigacao: escrever apenas fatos estruturais e regras de processo, evitando catalogo detalhado de implementacao.

- [Risco] A change ser confundida com redesign do sistema.
  -> Mitigacao: reforcar nao objetivos e manter specs no nivel de governanca minima.

- [Trade-off] Menos detalhe agora reduz cobertura descritiva imediata.
  -> Mitigacao: futuras changes podem aprofundar dominios especificos a partir desta base.

## Migration Plan

1. Criar e preencher os documentos da constituicao na raiz, em `docs/` e em `openspec/`.
2. Atualizar `openspec/config.yaml` com contexto, escopo e regras de processo.
3. Validar se os documentos registram apenas o backend e a arquitetura confirmada no codigo.
4. Usar a nova constituicao como referencia para a proxima change funcional.

Rollback:
- Remover ou reverter apenas os arquivos de documentacao criados por esta change, sem impacto em runtime.

## Open Questions

- O roadmap inicial deve priorizar reducao de risco transversal ou capacidades de negocio visiveis ao usuario?
- O frontend ausente deve continuar apenas como nota de contexto ou ser removido do README em uma change futura?
- A governanca futura devera incluir padrao de versionamento de API, ou isso fica para uma change posterior?
