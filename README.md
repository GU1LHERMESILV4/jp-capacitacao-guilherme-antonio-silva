# API de E‑commerce – Projeto de Capacitação JP Minsait

API REST de e‑commerce desenvolvida em **Java 21** com **Spring Boot**, utilizando **Oracle Database** como banco de dados relacional.
O projeto simula o fluxo completo de uma loja online (catálogo, estoque, carrinho, pedidos, promoções, reviews, auditoria e relatórios) com foco em **regras de negócio**, **boas práticas de API REST** e **testes automatizados**.

Este repositório foi baseado e evoluído a partir do projeto original:

Projeto base: <https://github.com/Felipe-Abreu/jp-capacitacao-2026>

---

## Objetivo do Projeto

- Consolidar conhecimentos de **backend Java** em um contexto próximo ao corporativo.
- Praticar **modelagem de domínio** (entidades, relacionamentos, regras).
- Implementar **APIs REST** limpas, bem estruturadas e documentadas.
- Exercitar **testes unitários** e boas práticas de manutenção de código.
- Servir como **projeto de portfólio** para oportunidades como desenvolvedor back-end.

---

## Tecnologias e Stack

- **Linguagem:** Java 21
- **Framework:** Spring Boot 4.0.4
- **Persistência:** Spring Data JPA (Hibernate)
- **Banco de Dados:** Oracle Database
- **Build:** Maven
- **Documentação de API:** Springdoc OpenAPI (Swagger UI)
- **Testes:** JUnit 5, Mockito
- **Logging:** SLF4J + Log4j2 (com `log4j2-test.xml` para testes)

---

## Arquitetura

O projeto segue uma arquitetura em camadas, organizada por pacote:

- **Controller** (`controller/`):  
  Camada de entrada HTTP (REST). Expõe endpoints para catálogo, carrinho, pedidos, inventário, promoções, reviews, auditoria, relatórios etc.

- **Service** (`service/`):  
  Camada de **regras de negócio**, por exemplo:
  - Controle de estoque / inventário
  - Fluxo de checkout (carrinho → pedido)
  - Cancelamento de pedidos
  - Disparo de notificações de mudança de status do pedido

- **Repository** (`repository/`):  
  Acesso a dados com Spring Data JPA, persistindo no Oracle.

- **Model / Domain** (`model/`):  
  Entidades do domínio de e‑commerce, como `Product`, `Category`, `Cart`, `CartItem`, `Order`, `OrderItem`, `InventoryTransaction`, `Promotion`, `Review`, `AuditLog` etc.

- **DTOs** (`dto/`):  
  Objetos de transferência usados pelos controllers e serviços, incluindo o evento de notificação de pedido (`OrderStatusChangeEventDTO`).

---

## Principais Funcionalidades

Resumo das features de negócio já modeladas/implementadas (detalhes em `docs/features.md`):

### 1. Categorias e Organização do Catálogo

- Todo produto pertence a uma **categoria**.
- Suporte a **hierarquia de categorias** (pai → filho).
- Nome de categoria **único por nível**.

Exemplos de endpoints:

- `GET    /categories`
- `POST   /categories`
- `PUT    /categories/{id}`
- `DELETE /categories/{id}`

---

### 2. Controle de Estoque (Inventário)

- Cada ajuste de estoque gera um registro de `InventoryTransaction`.
- A criação de um **pedido** reduz o estoque.
- O **cancelamento** de pedido devolve o estoque.
- Bloqueio de vendas com **estoque insuficiente**.

Exemplos de endpoints:

- `POST /inventory/{productId}/add`
- `POST /inventory/{productId}/remove`
- `GET  /inventory/{productId}`

---

### 3. Carrinho de Compras (Cart)

- Usuário autenticado tem **apenas 1 carrinho ativo**.
- Cada item guarda um `priceSnapshot` (preço no momento da adição).
- Atualizações recalculam automaticamente os totais do carrinho.

Endpoints típicos:

- `GET  /cart`
- `POST /cart/items`
- `PUT  /cart/items/{itemId}`
- `DELETE /cart/items/{itemId}`

---

### 4. Pedidos (Orders)

Fluxo principal: **Carrinho → Pedido (checkout)**.

Status principais do pedido:

- `CREATED`
- `PAID`
- `SHIPPED`
- `DELIVERED`
- `CANCELLED`

Regras chave:

- **Checkout**:
  - Cria `Order` a partir do carrinho ativo.
  - Copia itens de `Cart` para `OrderItem`.
  - Abate estoque via serviço de inventário.
  - Desativa o carrinho após checkout.
- **Cancelamento**:
  - Permitido apenas em `CREATED` ou `PAID`.
  - Devolve estoque ao inventário.

Endpoints típicos:

- `POST /orders`
- `GET  /orders/{id}`
- `POST /orders/{id}/cancel`

---

### 5. Promoções, Cupons, Reviews, Auditoria e Relatórios

Além das funcionalidades principais acima, o domínio contempla (detalhado em `docs/features.md`):

- **Promoções e Cupons**  
  Descontos percentuais/fixos, validade, limite de uso, associação por produto/categoria.

- **Reviews e Avaliações**  
  Apenas quem comprou avalia, 1 review por produto por pedido, média de avaliação por produto.

- **Auditoria (Audit Log)**  
  Registro de quem alterou o quê e quando, com antes/depois (JSON).

- **Relatórios e Métricas**  
  Produtos mais vendidos, faturamento por período, estoque baixo, promoções mais usadas.

---

## Banco de Dados (Oracle)

O projeto utiliza **Oracle Database** como banco principal, alinhado com cenários corporativos.

- Mapeamento objeto‑relacional com **JPA/Hibernate**.
- Entidades de domínio de e‑commerce (Product, Category, Cart, Order, InventoryTransaction, Promotion, Review, AuditLog…).
- Preparado para evolução via scripts SQL versionados (ex.: Flyway/Liquibase em futuras extensões).

A modelagem detalhada do banco (tabelas, colunas, relacionamentos) foi documentada em: `docs/schema-oracle.sql`.

---

## Configuração e Execução

### 1. Pré‑requisitos

- Java 21 instalado e configurado (`JAVA_HOME`).
- Maven (ou uso do wrapper `mvnw` / `mvnw.cmd`).
- Instância de **Oracle Database** acessível (local ou remota).
- Usuário/schema no Oracle com permissões de criação de tabelas.

### 2. Configurar `application.properties`

Arquivo: `src/main/resources/application.properties`

Exemplo de configuração (ajuste para o seu ambiente Oracle):

```properties
spring.application.name=jp-capacitacao-guilherme-antonio-silva

# Oracle
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=SEU_USUARIO
spring.datasource.password=SUA_SENHA
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

server.port=8080
```

### 3. Build e execução

Na raiz do projeto:

```bash
mvn clean install
mvn spring-boot:run
```

No Windows PowerShell:

```powershell
mvn clean install
mvn spring-boot:run
```

A aplicação ficará disponível em:

```text
http://localhost:8080
```

---

## Documentação da API (Swagger / OpenAPI)

A documentação interativa da API é gerada com **Springdoc OpenAPI**.

Após subir a aplicação, acesse:

```text
http://localhost:8080/swagger-ui.html
```

ou

```text
http://localhost:8080/swagger-ui/index.html
```

Você poderá:

- Visualizar todos os endpoints disponíveis.
- Conferir modelos de requisição/resposta.
- Executar chamadas diretamente pelo navegador.

---

## Notificações de Mudança de Status de Pedido (Webhook)

Sempre que o status de um pedido muda em pontos chave (ex.: criação e cancelamento), o sistema pode disparar uma notificação via **webhook**.

### Configuração

```properties
# URL do webhook de notificação de pedido (deixe em branco para desabilitar)
notification.order.webhook.url=http://localhost:8081/order-status-webhook
```

- Se a propriedade estiver vazia/ausente, a notificação é simplesmente ignorada (best‑effort).
- Implementação padrão utiliza `RestTemplate` + SLF4J (Log4j2) para logs.

### Evento de Notificação

O payload enviado usa um DTO similar a `OrderStatusChangeEventDTO`, contendo:

- Identificação: `orderId`, `userId`
- Status: `oldStatus`, `newStatus`
- Totais: `totalItens`, `totalValor`, `discount`, `freight`, `total`
- Dados adicionais: `address`
- Datas: `createdAt`, `paidAt`, `shippedAt`, `deliveredAt`, `cancelledAt`

---

## Testes Automatizados

O projeto utiliza **JUnit 5** e **Mockito** para testes unitários, com foco nas regras de negócio das services.

- Testes em `src/test/java/...` (ex.: `CartServiceTest`, `InventarioServiceTest`, `OrderServiceTest`).
- Configuração de logs de teste em `src/test/resources/log4j2-test.xml`.

Para executar os testes:

```bash
mvn test
```

No Windows PowerShell:

```powershell
mvn test
```

Os testes cobrem cenários como:

- Criação de pedido a partir do carrinho (status, totais, itens, baixa de estoque, desativação do carrinho).
- Cancelamento de pedido em status permitido (devolução de estoque, mudança de status).
- Erros de negócio (carrinho inexistente/vazio, cancelamento em status inválido) sem disparar notificações indevidas.
- Interações corretas com serviços auxiliares e repositórios.

---

## Melhorias Futuras

Algumas funcionalidades e melhorias planejadas/sugeridas:

-  **Scheduler** para alertas de estoque baixo.
-  **Multi‑seller** (múltiplos vendedores gerenciando seus produtos).
-  Autenticação/autorização (ex.: Spring Security + JWT).
-  Métricas e monitoramento (Spring Boot Actuator, Prometheus, etc.).
-  Integração com meios de pagamento externos.
-  Scripts de migração de banco (Flyway/Liquibase) versionados.

