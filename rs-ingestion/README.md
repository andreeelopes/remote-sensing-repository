# Sistema de Ingestão - Akka

## Organização das Pastas

```
.
|   .gitignore
|   build.sbt    # Configuração de dependências
|   README.md
|
+---data	# Dados (imagens e metadados) armazenados nesta pasta
|
+---logs
|
+---project
|
+---scripts
|       wait-for-it.sh	# script usado para deploy através do Docker
|
+---src
|   \---main
|       +---resources
|       |       application.conf	# Toda a configuração das fontes, extrações, metamodelos e infraestrutura
|       |       logback.xml			# Configuração dos logs
|       |
|       \---scala
|           |   Main.scala
|           |
|           +---api		# Endpoint de interação entre a componente da interface e a de ingestão
|			|
|           +---mongo	# Adaptador para MongoDB
|           |
|           \---protocol	# Protocolo de ingestão distribuída (Akka)
|           |   +---master
|           |   |
|           |   +---scheduler
|           |   |
|           |   \---worker
|           |
|           +---sources		# Definição das fontes e extrações
|           |   |
|           |   \---handlers	# Handlers de parsing, transformações e de erros
|           |
|           \---utils

```


## Compilação

Para correr localmente:

```
sbt run
```

Para gerar a imagem Docker:

```
sbt docker:publishLocal
```

Para publicar a imagem Docker no Docker Hub (podendo-se aceder à mesma em qualquer máquina):

```
sbt docker:publish
```


