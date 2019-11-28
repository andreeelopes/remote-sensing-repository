
# Repositório de Dados e Metadados de Observação Terrestre

A deteção remota tem vindo a tornar-se o método mais importante de recolha de
informação sobre a superfície terrestre. Os dados gerados revelam caraterísticas de big data:
volume, velocidade, diversidade.

Neste contexto, os múltiplos fornecedores já têm os seus próprios repositórios, nos quais
os standards de representação de dados e metadados diferem. Portanto, é importante definir
uma camada de abstração intermédia. Outro desafio passa pela heterogeneidade dos dados e
metadados. Sendo que os metamodelos dos diferentes produtos devem ser extensíveis.
Finalmente, aliado ao repositório, existe a necessidade de incorporar cadeias de processamento
local.

A abordagem para endereçar os problemas referidos condensa-se em cinco pontos:
automatização da ingestão de dados e metadados; especificação hierárquica do metamodelo;
linguagem de especificação ETL; mecanismo de interrogação de alto nível; e framework de
processamento local.

Por fim, a arquitetura proposta foi avaliada recorrendo a um conjunto de casos de estudo
reais. Paralelamente, comparou-se com a plataforma Google Earth Engine, cujo foco é a
computação. Tendo-se concluído que a abordagem proposta não oferece tantas garantias de
escalabilidade, mas, na perspetiva da catalogação, a presença de uma gestão colaborativa dos
produtos e dos seus metamodelos é uma grande vantagem face às outras soluções.

## Arquitetura

[Diagrama](https://drive.google.com/uc?id=127DAMerFBggWD6QbpDP7CYeaFtip53ER)

O projeto git está divido em dois principais módulos: ingestão e api (API REST).

**Para mais informações relativas aos submódulos, consultar o README presente em cada um deles.**

## Instalação e Deployment

Toda a arquitetura está dockerizada.

Portanto para instalar toda esta aplicação basta ter o docker (e o docker-compose) instalado.

Na instalação importa ir para a raíz do projeto, na qual está presente o docker-compose.yml e executar o seguinte comando:

```
docker-compose --compatibility up
```

Sendo que a infraestrutura de containers criada será a correspondente à seguinte [imagem](https://drive.google.com/uc?id=1MN4Mu2cd4h4aMwi8y9ER5dVjMaVNS05F).


### Deploy distribuído

Alternativamente, é possível executar toda a infraestrutura de uma forma distribuída através do Docker Swarm:

Iniciar o cluster (sendo que depois será gerado o comando através do qual outras máquinas se podem juntar a este cluster):

```
docker swarm init
```

Deploy da aplicação:

```
docker stack deploy --compose-file docker-compose.yml cluster_rs
docker stack services cluster_rs
```

Consultar serviços:

```
docker service ps cluster_rs_worker
```

NOTA: o deploy distribuído em várias máquinas não foi testado devidamente 


