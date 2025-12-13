# Use a imagem oficial do OpenJDK como imagem base
FROM eclipse-temurin:17-jdk-jammy

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo JAR do módulo master para o diretório /app no contêiner
# O JAR é construído no diretório target após o comando 'mvn clean install'
COPY target/master-1.0.0.jar master.jar

# Expõe a porta que a aplicação utiliza (se houver alguma que precise ser acessada externamente para este módulo)
# O módulo master no projeto original não parece ter uma porta exposta explicitamente para API REST,
# mas Spring Boot normalmente usa 8080 por padrão, se não configurado.
# Como o gateway redireciona para portas específicas, o master pode não precisar expor uma porta se
# sua função for puramente interna ou acionada por outros meios.
# Se o master for interagir com os nós diretamente, ou tiver uma API, ele precisaria expor uma porta.
# Para manter a compatibilidade com a estrutura do projeto, vamos assumir 8080 como padrão caso não haja conflito.
EXPOSE 8080

# Define o comando para executar a aplicação JAR
ENTRYPOINT ["java", "-jar", "master.jar"]
