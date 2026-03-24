# API de E‑commerce – Projeto de Capacitação Java / Spring Boot

Este projeto é uma API REST de e‑commerce desenvolvida em Java com Spring Boot, criada como base para estudos de:

- Java e Spring Boot
- Modelagem de domínio (entidades e regras de negócio)
- Boas práticas de API REST
- Persistência com Spring Data JPA
- Testes unitários
- Integração com notificações (webhook) ao mudar status do pedido

É um projeto **didático**, pensado para ser ampliado com novas regras de negócio, entidades, validações e funcionalidades.

## Mini Glossário

- [Tecnologias e Stack](#1-tecnologias-e-stack)
- [Funcionalidades Implementadas](#2-funcionalidades-implementadas)
  - [Categorias e Organização do Catálogo](#21-categorias-e-organização-do-catálogo)
  - [Controle de Estoque (Inventário)](#22-controle-de-estoque-inventário)
  - [Carrinho de Compras (Cart)](#23-carrinho-de-compras-cart)
  - [Pedidos (Orders)](#24-pedidos-orders)
  - [Notificações de Mudança de Status de Pedido (Webhook)](#25-notificações-de-mudança-de-status-de-pedido-webhook)
- [Funcionalidades Futuras / Em Evolução](#3-funcionalidades-futuras--em-evolução)
  - [Promoções e Cupons](#31-promoções-e-cupons)
  - [Reviews e Avaliações](#32-reviews-e-avaliações)
  - [Auditoria (Audit-log)](#33-auditoria-audit-log)
  - [Relatórios e Métricas](#34-relatórios-e-métricas)
- [Modelagem de Domínio (Entidades Sugeridas)](#4-modelagem-de-domínio-entidades-sugeridas)
- [Testes Unitários](#5-testes-unitários)
- [Sugestões de Evolução — Regras de Negócio e Melhorias](#6-sugestões-de-evolução--regras-de-negócio-e-melhorias-para-os-alunos)
- [Tarefas / Exercícios Práticos](#7-tarefas--exercícios-práticos)

---

## 1. Tecnologias e Stack

- **Linguagem:** Java 21
- **Framework:** Spring Boot
- **Persistência:** Spring Data JPA
- **Banco:** (configurável; em testes, H2 em memória)
- **Build:** Maven
- **Testes:** JUnit 5, Mockito
- **Logging:** SLF4J + Log4j2 (configuração específica para testes)
- **Documentação de API:** Springdoc OpenAPI (Swagger UI)

---

## 2. Funcionalidades Implementadas

### 2.1. Categorias e Organização do Catálogo

- Todo produto pertence a uma categoria.
- Categorias podem ter hierarquia (pai → filho).
- Nome de categoria é único no mesmo nível.

**Validações:**

- Nome obrigatório.
- Proibição de duplicidade de nome na mesma hierarquia.

**Endpoints sugeridos:**

- `GET    /categories`
- `POST   /categories`
- `PUT    /categories/{id}`
- `DELETE /categories/{id}`

> Observação: a implementação completa pode variar, mas a modelagem e os endpoints seguem esse padrão.

---

### 2.2. Controle de Estoque (Inventário)

- Cada ajuste de estoque gera um registro de `InventoryTransaction`.
- A criação de pedidos reduz o estoque dos produtos.
- Cancelamento de pedido devolve o estoque dos itens.
- Impede vendas com estoque insuficiente.
- Pode sinalizar estoque mínimo (via flag/regra de negócio).

**Tipos de transação:**

- Entrada (compra/fornecedor)
- Saída (venda)
- Ajuste
- Devolução

**Endpoints sugeridos:**

- `POST /inventory/{productId}/add`
- `POST /inventory/{productId}/remove`
- `GET  /inventory/{productId}`

---

### 2.3. Carrinho de Compras (Cart)

- Usuário autenticado possui **apenas 1 carrinho ativo**.
- Itens do carrinho possuem `priceSnapshot` (preço no momento da adição).
- Toda alteração no carrinho recalcula totais (`totalItens`, `totalValor`).

**Endpoints sugeridos:**

- `GET  /cart` — obtém o carrinho ativo do usuário
- `POST /cart/items` — adiciona item
- `PUT  /cart/items/{itemId}` — atualiza quantidade
- `DELETE /cart/items/{itemId}` — remove item

---

### 2.4. Pedidos (Orders)

Fluxo principal: **Carrinho → Pedido (checkout)**.

#### Status do pedido

- `CREATED`
- `PAID`
- `SHIPPED`
- `DELIVERED`
- `CANCELLED`

**Regras principais:**

- Checkout:
  - Cria um `Order` a partir do carrinho ativo.
  - Copia os itens do carrinho para `OrderItem`.
  - Abate o estoque de cada item via serviço de inventário.
  - Desativa o carrinho após o checkout.
- Cancelamento:
  - Permitido apenas se o pedido estiver em `CREATED` ou `PAID`.
  - Muda o status para `CANCELLED`.
  - Devolve o estoque dos itens.
- Consulta:
  - `getOrder` consulta pedido pelo `id` e `userId` (evitando acesso de outros usuários).

**Endpoints sugeridos:**

- `POST /orders` — cria pedido a partir do carrinho (checkout)
- `GET  /orders/{id}` — retorna o pedido do usuário
- `POST /orders/{id}/cancel` — cancela o pedido (quando permitido)

---

### 2.5. Notificações de Mudança de Status de Pedido (Webhook)

Foi implementada a feature de **Notificações via Webhook** sempre que o status de um pedido muda nos pontos principais:

- Na criação do pedido (`CREATED`).
- No cancelamento do pedido (`CANCELLED`).

#### 2.5.1. Modelo de evento

DTO `OrderStatusChangeEventDTO`, contendo:

- Identificação:
  - `orderId`
  - `userId`
- Status:
  - `oldStatus`
  - `newStatus`
- Totais:
  - `totalItens`
  - `totalValor`
  - `discount`
  - `freight`
  - `total`
- Dados adicionais:
  - `address`
- Datas:
  - `createdAt`
  - `paidAt`
  - `shippedAt`
  - `deliveredAt`
  - `cancelledAt`

#### 2.5.2. Serviço de notificação

Interface:

```java
public interface OrderNotificationService {
    void notifyStatusChange(OrderStatusChangeEventDTO event);
}
```

Implementação atual: `WebhookOrderNotificationService`:

- Envia um **POST HTTP** com JSON do evento para uma URL configurável.
- Usa `RestTemplate` + SLF4J.
- Tratamento de erro: **best-effort** (registra log, mas não impede a mudança de status).

**Configuração da URL do webhook:**

```properties
notification.order.webhook.url=http://localhost:8081/order-status-webhook
```

- Se não configurada ou vazia, nenhuma notificação é enviada.

#### 2.5.3. Integração com `OrderService`

- Após salvar um pedido criado:
  - Dispara evento de status com `oldStatus = null`, `newStatus = CREATED`.
- Após salvar um pedido cancelado:
  - Dispara evento de status com `oldStatus = CREATED|PAID`, `newStatus = CANCELLED`.
- Há uma verificação para **não notificar** quando o status não muda de fato.

---

## 3. Funcionalidades Futuras / Em Evolução

### 3.1. Promoções e Cupons

- Tipos:
  - Desconto percentual (%)
  - Desconto fixo (R$)
  - Promoção por categoria ou produto
  - Cupom com período de validade
  - Cupom com limite de uso

**Regras de validação:**

- Cupom expirado → rejeitar.
- Cupom já utilizado pelo usuário → rejeitar.
- Cupom sem relação com os produtos do carrinho → rejeitar.

**Endpoints sugeridos:**

- `POST /promotions`
- `POST /coupons/apply`

---

### 3.2. Reviews e Avaliações

- Apenas quem comprou pode avaliar.
- Limite de 1 avaliação por produto por pedido.
- Recalcular média de avaliações a cada novo review.

**Endpoints sugeridos:**

- `POST /reviews`
- `GET  /reviews/product/{productId}`

---

### 3.3. Auditoria (Audit Log)

- Registrar:
  - Quem criou/alterou/deletou.
  - Data e hora.
  - Antes e depois da alteração (JSON).
- Logs de auditoria devem ser imutáveis.

**Endpoint sugerido:**

- `GET /audit?entity=Product`

---

### 3.4. Relatórios e Métricas

Exemplos de relatórios:

- Produtos mais vendidos.
- Faturamento por período.
- Produtos com estoque baixo.
- Promoções mais utilizadas.

**Endpoints sugeridos:**

- `GET /reports/sales`
- `GET /reports/top-products`
- `GET /reports/low-stock`

---

## 4. Modelagem de Domínio (Entidades Sugeridas)

Abaixo uma visão geral das entidades principais usadas/planejadas:

- **Product**
  - `id`, `name`, `description`, `sku`, `price`, `costPrice`, `categoryId`, `stockQuantity`, `active`, `createdAt`, `updatedAt`

- **Category**
  - `id`, `name`, `parentId`, `createdAt`, `updatedAt`

- **InventoryTransaction**
  - `id`, `productId`, `delta`, `reason`, `referenceId`, `createdBy`, `createdAt`

- **Cart**
  - `id`, `userId`, `status`, `ativo`, `totalItens`, `totalValor`, etc.

- **CartItem**
  - `id`, `cartId`, `productId`, `quantity`, `priceSnapshot`, `totalLinha`

- **Order**
  - `id`, `userId`, `total`, `discount`, `freight`, `status`, `createdAt`, `paidAt`, `shippedAt`, `deliveredAt`, `cancelledAt`, `address`, `cartId`, etc.

- **OrderItem**
  - `id`, `orderId`, `productId`, `quantity`, `priceSnapshot`, `totalLinha`

- **Promotion**
  - `id`, `code`, `type`, `value`, `validFrom`, `validTo`, `usageLimit`, `usedCount`, `applicableTo`

- **Review**
  - `id`, `productId`, `userId`, `rating`, `comment`, `createdAt`

- **AuditLog**
  - `id`, `entityType`, `entityId`, `action`, `beforeJson`, `afterJson`, `who`, `when`

---

## 5. Testes Unitários

### 5.1. Estratégia

- Uso de **JUnit 5** e **Mockito**.
- Os testes validam principalmente:
  - Regras de negócio nas services (`CartService`, `InventarioService`, `OrderService`, etc.).
  - Interações com repositórios e serviços auxiliares.

### 5.2. Foco em `OrderServiceTest`

O arquivo `OrderServiceTest` cobre cenários importantes para pedidos:

- **Criação de pedido a partir do carrinho:**
  - Verifica:
    - `status = CREATED`
    - Totais corretos (`totalItens`, `totalValor`)
    - Itens copiados do carrinho
    - Baixa de estoque (`inventarioService.removerEstoque`)
    - Carrinho desativado (`cart.setAtivo(false)`)
    - **Chamada do serviço de notificação** (`orderNotificationService.notifyStatusChange`)

- **Criação de pedido com carrinho inexistente:**
  - Lança `EntityNotFoundException`.
  - Garante que **não há notificação enviada** (`verifyNoInteractions(orderNotificationService)`).

- **Criação de pedido com carrinho vazio:**
  - Lança `IllegalStateException`.
  - Garante que **não há notificação enviada**.

- **Cancelamento de pedido em status permitido (`CREATED`/`PAID`):**
  - Muda status para `CANCELLED`.
  - Devolve estoque (`inventarioService.adicionarEstoque`).
  - Dispara **notificação de mudança de status**.

- **Cancelamento de pedido em status não permitido (`SHIPPED`):**
  - Lança `IllegalStateException`.
  - Garante que nenhuma notificação é disparada.

- **Consulta de pedido (`getOrder`)**
  - Retorna corretamente o pedido associado ao `userId`.

### 5.3. Logging em Testes (Log4j2)

- Arquivo `log4j2-test.xml` em `src/test/resources`.
- Define:
  - Appender de console com pattern amigável.
  - `Root` logger em nível `INFO`.
  - Logger específico para `WebhookOrderNotificationService` em `DEBUG`, facilitando inspeção de logs de notificação durante os testes.

### 5.4. Rodando os testes

No diretório raiz do projeto:

```bash
./mvnw test
```

No Windows PowerShell:

```powershell
.\mvnw test
```

> Caso haja problemas com encoding em `application.properties`, ajuste para UTF-8 ou refine a configuração do plugin de resources no `pom.xml`.

---

## 6. Sugestões de Evolução — Regras de Negócio e Melhorias (para os alunos)

Este projeto foi criado como introdução ao Java e pode ser ampliado com novas regras de negócio, entidades, validações e funcionalidades. As sugestões abaixo servem como exercícios guiados para aprimorar:

- API REST
- Java
- Spring Boot
- Autenticação/autorização
- Modelagem de dados
- Boas práticas de testes

As funcionalidades estão organizadas por prioridade e dificuldade.

### 6.1. Funcionalidades Prioritárias

1. **Categorias e Organização do Catálogo**  
   _Prioridade: Alta • Dificuldade: Baixa_

   - Criar entidade `Category`.
   - Associar `Product` → `Category`.
   - Implementar busca de produtos por nome/categoria.
   - Validar dados básicos (nome obrigatório, proibir duplicidade).

2. **Controle de Estoque (Inventário)**  
   _Prioridade: Alta • Dificuldade: Média_

   - Criar `InventoryTransaction`.
   - Garantir consistência em entradas/saídas/ajustes/devoluções.
   - Impedir vendas com estoque insuficiente.
   - Criar alertas de estoque mínimo.

3. **Carrinho de Compras**  
   _Prioridade: Alta • Dificuldade: Média_

   - Implementar carrinho com 1 ativo por usuário.
   - Persistir `priceSnapshot` nos itens.
   - Recalcular totais a cada atualização.

4. **Pedidos (Orders)**  
   _Prioridade: Alta • Dificuldade: Média_

   - Completar o fluxo de status (`PAID`, `SHIPPED`, `DELIVERED`).
   - Integrar notificações em todas as mudanças de status.
   - Adicionar endpoints para pagar/enviar/entregar pedido.

5. **Promoções e Cupons**  
   _Prioridade: Média • Dificuldade: Média_

   - Implementar diferentes tipos de desconto.
   - Validar datas, limites de uso e elegibilidade.

6. **Reviews e Avaliações**  
   _Prioridade: Baixa • Dificuldade: Baixa_

   - Restringir avaliações a quem comprou.
   - Limitar a 1 review por produto/pedido.
   - Calcular média de notas.

7. **Auditoria (Audit Log)**  
   _Prioridade: Média • Dificuldade: Baixa_

   - Registrar ações de CRUD com payload antes/depois.
   - Garantir imutabilidade da auditoria.

8. **Relatórios e Métricas**  
   _Prioridade: Baixa • Dificuldade: Média_

   - Implementar relatórios agregados (vendas, top produtos, etc.).
