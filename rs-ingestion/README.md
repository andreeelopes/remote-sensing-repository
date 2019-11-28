# Sistema de Ingestão - Akka

## Organização das Pastas

```
.
+-- _config.yml
+-- _drafts
|   +-- begin-with-the-crazy-ideas.textile
|   +-- on-simplicity-in-technology.markdown
+-- _includes
|   +-- footer.html
|   +-- header.html
+-- _layouts
|   +-- default.html
|   +-- post.html
+-- _posts
|   +-- 2007-10-29-why-every-programmer-should-play-nethack.textile
|   +-- 2009-04-26-barcamp-boston-4-roundup.textile
+-- _data
|   +-- members.yml
+-- _site
+-- index.html
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

## TO DO


