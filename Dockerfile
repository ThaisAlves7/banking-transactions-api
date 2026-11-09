# Estágio 1: Compilação
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia o arquivo de dependências
COPY pom.xml .

# Baixa as dependências
RUN mvn dependency:go-offline

# Copia o código-fonte
COPY src ./src

# Compila e empacota a aplicação
RUN mvn clean package -DskipTests


# Estágio 2: Execução
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia o JAR gerado no estágio de compilação
COPY --from=build /app/target/*.jar app.jar

# Porta da aplicação
EXPOSE 8082

# Inicializa o Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]