# GlobalCluster: Um Sistema Distribuído com Roteamento Geo-Inteligente e Monitoramento

## Visão Geral do Projeto

O GlobalCluster é um sistema distribuído de demonstração construído com Spring Boot, projetado para ilustrar conceitos de arquitetura de microsserviços, roteamento geo-inteligente, persistência de dados, resiliência e orquestração com Docker. Ele simula uma rede global de nós que se registram em um Gateway, são direcionados para "servidores de continente" e têm seu status monitorado através de um Dashboard.

## Funcionalidades Principais

*   **Roteamento Geo-Inteligente:** O Gateway utiliza MaxMind GeoIP2 para determinar o continente de um nó com base em seu endereço IP e redirecioná-lo para uma porta específica do continente.
*   **Servidores de Continente Virtuais:** O próprio Gateway escuta em múltiplas portas (8081-8086), servindo como "servidor de continente" e retornando mensagens de boas-vindas específicas.
*   **Auto-registro de Nós:** Os nós detectam automaticamente seu IP externo, registram-se no Gateway e se conectam à porta do continente designada.
*   **Persistência de Dados (PostgreSQL):** O Gateway armazena os registros dos nós em um banco de dados PostgreSQL, garantindo a durabilidade dos dados.
*   **Dashboard de Monitoramento:** Uma interface web para visualizar os nós registrados, seus IPs, continentes, portas atribuídas e métricas simuladas (ping, latência, status).
*   **Restrição de Acesso por IP:** O acesso ao Dashboard é restrito a IPs configuráveis.
*   **Deregistro Gracioso:** Nós enviam uma requisição de desregistro ao Gateway quando são desligados.
*   **Mecanismos de Resiliência (Resilience4j):** Implementação de Circuit Breakers e Retries para proteger as chamadas HTTP dos nós para o Gateway, aumentando a robustez do sistema.
*   **Modularidade:** O projeto é dividido em módulos Maven, incluindo um módulo `shared` para DTOs e interfaces comuns.
*   **Containerização:** Todos os serviços são dockerizados para fácil deployment e orquestração.

## Arquitetura

O GlobalCluster é composto pelos seguintes módulos/serviços:

*   **`gateway`**: Ponto de entrada do sistema. Responsável pelo registro de nós, resolução Geo-IP, roteamento para portas de continente e persistência dos dados dos nós.
*   **`globalcluster-dashboard`**: Interface de usuário web para monitoramento dos nós. Autenticado e restrito por IP. Busca dados do `gateway`.
*   **`node`**: Representa um nó de trabalho. Auto-registra-se no `gateway` e se conecta à porta do continente atribuída. Implementa resiliência.
*   **`master`**: Módulo placeholder que representa um serviço mestre, não diretamente envolvido nas funcionalidades atuais de roteamento Geo-IP.
*   **`shared`**: Módulo Maven contendo classes DTO (Data Transfer Objects) e outras definições comuns compartilhadas entre os outros módulos.
*   **`postgres` (via Docker Compose)**: Banco de dados PostgreSQL para persistência dos dados de registro de nós do `gateway`.

A comunicação entre os serviços é feita via HTTP.

## Tecnologias Utilizadas

*   **Linguagem:** Java 17+
*   **Framework:** Spring Boot 3.x
*   **Build Tool:** Apache Maven
*   **Persistência:** Spring Data JPA, Hibernate
*   **Banco de Dados:** PostgreSQL
*   **Geo-IP:** MaxMind GeoIP2
*   **Resiliência:** Resilience4j (Circuit Breaker, Retry)
*   **Contêineres:** Docker, Docker Compose
*   **Web:** Spring Web, Thymeleaf, JavaScript (Frontend do Dashboard)
*   **Segurança:** Spring Security (autenticação básica, restrição por IP)
*   **Logs:** SLF4J com Logback

## Como Configurar e Rodar o Projeto

### Pré-requisitos

*   **Java Development Kit (JDK) 17 ou superior**
*   **Apache Maven 3.6 ou superior**
*   **Docker Desktop (ou Docker Engine e Docker Compose)**
*   **Banco de Dados GeoLite2-Country.mmdb:**
    1.  Crie uma conta gratuita em [MaxMind](https://www.maxmind.com/).
    2.  Baixe o arquivo `GeoLite2-Country.mmdb`.
    3.  Coloque este arquivo no diretório `gateway/src/main/resources/`. **Este passo é crucial para o serviço GeoIP funcionar.**

### Passos para Rodar

1.  **Construa o Projeto Maven:**
    No diretório raiz do projeto (onde está o `pom.xml` principal), execute:
    ```bash
    mvn clean install -DskipTests
    ```
    Este comando compilará todos os módulos e criará os arquivos `.jar` necessários para o Docker.

2.  **Construa e Execute os Contêineres Docker:**
    Ainda no diretório raiz do projeto (onde está o `docker-compose.yml`), execute:
    ```bash
    docker-compose up --build
    ```
    *   **Para simular múltiplos nós:** Se desejar rodar mais de um nó, use a flag `--scale`:
        ```bash
        docker-compose up --build --scale node=3 # Roda 1 Gateway, 1 Dashboard, 3 Nós, 1 Master, 1 Postgres
        ```

3.  **Acesse a Aplicação:**

    *   **Dashboard de Monitoramento:**
        Abra seu navegador e acesse: `http://localhost:8087/dashboard`
        *   Você será redirecionado para uma tela de login. Use o mecanismo de login existente (o projeto não vem com usuários pré-configurados, mas a funcionalidade de login está presente).
        *   **Restrição de IP:** O acesso ao dashboard é restrito por IP. Se você estiver acessando de um IP diferente do `127.0.0.1` ou `192.168.1.7` (configurado no `globalcluster-dashboard/src/main/resources/application.properties`), certifique-se de que seu IP está incluído na propriedade `dashboard.allowed-ips`.

    *   **Servidores de Continente (Gateway):**
        Você pode testar as portas dos continentes diretamente no navegador:
        *   Américas: `http://localhost:8081/`
        *   Europa: `http://localhost:8082/`
        *   África: `http://localhost:8083/`
        *   Ásia: `http://localhost:8084/`
        *   Oceania: `http://localhost:8085/`
        *   Antártica: `http://localhost:8086/`
        Cada uma deve retornar uma mensagem "Bem vindo ao servidor da/do {continente}".

## Observações Importantes

*   **Persistência:** Os dados de registro dos nós são armazenados no banco de dados PostgreSQL e persistem entre reinicializações dos serviços.
*   **Desregistro Gracioso:** Ao desligar os contêineres (`docker-compose down`), observe os logs dos nós para ver as mensagens de desregistro enviadas ao Gateway.
*   **Resiliência:** Ao tentar simular falhas (ex: parar o serviço `gateway` e observar os logs dos `nodes`), você verá os mecanismos de retentativa e Circuit Breaker do Resilience4j em ação.
*   **IPs dos Nós:** Os nós rodando em contêineres Docker terão IPs internos da rede Docker (`172.x.x.x`), e o `getExternalIp()` do nó retornará o IP do seu contêiner, não o IP público da sua máquina. O GeoIP continuará funcionando com esses IPs (seja resolvendo-os para "UNKNOWN" ou para um continente baseado na rede Docker).

## Como Desligar

Para parar e remover todos os serviços e volumes criados pelo Docker Compose:
```bash
docker-compose down -v
```

## Próximos Passos (Sugestões de Melhoria)

O projeto pode ser expandido e aprimorado com as seguintes funcionalidades:

*   **Mecanismo de Heartbeat:** Implementar heartbeats regulares dos nós para o Gateway para ter um status "UP/DOWN" mais preciso.
*   **Identificação de Nó (UUID):** Usar um UUID persistente para identificar nós de forma única, independentemente do IP.
*   **Configuração Centralizada:** Integrar Spring Cloud Config para gerenciar configurações dinamicamente.
*   **Métricas e Observabilidade:** Adicionar Actuator e Micrometer para coletar métricas reais, com integração a Prometheus/Grafana.
*   **Segurança Avançada:** Implementar autenticação e autorização mais robustas entre serviços.
*   **Kubernetes:** Preparar o deployment para um cluster Kubernetes.

## Licença

[Adicione aqui as informações da sua licença, ex: MIT License]

## Contato

[Seu Nome/Email/Perfil do GitHub]