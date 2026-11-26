# SingleBroker

Projeto Java desktop do SingleBroker, organizado para separar as telas das regras de negócio e preparar o reaproveitamento dos serviços na versão web.

## Requisitos

- JDK 24 ou superior
- Apache Maven (já incluído no Apache NetBeans)
- MySQL para usar a aplicação
- NetBeans com suporte a projetos Maven

## Configuração do banco

1. Crie no MySQL o banco `singlebrokerdb` ou informe outro schema na configuração.
2. Na raiz do projeto, abra `singlebroker.local.properties` e ajuste os dados para o seu MySQL. Se o arquivo não existir, crie-o com estas propriedades e edite os valores necessários:

```properties
db.url=jdbc:mysql://localhost:3306/singlebrokerdb?useSSL=false&serverTimezone=America/Sao_Paulo
db.user=root
db.password=
db.ddl=update
```

3. Abra o projeto no NetBeans e execute a classe `main.Main`.

O arquivo `singlebroker.local.properties` é local e não deve ser enviado ao repositório. O projeto também aceita as variáveis de ambiente `SINGLEBROKER_DB_URL`, `SINGLEBROKER_DB_USER`, `SINGLEBROKER_DB_PASSWORD` e `SINGLEBROKER_DB_DDL`.

No primeiro acesso a um banco sem usuários, o sistema solicita a criação do usuário administrador.

## Testes

Os testes JUnit ficam em `src/test/java` no próprio projeto Maven. Para executá-los no NetBeans, abra o projeto, clique com o botão direito sobre ele e escolha **Test**. Também é possível usar `mvn test` no terminal, na pasta do projeto.

Os testes de regras e serviços sempre rodam sem conexão com o banco. `ConexaoMySqlTest` verifica automaticamente a conexão com uma consulta somente de leitura, usando a mesma configuração do sistema. Se o MySQL não estiver acessível, essa verificação é marcada como ignorada e os demais testes continuam normalmente. Não é necessário habilitar propriedades especiais nem criar um banco de teste para executar a suíte automatizada.

Para testar a aplicação manualmente, use um banco separado dos dados reais.

Para executar as verificações rápidas de regras sem abrir a interface nem acessar o banco, execute `main.Main` com o argumento `--verificar-regras`.
