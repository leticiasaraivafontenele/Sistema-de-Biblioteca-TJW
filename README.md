# Sistema de Biblioteca — Microsserviços com Spring Boot

Trabalho Final da disciplina de Microsserviços (IFCE — Ciência da Computação).
Sistema de biblioteca composto por **4 containers** orquestrados via Docker Compose,
seguindo a arquitetura de microsserviços: cada serviço com banco próprio, comunicação
via HTTP e um Nginx como API Gateway (ponto único de entrada).

## Arquitetura

```
                       [ Browser ]
                            |
                            v
                  [ nginx :80 ]  ← API Gateway (roteamento + CORS)
                  /         |          \
                 /          |           \
   /  ──────────►     /api/livros ──►     /api/autores ──►
   front-ms :8080     livro-ms :8081       autor-ms :8082
   (Thymeleaf)        (REST + JPA)         (REST + JPA)
        |                  |                    |
        |            postgres-livros      postgres-autores
        |             (db_livros)          (db_autores)
        |                  ▲                    ▲
        └──── consome via RestClient ──────────┘
              (autor-ms:8082 / livro-ms:8081)
```

| Serviço     | Tecnologia                | Responsabilidade                                                        |
|-------------|---------------------------|-------------------------------------------------------------------------|
| **nginx**   | Nginx 1.25                | API Gateway: roteamento, CORS centralizado, ponto único de entrada      |
| **front-ms**| Spring Boot + Thymeleaf   | Interface web; consome `livro-ms` e `autor-ms` via RestClient           |
| **livro-ms**| Spring Boot + REST + JPA  | CRUD de Livros; guarda `autorId` como `Long`; não conhece o `autor-ms`  |
| **autor-ms**| Spring Boot + REST + JPA  | CRUD de Autores; serviço independente, sem dependência dos demais       |

> **Regra de ouro dos microsserviços:** cada serviço tem seu próprio PostgreSQL.
> O `livro-ms` **não** faz JOIN com a tabela de autores — ele armazena apenas o
> `autorId` (referência lógica, sem chave estrangeira no banco). Quem resolve o
> nome do autor é o `front-ms`, consultando o `autor-ms` via HTTP antes de
> renderizar a página.

## Tecnologias

- **Java 21** · **Spring Boot 3.5** · **Maven**
- **PostgreSQL 16** (um banco por microsserviço)
- **Nginx 1.25** (gateway)
- **Bootstrap 5** (interface web, via CDN)
- **Docker** + **Docker Compose**

## Como executar

Pré-requisito: **Docker** e **Docker Compose** instalados. Não é necessário ter
Java nem Maven na máquina — o build acontece dentro do Docker (multi-stage).

Na raiz do projeto:

```bash
docker compose up --build
```

Na primeira execução, as imagens são construídas, os bancos são criados e
**populados automaticamente** com dados de exemplo (3 autores e 5 livros).

Acesse a aplicação em:

- **Interface web:** <http://localhost>
- **API de livros:** <http://localhost/api/livros>
- **API de autores:** <http://localhost/api/autores>

### Parar a aplicação

```bash
docker compose down            # derruba os containers (mantém os dados)
docker compose down -v         # derruba os containers E apaga os volumes (zera os bancos)
```

### Persistência dos dados

Os dados ficam em **volumes Docker** nomeados (`pgdata-autores` e `pgdata-livros`),
declarados no `docker-compose.yml`. Por isso:

- Os dados **sobrevivem** a `docker compose restart` e a `docker compose down` seguido de `up`.
- Para **apagar** todos os dados e voltar ao estado inicial, use `docker compose down -v`.

## Endpoints da API

### autor-ms — `/api/autores`

| Método | Rota                  | Descrição               | Respostas              |
|--------|-----------------------|-------------------------|------------------------|
| GET    | `/api/autores`        | Lista todos os autores  | 200                    |
| GET    | `/api/autores/{id}`   | Busca autor por ID      | 200 / 404              |
| POST   | `/api/autores`        | Cadastra autor          | 201 / 400              |
| PUT    | `/api/autores/{id}`   | Atualiza autor          | 200 / 400 / 404        |
| DELETE | `/api/autores/{id}`   | Exclui autor            | 204 / 404              |

### livro-ms — `/api/livros`

| Método | Rota                              | Descrição                         | Respostas        |
|--------|-----------------------------------|-----------------------------------|------------------|
| GET    | `/api/livros`                     | Lista todos os livros             | 200              |
| GET    | `/api/livros?disponivel=true`     | Filtra por disponibilidade        | 200              |
| GET    | `/api/livros/{id}`                | Busca livro por ID                | 200 / 404        |
| POST   | `/api/livros`                     | Cadastra livro                    | 201 / 400        |
| PUT    | `/api/livros/{id}`                | Atualiza livro                    | 200 / 400 / 404  |
| DELETE | `/api/livros/{id}`                | Exclui livro                      | 204 / 404        |

Exemplos:

```bash
# Listar autores
curl http://localhost/api/autores

# Cadastrar um autor
curl -X POST http://localhost/api/autores \
  -H "Content-Type: application/json" \
  -d '{"nome":"José Saramago","nacionalidade":"Portuguesa","anoNascimento":1922}'

# Buscar autor inexistente (404)
curl -i http://localhost/api/autores/999
# -> {"erro":"Autor não encontrado: 999"}

# Listar apenas livros indisponíveis
curl "http://localhost/api/livros?disponivel=false"
```

## Modelos de dados

**Autor** (`db_autores`): `id`, `nome` (≤100), `nacionalidade` (≤80), `anoNascimento` (positivo).

**Livro** (`db_livros`): `id`, `titulo` (≤150), `genero` (≤60), `anoPublicacao` (positivo),
`disponivel` (boolean, padrão `true`), `autorId` (referência lógica ao autor).

## Estrutura do projeto

```
.
├── docker-compose.yml          # orquestração dos 4 serviços + 2 bancos + volumes
├── nginx/
│   └── nginx.conf              # configuração do API Gateway
├── autor-ms/                   # microsserviço de autores (Spring Boot REST)
│   ├── Dockerfile
│   └── src/...
├── livro-ms/                   # microsserviço de livros (Spring Boot REST)
│   ├── Dockerfile
│   └── src/...
└── front-ms/                   # interface web (Spring Boot + Thymeleaf)
    ├── Dockerfile
    └── src/...
```

## Decisões de projeto

- **Resiliência:** se um microsserviço estiver indisponível, o `front-ms` exibe uma
  mensagem amigável em vez de uma página de exceção. Na listagem de livros, se o autor
  de um livro não for encontrado, exibe-se *"Autor removido"* sem quebrar a página.
- **Roteamento único:** o browser acessa tudo pela porta 80. O `front-ms` fala com os
  microsserviços pelos nomes de serviço do Docker (`autor-ms:8082`, `livro-ms:8081`),
  nunca por `localhost`.
- **Padrão PRG (Post/Redirect/Get):** após cadastrar/editar/excluir pela interface web,
  o usuário é redirecionado, evitando reenvio do formulário.

## Solução de problemas

- **A porta 80 já está em uso:** pare o serviço que a ocupa ou altere o mapeamento de
  portas do `nginx` no `docker-compose.yml` (ex.: `"8000:80"`) e acesse `http://localhost:8000`.
- **Primeira subida lenta:** o build Maven dentro do Docker baixa as dependências na
  primeira vez; execuções seguintes aproveitam o cache.
- **Quero recomeçar do zero:** `docker compose down -v` apaga os volumes e os dados de
  exemplo são recriados na próxima subida.
