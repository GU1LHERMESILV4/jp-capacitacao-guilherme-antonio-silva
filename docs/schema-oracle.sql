create table SYSTEM.CUSTOMERS
(
    CUSTOMER_ID   NUMBER(10)   not null
        constraint CUSTOMERS_PK
            primary key,
    CUSTOMER_NAME VARCHAR2(50) not null,
    CITY          VARCHAR2(50)
)
    /

create table SYSTEM.CATEGORIAS
(
    ID               NUMBER(19) generated as identity
        primary key,
    NOME             VARCHAR2(150 char) not null,
    CATEGORIA_PAI_ID NUMBER(19)
        constraint FKG020APPL1V0LS162ABBHM557S
            references SYSTEM.CATEGORIAS,
    constraint UK_CATEGORIA_NOME_PAI
        unique (NOME, CATEGORIA_PAI_ID)
)
    /

create table SYSTEM.PRODUTOS
(
    ID                    NUMBER(19) generated as identity
        primary key,
    CODIGO_BARRAS         VARCHAR2(255 char),
    DESCRICAO             VARCHAR2(255 char),
    NOME                  VARCHAR2(255 char) not null,
    PRECO                 NUMBER(38, 2)      not null,
    CATEGORIA_ID          NUMBER(19)         not null
        constraint FK8RQW0LJWDAOM34JR2T46BJTRN
            references SYSTEM.CATEGORIAS,
    ESTOQUE_ABAIXO_MINIMO NUMBER(1)          not null
        check ((estoque_abaixo_minimo in (0, 1))),
    ESTOQUE_MINIMO        NUMBER(10)         not null,
    QUANTIDADE_ESTOQUE    NUMBER(10)         not null
)
    /

create table SYSTEM.HISTORICO_PRECO
(
    ID             VARCHAR2(36) not null
        primary key,
    DATA_ALTERACAO TIMESTAMP(9),
    PRECO_ANTIGO   NUMBER(38, 2),
    PRECO_NOVO     NUMBER(38, 2),
    PRODUTOS_ID    NUMBER(19)
        constraint FKGUOGJL8SV68MFPXY050SB8D5T
            references SYSTEM.PRODUTOS
)
    /

create table SYSTEM.INVENTORY_TRANSACTIONS
(
    ID                 NUMBER(19) generated as identity
        primary key,
    DATA_TRANSACAO     TIMESTAMP(9),
    DESCRICAO          VARCHAR2(255 char),
    ESTOQUE_RESULTANTE NUMBER(10)        not null,
    QUANTIDADE         NUMBER(10)        not null,
    TIPO               VARCHAR2(20 char) not null
        check ((tipo in ('ENTRADA', 'SAIDA', 'AJUSTE', 'DEVOLUCAO'))),
    PRODUTO_ID         NUMBER(19)        not null
        constraint FKLFPFA84NQ5LKD6VJW86E8K1YS
            references SYSTEM.PRODUTOS
)
    /

create table SYSTEM.CARTS
(
    ID          NUMBER(19) generated as identity
        primary key,
    ATIVO       NUMBER(1)          not null
        check ((ativo in (0, 1))),
    TOTAL_ITENS NUMBER(10)         not null,
    TOTAL_VALOR NUMBER(19, 2)      not null,
    USER_ID     VARCHAR2(100 char) not null
)
    /

create table SYSTEM.CART_ITEMS
(
    ID                      NUMBER(19) generated as identity
        primary key,
    PRECO_UNITARIO_SNAPSHOT NUMBER(19, 2) not null,
    QUANTIDADE              NUMBER(10)    not null,
    TOTAL_LINHA             NUMBER(19, 2) not null,
    CART_ID                 NUMBER(19)    not null
        constraint FKPCTTVUQ4MXPPO8SXGGJTN5I2C
            references SYSTEM.CARTS,
    PRODUTO_ID              NUMBER(19)    not null
        constraint FKQOT222OLBP7HKLFQKS4GVUTXO
            references SYSTEM.PRODUTOS
)
    /

create table SYSTEM.AUDIT_LOGS
(
    ID          NUMBER(19) generated as identity
        primary key,
    ACTION      VARCHAR2(50 char)           not null,
    AFTER_JSON  CLOB,
    BEFORE_JSON CLOB,
    ENTITY_ID   VARCHAR2(100 char)          not null,
    ENTITY_TYPE VARCHAR2(100 char)          not null,
    WHEN_AT     TIMESTAMP(9) WITH TIME ZONE not null,
    WHO         VARCHAR2(100 char)          not null
)
    /

create table SYSTEM.ORDERS
(
    ID           NUMBER(19) generated as identity
        primary key,
    ADDRESS      VARCHAR2(500 char),
    CANCELLED_AT TIMESTAMP(9) WITH TIME ZONE,
    CART_ID      NUMBER(19),
    CREATED_AT   TIMESTAMP(9) WITH TIME ZONE not null,
    DELIVERED_AT TIMESTAMP(9) WITH TIME ZONE,
    DISCOUNT     NUMBER(19, 2),
    FREIGHT      NUMBER(19, 2),
    PAID_AT      TIMESTAMP(9) WITH TIME ZONE,
    SHIPPED_AT   TIMESTAMP(9) WITH TIME ZONE,
    STATUS       VARCHAR2(20 char)           not null
        check ((status in ('CREATED', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED'))),
    TOTAL        NUMBER(19, 2),
    TOTAL_ITENS  NUMBER(10)                  not null,
    TOTAL_VALOR  NUMBER(19, 2)               not null,
    USER_ID      VARCHAR2(100 char)          not null
)
    /

create table SYSTEM.ORDER_ITEMS
(
    ID                      NUMBER(19) generated as identity
        primary key,
    PRECO_UNITARIO_SNAPSHOT NUMBER(19, 2) not null,
    QUANTIDADE              NUMBER(10)    not null,
    TOTAL_LINHA             NUMBER(19, 2) not null,
    ORDER_ID                NUMBER(19)    not null
        constraint FKBIOXGBV59VETRXE0EJFUBEP1W
            references SYSTEM.ORDERS,
    PRODUTO_ID              NUMBER(19)    not null
        constraint FK5OJBQRK2CIBKK5O1K54GV2WB2
            references SYSTEM.PRODUTOS
)
    /

create table SYSTEM.PROMOTIONS
(
    ID            NUMBER(19) generated as identity
        primary key,
    APPLICABLE_TO VARCHAR2(200 char),
    CODE          VARCHAR2(50 char) not null
        constraint UKJDHO73YMBYU46P2HH562DK4KK
            unique,
    TYPE          VARCHAR2(20 char) not null
        check ((type in ('PERCENTAGE', 'FIXED'))),
    USAGE_LIMIT   NUMBER(10),
    USED_COUNT    NUMBER(10),
    VALID_FROM    TIMESTAMP(9) WITH TIME ZONE,
    VALID_TO      TIMESTAMP(9) WITH TIME ZONE,
    VALUE         NUMBER(19, 2)     not null
)
    /

create table SYSTEM.COUPON_USAGE
(
    ID           NUMBER(19) generated as identity
        primary key,
    ORDER_ID     NUMBER(19),
    USED_AT      TIMESTAMP(9) WITH TIME ZONE not null,
    USER_ID      VARCHAR2(100 char)          not null,
    PROMOTION_ID NUMBER(19)                  not null
        constraint FK2GL9CX60OPC4MKTI2MOH9BB1R
            references SYSTEM.PROMOTIONS
)
    /

create table SYSTEM.TESTE_REVIEWS
(
    ID NUMBER(19)
)
    /

create table SYSTEM.TESTE_REVIEWS2
(
    ID         NUMBER(19) not null
        primary key,
    PRODUCT_ID NUMBER(19) not null
)
    /

create table SYSTEM.TESTE_REVIEWS3
(
    ID         NUMBER(19)    not null
        primary key,
    PRODUCT_ID NUMBER(19)    not null,
    USER_ID    VARCHAR2(100) not null
)
    /

create table SYSTEM.REVIEWS
(
    ID             NUMBER(19)    not null
        primary key,
    PRODUCT_ID     NUMBER(19)    not null,
    USER_ID        VARCHAR2(100) not null,
    RATING         NUMBER(10)    not null,
    CREATED_AT     TIMESTAMP(6)  not null,
    REVIEW_COMMENT VARCHAR2(2000)
)
    /

