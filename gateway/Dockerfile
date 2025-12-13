# Use a imagem oficial do OpenJDK como imagem base
FROM eclipse-temurin:17-jdk-jammy

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo JAR do módulo gateway para o diretório /app no contêiner
# O JAR é construído no diretório target após o comando 'mvn clean install'
COPY target/gateway-1.0.0.jar gateway.jar

# Expõe as portas que a aplicação utiliza
# 8080 para o dashboard principal
# 8081-8086 para as portas dos continentes
EXPOSE 8080 8081 8082 8083 8084 8085 8086

# Define o comando para executar a aplicação JAR
ENTRYPOINT ["java", "-jar", "gateway.jar"]
