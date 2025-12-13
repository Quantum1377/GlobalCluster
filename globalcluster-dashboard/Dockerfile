# Use a imagem oficial do OpenJDK como imagem base
FROM eclipse-temurin:17-jdk-jammy

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo JAR do módulo globalcluster-dashboard para o diretório /app no contêiner
# O JAR é construído no diretório target após o comando 'mvn clean install'
COPY target/globalcluster-dashboard-1.0-SNAPSHOT.jar dashboard.jar

# Expõe a porta que a aplicação utiliza (porta do dashboard)
EXPOSE 8080

# Define o comando para executar a aplicação JAR
ENTRYPOINT ["java", "-jar", "dashboard.jar"]
