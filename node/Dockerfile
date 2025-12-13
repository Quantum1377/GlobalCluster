# Use a imagem oficial do OpenJDK como imagem base
FROM eclipse-temurin:17-jdk-jammy

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo JAR do módulo node para o diretório /app no contêiner
# O JAR é construído no diretório target após o comando 'mvn clean install'
COPY target/node-1.0-SNAPSHOT.jar node.jar

# Expõe a porta que a aplicação utiliza (se houver alguma que precise ser acessada externamente para este nó)
# Por padrão, os nós se conectam ao gateway, não necessariamente expõem portas diretamente
# No entanto, se o nó precisar receber conexões de outros serviços, esta porta deve ser aberta.
# Como o nó auto-registra-se no gateway e o gateway redireciona para as portas continentais, o nó em si não precisa de porta exposta.
# Mas para fins de demonstração ou futura expansão, podemos deixar a porta padrão 8081 exposta, se o nó eventualmente virar um servidor.
EXPOSE 8081

# Define o comando para executar a aplicação JAR
ENTRYPOINT ["java", "-jar", "node.jar"]
