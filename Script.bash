#Para executar ==> CMD ../script.bash
# Cria diretório pasta
mkdir pasta

# Cria diretório pasta, mas retorna erro, pois já existe
mkdir pasta

# Cria diretório outraPasta dentro do diretório pasta
mkdir pasta/outraPasta

# Navega para a pasta SERVER:/pasta
cd pasta

# Tenta navegar para a pasta SERVER:/pasta/algoInexistente, mas informa erro, pois não existe
cd algoInexistente

# Retorna que comando é inexistente
print

# sai da pasta atual
cd ..

# Apresenta erro, pois não é possível acessar pasta fora do servidor.
cd ..

# Faz upload para o servidor do arquivo na pasta src/gui/TCPClient.java
upload src/gui/TCPClient.java

# Tenta fazer upload para o servidor, mas retorna erro pois arquivo não existe
upload src/gui/TCPServer.java

#Lista conteúdo do servidor, na pasta atual
ls

# Tenta deletar arquivo, mas retorna erro, pois arquivo não existe
del TCPServer.java

#Realiza download do arquivo na pasta padrão GUI Client/src/gui/Downloads
download TCPClient.java

#Tenta realizar o download, porem não existe “src/gui/TCPClient.java” no servidor
download src/gui/TCPClient.java

#Deleta pasta, mas apresenta erro pois não está vazia
del pasta

#Mostra os comandos
help

#Não é permitido dar exit dentro do arquivo.bash
#Não é permitido dar clear dentro do arquivo.bash
