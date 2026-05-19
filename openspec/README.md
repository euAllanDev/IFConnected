# OpenSpec Workspace

Este diretório guarda artefatos de SDD/OpenSpec para o backend do IFConnected.

## Princípios

- O código existente é a fonte de verdade operacional.
- Este repositório contém apenas o backend Spring Boot.
- Explorações documentam o estado atual e os riscos.
- Specs futuras devem ser pequenas e focadas por capacidade.

## Estrutura Inicial

- `config.yaml`: contexto do projeto e regras para artefatos futuros.
- `explorations/`: notas de descoberta brownfield e levantamentos técnicos.

## Escopo Atual

- Backend Java 17 com Spring Boot.
- Persistência poliglota com PostgreSQL/PostGIS, MongoDB, Redis e MinIO.
- Segurança com JWT e login Google.

## Fora Do Escopo Neste Repositório

- Implementação do frontend citado no README.
- Specs completas do sistema inteiro em uma única rodada.
