## Why

O repositório ja tem um backend operacional relevante, mas ainda nao tem uma constituicao de projeto que registre o contexto estavel necessario para orientar futuras mudancas brownfield. Sem esse baseline, novas changes tendem a repetir descoberta, assumir escopos incorretos ou misturar comportamento real com intencoes antigas do README.

## What Changes

- Criar a primeira change de governanca para registrar a constituicao do projeto IFConnected no estado atual do backend.
- Definir e documentar missao, stack, arquitetura, dominio, convencoes e roadmap em artefatos leves e estaveis.
- Atualizar `openspec/config.yaml` para refletir o escopo real deste repositorio e a regra de iniciar por `/opsx:explore` antes de implementacoes.
- Criar `CLAUDE.md` na raiz com orientacoes operacionais de colaboracao para futuras mudancas.
- Registrar explicitamente que este repositorio contem o backend Spring Boot e que o frontend citado no README nao esta presente aqui.
- Registrar a persistencia poliglota como decisao arquitetural central.

## Capabilities

### New Capabilities
- `project-constitution`: Define a documentacao minima e verificavel de governanca para orientar futuras mudancas no backend brownfield do IFConnected.

### Modified Capabilities
- None.

## Impact

- Artefatos de documentacao na raiz, em `docs/` e em `openspec/`.
- Sem mudancas funcionais em APIs do backend nesta change.
- Impacta o processo de trabalho futuro ao padronizar descoberta, escopo e contexto brownfield.
- Afeta indiretamente todos os modulos porque a constituicao referencia a persistencia poliglota, seguranca e limites de escopo do repositorio.
