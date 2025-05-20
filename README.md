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
Posteriomente à primeira operação poderá ser executado o comando `--help` para mostrar os comandos implementados e `exit` para terminar a sessão.




