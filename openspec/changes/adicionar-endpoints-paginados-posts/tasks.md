## 1. Paginated Contract

- [x] 1.1 Definir o DTO de resposta paginada para posts com itens e metadados
- [x] 1.2 Definir os parâmetros suportados pelos novos endpoints paginados, incluindo defaults e validações mínimas

## 2. Repository And Service Support

- [x] 2.1 Adaptar o acesso ao MongoDB para suportar consultas paginadas com ordenação explícita por `createdAt` decrescente
- [x] 2.2 Adicionar o suporte mínimo de serviço necessário para feeds geral, por usuário e por amigos paginados
- [x] 2.3 Preservar no feed regional paginado a sequência atual de buscar `userIds` no Postgres antes da consulta de posts no MongoDB

## 3. Controller Endpoints

- [x] 3.1 Adicionar endpoints paginados paralelos para posts gerais sem alterar o endpoint legado
- [x] 3.2 Adicionar endpoints paginados paralelos para posts por usuário sem alterar o endpoint legado
- [x] 3.3 Adicionar endpoints paginados paralelos para feed de amigos e feed regional sem alterar os endpoints legados

## 4. Documentation And Validation

- [x] 4.1 Documentar o novo contrato paginado em README ou docs apropriado
- [x] 4.2 Validar que os endpoints antigos continuam retornando `List<Post>` sem alteração de contrato
- [x] 4.3 Adicionar testes automatizados ou registrar validação manual mínima caso o baseline de testes impeça cobertura adequada nesta change
