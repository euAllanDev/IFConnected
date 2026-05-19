## 1. Repository Hygiene

- [x] 1.1 Atualizar `.gitignore` para cobrir logs JVM, replay logs, temporarios e outros artefatos locais previsiveis
- [x] 1.2 Revisar se as regras de ignore preservam o comportamento atual esperado para artefatos versionados intencionais

## 2. Local Bootstrap Baseline

- [x] 2.1 Criar ou atualizar `.env.example` com as variaveis necessarias para docker compose e backend local
- [x] 2.2 Atualizar `README.md` com comandos reais para subir infraestrutura e rodar o backend deste repositorio
- [x] 2.3 Documentar no `README.md` que `./mvnw.cmd -DskipTests package` funciona sem servicos externos
- [x] 2.4 Documentar no `README.md` que `./mvnw.cmd test` depende de Postgres e Liquibase disponiveis

## 3. Governance Alignment

- [x] 3.1 Atualizar `docs/conventions.md` com os checks minimos atuais e a ordem recomendada de execucao
- [x] 3.2 Atualizar `docs/architecture.md` para registrar que Postgres e Liquibase fazem parte do bootstrap atual da aplicacao

## 4. Validation

- [x] 4.1 Verificar que a change nao altera regra de negocio, modelo de dados, stack ou arquitetura funcional
- [x] 4.2 Verificar que a documentacao resultante continua backend-only e nao pressupoe frontend neste repositorio
- [x] 4.3 Validar que os comandos documentados refletem o comportamento atualmente observado para `package` e `test`
