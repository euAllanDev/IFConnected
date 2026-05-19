## Why

O modulo de posts hoje expõe feeds sem paginação e sem ordenação explícita no contrato, o que limita evolução segura para listas maiores e infinite scroll. Em brownfield, trocar o retorno dos endpoints atuais quebraria consumidores existentes, então a paginação precisa entrar por endpoints paralelos e explícitos.

## What Changes

- Introduzir endpoints paginados paralelos para os feeds de posts, preservando integralmente os endpoints atuais e seus contratos baseados em `List<Post>`.
- Definir um contrato paginado explícito com metadados, parâmetros de paginação e ordenação padrão estável.
- Registrar ordenação explícita padrão, preferencialmente por `createdAt` decrescente, para evitar inconsistência entre páginas.
- Preservar a lógica atual do feed regional, incluindo a busca de `userIds` no Postgres antes da consulta de posts no MongoDB.
- Documentar que qualquer futura migração dos endpoints antigos para os paginados depende de alinhamento com o frontend, que não está presente neste repositório.

## Capabilities

### New Capabilities
- `paginated-post-feeds`: Define endpoints paginados paralelos para feeds de posts sem alterar os contratos legados existentes.

### Modified Capabilities
- None.

## Impact

- `PostController`, `PostRepository` e possivelmente `GeoFeedService` no recorte mínimo necessário.
- Novo DTO de resposta paginada para posts, se necessário.
- Documentação técnica em README ou docs apropriado para o novo contrato.
- Dependência externa registrada para frontend, sem implementação neste repositório.
- Impacta PostgreSQL/PostGIS e MongoDB no fluxo regional, porque a seleção regional cruza ambos os armazenamentos.
