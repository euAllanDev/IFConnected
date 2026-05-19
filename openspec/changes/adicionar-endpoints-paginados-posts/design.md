## Context

O modulo de posts hoje expõe endpoints de listagem que retornam `List<Post>` diretamente, sem paginação e sem ordenação explícita no contrato. Isso funciona para o estado atual, mas cria risco de inconsistência e crescimento ruim para feeds maiores. Em brownfield, substituir os contratos existentes por respostas paginadas quebraria consumidores, especialmente porque o frontend nao esta presente neste repositorio para alinhamento imediato.

O feed regional adiciona uma restricao importante: ele depende primeiro da busca de `userIds` no Postgres/PostGIS para depois consultar posts no MongoDB. Qualquer paginação nova precisa preservar essa sequência de integração.

## Goals / Non-Goals

**Goals:**
- Adicionar suporte a paginação para feeds de posts sem alterar os endpoints existentes.
- Definir ordenação explícita padrão para evitar resultados inconsistentes entre páginas.
- Introduzir um contrato novo e explícito de resposta paginada com metadados.
- Preservar o fluxo atual do feed regional, incluindo a dependência de `userIds` vindos do Postgres.
- Registrar que a eventual migração dos consumidores antigos para os novos endpoints depende de alinhamento com o frontend.

**Non-Goals:**
- Nao alterar o contrato dos endpoints atuais.
- Nao trocar `List<Post>` por `Page<Post>` nas rotas legadas.
- Nao refatorar todo o modulo de posts.
- Nao alterar regra de negocio, autenticacao, modelo de dados ou contratos antigos de API.
- Nao implementar frontend nem assumir mudanças no consumidor atual.

## Decisions

### 1. Escolher endpoints paginados paralelos em vez de mudar os endpoints atuais

Decisao:
- A change criara novos endpoints paginados paralelos para os feeds de posts.

Racional:
- Preserva consumidores existentes e permite introduzir um contrato novo com metadados sem ruptura de API.

Alternativas consideradas:
- Opcao 1: adicionar `page` e `size` aos endpoints atuais mantendo `List<Post>`.
  - rejeitada porque cria ambiguidade de contrato e nao fornece metadados claros.
- Opcao 2: mudar os endpoints atuais para resposta paginada.
  - rejeitada porque quebraria o frontend e outros consumidores existentes.
- Opcao 3: criar endpoints paginados paralelos.
  - escolhida por isolar a mudança e permitir migração gradual.

### 2. Definir ordenação padrão explícita por `createdAt` decrescente

Decisao:
- Os novos endpoints terao ordenação padrão por data de criação decrescente.

Racional:
- Paginação sem ordenação explícita gera resultados instáveis e potencial duplicação ou omissão visual entre páginas.

Alternativas consideradas:
- Nenhuma ordenação padrão: rejeitada por inconsistência.
- Ordenação por id: rejeitada porque `createdAt` comunica melhor a semântica de feed.

### 3. Usar parâmetros `page`, `size` e `sort` apenas nos novos endpoints

Decisao:
- Os endpoints paginados aceitarão parâmetros explícitos de paginação, com defaults seguros.

Racional:
- Isso evita contaminar o contrato legado e deixa a paginação nova autodescrita.

Alternativas consideradas:
- Cursor-based pagination agora: rejeitada por ser uma mudança maior do que o recorte pedido.

### 4. Introduzir um DTO de resposta paginada

Decisao:
- A resposta nova será um envelope explícito com itens e metadados de paginação.

Racional:
- Isso diferencia claramente os endpoints novos dos antigos e expõe informação útil para consumidores futuros.

Alternativas consideradas:
- Retornar `Page<Post>` diretamente: rejeitada para evitar vazar detalhes de infraestrutura no contrato.
- Retornar apenas `List<Post>` nos endpoints novos: rejeitada por não resolver o problema de metadados.

### 5. Preservar o fluxo regional atual antes de paginar

Decisao:
- O feed regional paginado continuará buscando `userIds` no Postgres antes da consulta de posts no MongoDB.

Racional:
- Isso mantém a regra atual do sistema e evita regressão funcional no recorte regional.

Alternativas consideradas:
- Tentar paginar diretamente do lado relacional ou redesenhar a estratégia regional: rejeitada por fugir do escopo.

## Risks / Trade-offs

- [Risco] Duplicar endpoints aumenta a superfície de API temporariamente.
  -> Mitigacao: documentar claramente que os antigos permanecem por compatibilidade e que a migração futura depende do frontend.

- [Risco] `findByUserIdIn(...)` paginado exigir adaptação maior no acesso ao Mongo.
  -> Mitigacao: limitar a mudança ao necessário e definir ordenação padrão clara desde o início.

- [Trade-off] Endpoints paralelos evitam quebra, mas postergam consolidação de contrato.
  -> Mitigacao: registrar no design que a migração futura depende de alinhamento com o frontend.

- [Risco] Testes automatizados completos serem difíceis no baseline atual.
  -> Mitigacao: prever validação manual mínima caso os testes dependam da infraestrutura completa.

## Migration Plan

1. Adicionar endpoints paginados paralelos no controller de posts.
2. Introduzir suporte mínimo de paginação no repository/service necessário para os feeds afetados.
3. Definir e usar DTO de resposta paginada.
4. Documentar o novo contrato em README ou docs apropriado.
5. Validar que endpoints antigos continuam intactos.

Rollback:
- Remover apenas os novos endpoints paginados e DTOs associados, sem afetar os endpoints legados.

## Open Questions

- Quais defaults de `page` e `size` devem ser usados inicialmente?
- O parâmetro `sort` será exposto livremente ou apenas aceito com um conjunto mínimo controlado?
- A documentação do novo contrato deve ficar no README ou em documento dedicado de API interna?

## Frontend Dependency

Qualquer futura migração dos endpoints antigos para os paginados depende de alinhamento com o frontend, que nao esta presente neste repositorio.
