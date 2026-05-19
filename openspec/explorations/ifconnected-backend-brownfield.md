# IFConnected Backend Brownfield Exploration

Data: 2026-05-19

## Objetivo

Registrar uma leitura inicial do backend real antes da criacao de specs OpenSpec.
Este documento descreve o estado atual, os dominios identificados, decisoes arquiteturais
visiveis no codigo, riscos tecnicos e perguntas abertas.

## Escopo Observado

- Repositorio contem apenas o backend Spring Boot.
- O README descreve um frontend Next.js, mas ele nao esta presente aqui.
- O backend usa multiplos datastores e integra seguranca, cache e armazenamento de arquivos.

## Stack Operacional

- Java 17
- Spring Boot 3.2
- Maven
- PostgreSQL com PostGIS
- MongoDB
- Redis
- MinIO
- Liquibase
- Spring Security com JWT
- Login Google via validacao de token

## Dominios Principais

### Auth

- Login por email e senha via `AuthenticationManager`.
- Login Google via verificacao de token do Google e provisionamento de usuario.
- JWT emitido com subject baseado em email e claim de `id`.

### Users

- Cadastro, leitura, atualizacao de perfil e foto.
- Papel do usuario armazenado como string (`STUDENT`, potencialmente `ADMIN`).
- Cache Redis aplicado na leitura de usuario por ID.

### Campus

- Campus armazenado no PostgreSQL com geometria PostGIS.
- Seed inicial feito por `campuses.json` no startup quando a tabela esta vazia.
- Base para recomendacoes e feed regional.

### Posts

- Posts armazenados no MongoDB.
- Comentarios embutidos no documento.
- Likes embutidos no documento.
- Upload de imagem opcional via MinIO.

### Regional Feed

- Fluxo parte do campus do usuario no Postgres.
- Busca campi vizinhos via PostGIS.
- Resolve usuarios desses campi.
- Busca posts desses usuarios no MongoDB.

### Events

- Entidade JPA simples.
- Participantes representados como colecao de IDs em tabela associativa.

### Projects

- Entidade JPA simples ligada a um `user_id`.
- Imagem opcional enviada ao MinIO.

### Notifications

- Documento Mongo criado a partir de acoes sociais como like, follow e comment.

## Mapa Arquitetural Atual

```text
Controllers
  -> Services
    -> JDBC repositories for users/campus/follows
    -> JPA repositories for events/projects
    -> Mongo repositories for posts/notifications
    -> Redis cache for user reads
    -> MinIO for media uploads
```

## Decisoes Arquiteturais Ja Existentes

1. Persistencia poliglota e real, nao apenas conceitual.
2. Uso de `JdbcTemplate` para dominio relacional com maior controle de SQL e geografia.
3. Uso de JPA para entidades mais simples e localizadas.
4. Uso de MongoDB para agregados sociais com leitura e escrita simples.
5. Uso de Redis como cache de perfil de usuario.
6. Uso de MinIO para arquivos binarios fora do banco relacional.
7. Users e Campus funcionam como ancora identitaria e geografica para outros modulos.

## Fluxos Operacionais Relevantes

### Login tradicional

1. Cliente envia email e senha.
2. Spring Security autentica via `CustomUserDetailsService`.
3. `TokenService` gera JWT.
4. Resposta retorna token e dados resumidos do usuario.

### Login Google

1. Cliente envia token Google.
2. Backend valida audience.
3. Usuario e buscado por email.
4. Se nao existir, backend cria um usuario local com senha dummy criptografada.
5. Backend responde com JWT proprio e dados do usuario.

### Feed regional

1. Cliente informa `userId` e raio.
2. Backend resolve o campus do usuario.
3. PostGIS encontra campi dentro do raio.
4. Backend busca usuarios desses campi.
5. Mongo retorna posts desses usuarios.

## Riscos Tecnicos Identificados

### 1. Autorizacao fragil

Varios endpoints aceitam `userId` por path, query ou form-data, em vez de derivar a identidade
do usuario autenticado no contexto de seguranca.

Impacto:
- risco de acesso indevido a dados e acoes em nome de terceiros
- contrato de API acoplado a um modelo de confianca fraco

### 2. Segredos e defaults sensiveis no codigo de configuracao

- segredo JWT presente em `application.properties`
- `google.client.id` presente em `application.properties`
- credenciais default para bancos e MinIO expostas como fallback local

### 3. Testabilidade baixa sem infraestrutura externa

- teste existente e apenas `contextLoads`
- subida do contexto depende do Postgres acessivel
- validacao automatizada atual nao isola comportamento por dominio

### 4. Tratamento de erro generico

- `RuntimeException` e convertida genericamente para HTTP 400
- falhas de negocio, validacao, ausencia de recurso e falhas internas se misturam

### 5. Sinais de codigo em transicao

- comentarios de ajuste e correcao espalhados pelo fonte
- classes de suporte duplicadas ou residuais, como `security/SecurityBeans.java` vazia e
  `config/SecurityBeans.java` com o bean real

### 6. Logging improvisado

- uso de `System.out.println` e `System.err.println` em fluxo de autenticacao, seed e MinIO

### 7. Dependencias transversais sem isolamento forte

- controllers conhecem detalhes de persistencia e orquestram multiplas camadas
- notificacoes sao efeitos colaterais acoplados a endpoints sociais

## Lacunas Entre README, Codigo E Configuracao

1. README menciona frontend Next.js, mas o repositorio nao contem frontend.
2. README descreve ambiente completo via Docker Compose, mas o compose atual sobe apenas a infraestrutura.
3. O backend realmente implementa feed regional, mas a modelagem de `Post.location` nao aparece como eixo principal do fluxo atual.
4. O teste existente nao corresponde a uma narrativa de execucao simples ou isolada.
5. O `openspec/config.yaml` estava vazio de contexto e precisou ser inicializado para orientar futuras mudancas.

## Leitura Brownfield Do Sistema

- O centro do sistema parece ser o nucleo social: auth, users, posts, notifications e campus/feed regional.
- Events e Projects existem, mas parecem menos integrados ao fluxo social principal.
- O principal risco estrutural nao e a persistencia poliglota; e a consistencia de seguranca,
  autorizacao e confiabilidade operacional.

## Perguntas Abertas

1. O frontend ausente deve ser mantido apenas como contexto documental ou removido do escopo das futuras specs?
2. O primeiro change OpenSpec deve atacar risco transversal de autorizacao/testabilidade ou uma capacidade de negocio?
3. `events` e `projects` entram no mesmo nivel de prioridade do nucleo social?
4. O contrato atual da API deve ser preservado no curto prazo, mesmo onde ele expõe `userId` externamente?
5. O feed regional futuro deve continuar ancorado em campus do usuario ou migrar para localizacao do conteudo?

## Nao Objetivos Desta Exploracao

- Nao implementar mudancas no backend.
- Nao definir specs completas do sistema inteiro.
- Nao propor ainda um redesign amplo da arquitetura.

## Proximo Passo Sugerido

Criar o primeiro change OpenSpec em torno de um recorte pequeno e de alto impacto, por exemplo:

- hardening de autorizacao no nucleo social
- estabilizacao de testes e configuracao local
- clarificacao formal do dominio de feed regional
