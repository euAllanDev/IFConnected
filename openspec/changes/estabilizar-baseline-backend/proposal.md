## Why

O backend do IFConnected ainda tem uma baseline operacional fraca para evolucao segura via SDD: ha divergencias entre README e repositorio real, configuracoes sensiveis misturadas ao source, artefatos locais indevidos e checks que dependem de infraestrutura sem documentacao minima consistente. Esta change reduz ambiguidade e risco operacional sem alterar comportamento funcional do sistema.

## What Changes

- Atualizar `.gitignore` para cobrir artefatos locais previsiveis, incluindo logs de crash JVM e arquivos temporarios.
- Criar ou atualizar `.env.example` com as variaveis necessarias para rodar a infraestrutura e o backend localmente.
- Atualizar o `README` com comandos reais para subir a infraestrutura e rodar o backend deste repositorio.
- Documentar explicitamente quais checks funcionam sem servicos externos e quais dependem de Postgres/Liquibase disponiveis.
- Registrar em `docs/conventions.md` os checks minimos atuais de baseline.
- Registrar em `docs/architecture.md` que Postgres e Liquibase fazem parte do bootstrap atual da aplicacao.

## Capabilities

### New Capabilities
- `backend-baseline`: Define a baseline operacional minima, verificavel e backend-only para evolucao segura do IFConnected via SDD.

### Modified Capabilities
- None.

## Impact

- Arquivos de governanca e bootstrap local: `.gitignore`, `.env.example`, `README.md`, `docs/conventions.md` e `docs/architecture.md`.
- Sem alteracoes de API, regra de negocio, modelo de dados ou arquitetura funcional.
- Impacta a experiencia de setup local, verificacao minima e higiene do repositorio.
- Envolve principalmente bootstrap com PostgreSQL/PostGIS, Liquibase, MongoDB, Redis e MinIO no contexto de documentacao e execucao local.
