# API REST - Spring

## Organização das Pastas


````
.
|   .gitignore
|   Dockerfile     # Especificação da imagem Docker
|   pom.xml     # Configuração de dependências
|   README.md
|
+---data        # Dados (imagens e metadados) armazenados nesta pasta
|
+---logs
|
+---products    # Esquema e dois produtos da deteção de parcelas agrícolas (para testar API)
|   |   schema.json
|   |
|   +---LEZIRIA_PARCELS
|   |
|   \---VILA_FRANCA_DE_XIRA_PARCELS
|
+---scripts
|       wait-for-it.sh      # script usado para deploy através do Docker
|
+---src
|   +---main
|   |   +---java
|   |   |   \---pt
|   |   |       \---unl
|   |   |           \---fct
|   |   |               |   RSApiApplication.java
|   |   |               |
|   |   |               +---api         # Documentação do Swagger
|   |   |               |
|   |   |               +---conf        # Configuração do Swagger
|   |   |               |
|   |   |               +---controllers
|   |   |               |
|   |   |               +---errors
|   |   |               |
|   |   |               +---model
|   |   |               |
|   |   |               +---services
|   |   |               |
|   |   |               \---utils
|   |   |
|   |   \---resources
|   |           application.properties      # Inclui configuração do MongoDB
|   |           logback-spring.xml          #  Configuração dos logs

````


## Compilação

Para correr localmente:

```
mvn spring-boot:run
```

Para gerar a imagem Docker:

```
mvn install
```

Para publicar a imagem Docker no Docker Hub (podendo-se aceder à mesma em qualquer máquina):

```
mvn publish
```

## Utilização

A API está documentada através do Swagger. Portanto existe um endpoint no qual é disponibilizada uma página na qual é possível testar a API:

```
http://<ip>:<port>/swagger-ui.html
```
### Exemplo

1. ``POST /metamodels`` 

"schema" -> ``products/schema.json``

2. ``POST /products`` 

"metadata" -> ``products/metamodel.json``

"file" -> ``LEZIRIA_PARCELS/LEZIRIA_PARCELS.zip``
