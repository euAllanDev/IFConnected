Aqui está uma versão profissional, robusta e acadêmica do README.md.

Ele foi estruturado não apenas para dizer "o que é", mas para justificar as escolhas arquiteturais, explicando o porquê de cada banco de dados (Persistência Poliglota), como foi a implementação técnica e como rodar tudo.

Copie o código abaixo e salve como README.md na raiz do seu projeto.

code
Markdown
download
content_copy
expand_less
# 🌐 IFConnected
### A Rede Social Acadêmica Geo-Localizada

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Next.js](https://img.shields.io/badge/Next.js-14-black?style=for-the-badge&logo=next.js&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-PostGIS-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-NoSQL-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![MinIO](https://img.shields.io/badge/MinIO-Object_Storage-c72c48?style=for-the-badge&logo=minio&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

---

## 📖 Sobre o Projeto

**IFConnected** é uma plataforma de rede social desenvolvida para conectar estudantes dos Institutos Federais (IFs). 

O grande diferencial do projeto é o uso de **Inteligência Geográfica**. Ao contrário de redes sociais tradicionais, o IFConnected sugere conexões e conteúdos baseados na proximidade física entre os Campi, utilizando cálculos espaciais no banco de dados para integrar alunos de cidades vizinhas.

Este projeto serve como um estudo de caso avançado sobre **Persistência Poliglota** (Polyglot Persistence), demonstrando como orquestrar múltiplos tipos de bancos de dados (Relacional, NoSQL, Cache e Espacial) em uma única aplicação.

---

## 🏗️ Arquitetura e Decisões Técnicas

O sistema foi desenhado seguindo uma arquitetura híbrida, onde cada tecnologia foi escolhida para resolver um problema específico de performance ou modelagem de dados.

### 1. PostgreSQL + PostGIS (Dados Relacionais e Espaciais)
*   **Propósito:** Gerenciar dados críticos que exigem integridade referencial (ACID) e realizar cálculos geográficos complexos.
*   **O que armazena:** Usuários, Relacionamentos (Seguidores), Campi e Eventos.
*   **Implementação:**
    *   Utilizamos **JDBC Template** para `Users` e `Campus` para ter controle total sobre as queries SQL.
    *   Utilizamos a extensão **PostGIS** para armazenar coordenadas geográficas (`GEOMETRY`) e executar funções como `ST_DWithin` (busca por raio).
    *   Utilizamos **JPA/Hibernate** para o módulo de `Events`, simplificando o mapeamento de tabelas associativas (`event_participants`).

### 2. MongoDB (Dados Volumosos e Não-Estruturados)
*   **Propósito:** Garantir alta performance de escrita e leitura para dados que crescem exponencialmente e possuem estrutura flexível.
*   **O que armazena:** Publicações (Feed), Comentários e Notificações.
*   **Implementação:**
    *   Os posts contêm documentos aninhados (Comentários), o que seria custoso fazer com JOINs em SQL.
    *   As notificações são geradas via gatilhos no código Java e salvas como documentos JSON para leitura rápida.

### 3. Redis (Cache In-Memory)
*   **Propósito:** Reduzir a latência e a carga no banco de dados relacional para dados muito acessados.
*   **O que armazena:** Perfis de Usuário.
*   **Implementação:**
    *   Utilizamos a anotação `@Cacheable` do Spring. Ao buscar um usuário, o sistema verifica primeiro no Redis (milissegundos). Se não achar, busca no Postgres e salva no cache.
    *   Utilizamos `@CacheEvict` para invalidar o cache quando o usuário atualiza o perfil.

### 4. MinIO (Object Storage S3)
*   **Propósito:** Armazenar arquivos binários (imagens) fora do banco de dados, mantendo o banco leve.
*   **O que armazena:** Fotos de perfil e imagens das publicações.
*   **Implementação:**
    *   Simula um ambiente AWS S3 localmente via Docker.
    *   O Java recebe o arquivo, envia para o MinIO e salva apenas a URL pública no banco de dados.

### 5. Docker Compose (Orquestração)
*   **Propósito:** Garantir que todo o ambiente (4 bancos de dados + Aplicação) suba com um único comando, independente do sistema operacional.

---

## 💻 Frontend (Next.js)

A interface foi construída com **Next.js 14 (App Router)** e **TypeScript**, focando em uma experiência de usuário moderna (SPA).

*   **Design System:** Tailwind CSS com suporte nativo a **Dark Mode**.
*   **Funcionalidades:**
    *   Feed Infinito.
    *   Layout responsivo estilo Twitter/X.
    *   Atualizações otimistas (Feedback imediato ao curtir/seguir).
    *   Integração com mapas e geolocalização.

---

## 📂 Estrutura do Projeto (Backend)

A organização dos pacotes reflete a natureza híbrida do projeto:

src/main/java/com/ifconnected
├── controller # API REST Endpoints
├── model
│ ├── JDBC # Entidades mapeadas via SQL puro (User, Campus)
│ ├── JPA # Entidades mapeadas via Hibernate (Event)
│ ├── NOSQL # Documentos MongoDB (Post, Notification)
│ └── DTO # Objetos de transferência de dados
├── repository
│ ├── jdbc # Queries manuais e PostGIS
│ ├── jpa # Interfaces JpaRepository
│ └── mongo # Interfaces MongoRepository
├── service # Regras de Negócio e Integração dos bancos
└── config # Configurações de Segurança, CORS e DataSeeding

code
Code
download
content_copy
expand_less
---

## 🚀 Como Rodar o Projeto

### Pré-requisitos
*   **Docker** e **Docker Compose** instalados e rodando.
*   **Java 17** (JDK).
*   **Node.js 18+** (Para o frontend).

### Passo 1: Subir a Infraestrutura
Na raiz do projeto (onde está o `docker-compose.yml`), execute:

```bash
docker-compose up -d

Isso iniciará os containers: Postgres (5432), Mongo (27017), Redis (6379) e MinIO (9000).

Passo 2: Executar o Backend

No terminal, dentro da pasta do projeto Java:

code
Bash
download
content_copy
expand_less
./mvnw spring-boot:run

O sistema irá inicializar, criar as tabelas automaticamente e popular os Campi do IFPB através do DataSeeder.

Passo 3: Executar o Frontend

Em outro terminal, entre na pasta do frontend:

code
Bash
download
content_copy
expand_less
cd ifconnected-front
npm install
npm run dev

Acesse a aplicação em: http://localhost:3000

🧪 Testando as Funcionalidades

Crie uma Conta: Na tela de registro, selecione seu Campus (Isso é vital para a geolocalização).

Feed Regional: Acesse a aba "Perto". O sistema usará o PostGIS para calcular quais usuários estão num raio de 50km do seu campus e mostrará as postagens deles.

Publicar: Crie um post com foto. A imagem vai para o MinIO, o texto para o Mongo e a notificação para seus seguidores.

Perfil: Edite seu perfil. A próxima vez que carregar, os dados virão do Redis (Cache).

🛠️ Diagrama de Fluxo de Dados
code
Mermaid
download
content_copy
expand_less
graph TD
    Client[Frontend Next.js] --> API[Spring Boot Controller]
    
    subgraph "Camada de Persistência"
    API -->|Auth/Geo| Postgres[(Postgres + PostGIS)]
    API -->|Feed/Logs| Mongo[(MongoDB)]
    API -->|Cache| Redis[(Redis)]
    API -->|Upload| MinIO[(MinIO Storage)]
    end
👨‍💻 Autor

Jorge Allan da Silva Santos
Estudante de Análise e Desenvolvimento de Sistemas - IFPB

Desenvolvido como projeto prático para demonstrar competências em Arquitetura de Software, Java Ecosystem e DevOps.

code
Code
download
content_copy
expand_less
