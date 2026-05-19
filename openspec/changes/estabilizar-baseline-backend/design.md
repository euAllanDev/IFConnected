## Context

O backend do IFConnected ja possui uma arquitetura operacional definida, mas a baseline de uso local e governanca tecnica ainda esta irregular. O repositorio apresenta divergencias entre README e escopo real, artefatos locais rastreados indevidamente, ausencia de um exemplo claro de variaveis de ambiente e uma verificacao minima dependente de Postgres/Liquibase que hoje nao esta documentada de forma objetiva.

Esta change e transversal apenas no plano de documentacao, bootstrap local e higiene do repositorio. Ela nao altera comportamento funcional do backend, nao mexe em regra de negocio, nao toca modelo de dados e nao tenta desacoplar os testes da infraestrutura existente.

## Goals / Non-Goals

**Goals:**
- Tornar a baseline local do backend verificavel e coerente com o estado real do repositorio.
- Registrar comandos minimos reais para subir infraestrutura e backend.
- Tornar explicita a dependencia atual de Postgres/Liquibase para testes completos e bootstrap da aplicacao.
- Reduzir ruido local no repositorio com regras de ignore mais adequadas.
- Introduzir um `.env.example` que sirva como referencia operacional segura.

**Non-Goals:**
- Nao criar frontend nem presumir sua presenca neste repositorio.
- Nao refatorar autenticacao, services, controllers ou arquitetura interna.
- Nao alterar regra de negocio, endpoints, modelo de dados ou stack.
- Nao desacoplar o teste de contexto do Postgres nesta change.

## Decisions

### 1. Corrigir a baseline pela documentacao e pelo bootstrap, nao por refatoracao

Decisao:
- A change atuara apenas em `.gitignore`, `.env.example`, `README.md`, `docs/conventions.md` e `docs/architecture.md`.

Racional:
- O objetivo e preparar o projeto para evolucao segura via SDD sem tocar comportamento funcional.

Alternativas consideradas:
- Corrigir tambem testes, beans residuais e configuracao de runtime: rejeitado por aumentar o escopo e tocar comportamento tecnico que merece change propria.

### 2. Documentar explicitamente a ordem recomendada de execucao local

Decisao:
- O fluxo recomendado sera documentado como: `docker compose up -d` antes de testes completos ou bootstrap com Liquibase.

Racional:
- Isso reflete o estado real atual, no qual `mvn test` depende de Postgres disponivel.

Alternativas consideradas:
- Tentar fornecer um unico fluxo universal que funcione sem servicos: rejeitado porque isso nao representa a realidade atual do projeto.

### 3. Separar checks que dependem e que nao dependem de infraestrutura externa

Decisao:
- O baseline vai registrar que `./mvnw.cmd -DskipTests package` funciona sem servicos externos, enquanto `./mvnw.cmd test` depende de Postgres/Liquibase disponiveis.

Racional:
- Isso evita falso negativo para contribuidores e cria uma verificacao minima honesta.

Alternativas consideradas:
- Ocultar a dependencia de infraestrutura: rejeitado por mascarar o comportamento real.

### 4. Introduzir `.env.example` apenas como referencia, sem mudar o contrato funcional atual

Decisao:
- O arquivo example vai espelhar as variaveis relevantes do compose e do backend atual, sem tentar redesenhar a estrategia de configuracao.

Racional:
- A baseline precisa ser segura e util, mas sem alterar bootstrap ou nomes de propriedades nesta change.

Alternativas consideradas:
- Mudar nomes de propriedades e padronizacao completa agora: rejeitado por potencialmente alterar comportamento.

## Risks / Trade-offs

- [Risco] O `.env.example` refletir parcialmente a configuracao real e gerar falsa confianca.
  -> Mitigacao: limitar o arquivo ao que e de fato observado em `docker-compose.yml` e `application.properties`.

- [Risco] Atualizar README sem corrigir todos os problemas tecnicos do projeto parecer uma solucao completa.
  -> Mitigacao: declarar explicitamente limites atuais, incluindo dependencia de Postgres/Liquibase para testes completos.

- [Trade-off] A change melhora a baseline sem resolver acoplamentos estruturais.
  -> Mitigacao: tratar esta change como preparatoria para evolucao segura posterior.

## Migration Plan

1. Ajustar `.gitignore` para artefatos locais previsiveis.
2. Criar ou atualizar `.env.example` com variaveis necessarias para compose e backend.
3. Atualizar `README.md` com comandos reais de execucao local do backend.
4. Atualizar `docs/conventions.md` com checks minimos atuais.
5. Atualizar `docs/architecture.md` com a dependencia de Postgres/Liquibase no bootstrap atual.
6. Validar que o resultado documenta a baseline sem alterar comportamento funcional.

Rollback:
- Reverter apenas arquivos de documentacao e ignore/example, sem impacto em runtime.

## Open Questions

- O `.env.example` deve incluir apenas placeholders ou tambem comentarios curtos de uso?
- A limpeza de arquivos rastreados indevidos como logs JVM deve entrar nesta change ou em uma change de higiene separada?
