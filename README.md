# SingleBroker

Projeto Java desktop do SingleBroker, organizado para separar as telas das regras de negócio e preparar o reaproveitamento dos serviços na versão web.

## Requisitos

- JDK 24
- Apache Maven
- MySQL
- NetBeans com suporte a projetos Maven

## Configuração do banco

1. Crie no MySQL o banco `singlebrokerdb` ou informe outro schema na configuração.
2. Na raiz do projeto, abra `singlebroker.local.properties` e ajuste os dados para o seu MySQL. Se o arquivo não existir, crie-o com este conteúdo e edite os valores necessários:

```properties
db.url=jdbc:mysql://localhost:3306/singlebrokerdb?useSSL=false&serverTimezone=America/Sao_Paulo
db.user=root
db.password=
db.ddl=update
```

3. Abra o projeto no NetBeans e execute a classe `main.Main`.

O arquivo `singlebroker.local.properties` é local e não deve ser enviado ao repositório. O projeto também aceita as variáveis de ambiente `SINGLEBROKER_DB_URL`, `SINGLEBROKER_DB_USER`, `SINGLEBROKER_DB_PASSWORD` e `SINGLEBROKER_DB_DDL`.

No primeiro acesso a um banco sem usuários, o sistema solicita a criação do usuário administrador.

## Verificações

Para executar os testes automatizados pelo Maven:

```text
mvn test
```

Para executar as verificações rápidas de regras sem abrir a interface ou conectar ao banco, execute `main.Main` com o argumento `--verificar-regras`.
