# CSD-Rodel
Project For CSD-2025 by **Pedro Galego** 72298 

# Como correr o Rodel
As réplicas que têm que ser corridas estão nos devidos directories: 
- rodel
- rodel1
- rodel2
- rodel3   

Cada réplica tem que ser corrida com o comando `./mvnw spring-boot:run`.
# Como correr o client 
Depois de as réplicas reconherem-se, teremos de correr o nosso client.
Para compilar o nosso client fazemos: 
- `javac -cp ".:json-20250107.jar" -d bin src/Client.java src/objects/*.java`
 
E para executar fazemos:

- `java -cp ".:bin:json-20250107.jar" Client email secretKey operation`
  
Tanto `email` como `secretKey` são arbitrários sendo o client que decide esses valores. 
No caso da `operation` essa já tem que ser uma das operações implementadas no projeto. 
Exemplo:     
- `java -cp ".:bin:json-20250107.jar" Client pedro@gmail.com sporting loadmoney`
   
Depois de feita a primeira operação uma conta é automaticamente criada com o id que será exibido no terminal.  
Posteriormente à primeira operação poderá ser executado o comando `--help` para mostrar os comandos implementados e `exit` para terminar a sessão.  
# Sobre o benchmark do Gatling
De momento, a benchmark apenas funciona com uma outra versão do rodel que tenho o qual não existem verificações/assinaturas nas mensagens trocadas pois isso permite ao gatling fazer as requests sem qualquer tipo de encriptção, que dificultaria a execução da benchmark.  
O teste tem o nome: `LedgerBenchmarks.java` em `CSD-Rodel/gatlingBenchmarks/src/test/java/example/LedgerBenchmarks.java`  
Para correr os teste teriamos que fazer:  
- `./mvnw gatling:test`

E selecionar o teste `LedgerBenchmarks`.






