uWant @ Play! Framework
=====================================
Copyright @ 2014-2015.

![alt tag](https://raw.githubusercontent.com/uWant-Brazil/uWant-Play/master/public/images/uwant_logo.jpg?token=1410319__eyJzY29wZSI6IlJhd0Jsb2I6dVdhbnQtQnJhemlsL3VXYW50LVBsYXkvbWFzdGVyL3B1YmxpYy9pbWFnZXMvdXdhbnRfbG9nby5qcGciLCJleHBpcmVzIjoxNDAyMjc0OTQ4fQ%3D%3D--de112f8e26184686279c5555f701e7a5b2cab6f0)

## Instalação ##
### Windows ###
1. Baixe o [Play Framework 2.2.3](http://downloads.typesafe.com/play/2.2.3/play-2.2.3.zip) e extraia em alguma pasta do computador **que não contenha espaços**.
2. Coloque a pasta raíz do Play nas variáveis do ambiente, assim como a pasta *caminho-jdk/bin* do seu JDK.
3. Certifique-se que os comandos *play help* e *javac* estão sendo reconhecidos na linha de comando.

### Mac OSX ###
1. Baixe o [Play Framework 2.2.3](http://downloads.typesafe.com/play/2.2.3/play-2.2.3.zip) e extraia em alguma pasta do computador **que não contenha espaços**.
2. Adicione o PATH no arquivo *.bash_profile* para o bin do play - *export PATH=%PATH;caminho_ate_play;*
3. Certifique-se que os comandos *play help* e *javac* estão sendo reconhecidos na linha de comando.


Mais informações em [Documentation - Installing](http://www.playframework.com/documentation/2.2.x/Installing).


#### Importando projeto no Eclipse ####

1. Clone o projeto do GitHub e navegue até a pasta no cmd.
2. Digite *play* na linha de comando e depois *eclipsify*.
3. Abre o Eclipe e vá em *File > Import > Projects from Git > Existing local repository* e adicione o seu repositório local do Git (onde você deu clone).

#### Importando projeto no IntelliJ ####

1. Clone o projeto do GitHub e navegue até a pasta no cmd.
2. Digite *play* na linha de comando e depois *idea with-sources=yes*.
3. Abre o IntelliJ e vá em *File > Import* e adicione o seu repositório local do Git (onde você deu clone).


Play instalado, Eclipse configurado, agora é só rodar utilizando *play ~run* na linha de comando. Mais informações em [Documentation - IDE](http://www.playframework.com/documentation/2.2.x/IDE) e [Documentation](http://www.playframework.com/documentation/2.2.x/Home).  
