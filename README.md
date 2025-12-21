# GlobalCluster: A Geo-Intelligent Distributed System with Monitoring

## Project Overview

GlobalCluster is a demonstration distributed system built with Spring Boot, designed to illustrate concepts of microservices architecture, geo-intelligent routing, data persistence, resilience, and Docker orchestration. It simulates a global network of nodes that register with a Gateway, are directed to "continent servers," and have their status monitored through a Dashboard.

## Key Features

*   **Geo-Intelligent Routing:** The Gateway uses MaxMind GeoIP2 to determine a node's continent based on its IP address and redirects it to a specific continent port.
*   **Virtual Continent Servers:** The Gateway itself listens on multiple ports (8081-8086), serving as a "continent server" and returning specific welcome messages.
*   **Node Auto-Registration:** Nodes automatically detect their external IP, register with the Gateway, and connect to the assigned continent port.
*   **Data Persistence (PostgreSQL):** The Gateway stores node registration data in a PostgreSQL database, ensuring data durability across Gateway restarts.
*   **Monitoring Dashboard:** A web interface to visualize registered nodes, their IPs, continents, assigned ports, and simulated metrics (ping, latency, status).
*   **IP Access Restriction:** Dashboard access is restricted to configurable IP addresses.
*   **Graceful Deregistration:** Nodes send a deregistration request to the Gateway when they shut down.  
*   **Resilience Mechanisms (Resilience4j):** Implementation of Circuit Breakers and Retries to protect HTTP calls from nodes to the Gateway, increasing system robustness.
*   **Modularity:** The project is divided into Maven modules, including a `shared` module for common DTOs and interfaces.
*   **Containerization:** All services are dockerized for easy deployment and orchestration.

## Architecture

GlobalCluster is composed of the following modules/services:

*   **`gateway`**: The entry point of the system. Responsible for node registration, Geo-IP resolution, routing to continent ports, and persistence of node data.
*   **`globalcluster-dashboard`**: Web user interface for node monitoring. Authenticated and IP-restricted. Fetches data from the `gateway`.
*   **`node`**: Represents a worker node. Auto-registers with the `gateway` and connects to the assigned continent port. Implements resilience.
*   **`master`**: A placeholder module representing a master service, not directly involved in the current Geo-IP routing functionalities.
*   **`shared`**: A Maven module containing Data Transfer Objects (DTOs) and other common definitions shared among other modules.
*   **`postgres` (via Docker Compose)**: PostgreSQL database for persisting node registration data from the `gateway`.

Communication between services is done via HTTP.

## Technologies Used

*   **Language:** Java 17+
*   **Framework:** Spring Boot 3.x
*   **Build Tool:** Apache Maven
*   **Persistence:** Spring Data JPA, Hibernate
*   **Database:** PostgreSQL
*   **Geo-IP:** MaxMind GeoIP2
*   **Resilience:** Resilience4j (Circuit Breaker, Retry)
*   **Containers:** Docker, Docker Compose
*   **Web:** Spring Web, Thymeleaf, JavaScript (Dashboard Frontend)
*   **Security:** Spring Security (basic authentication, IP restriction)
*   **Logging:** SLF4J with Logback

## How to Set Up and Run the Project

### Prerequisites

*   **Java Development Kit (JDK) 17 or higher**
*   **Apache Maven 3.6 or higher**
*   **Docker Desktop (or Docker Engine and Docker Compose)**
*   **GeoLite2-Country.mmdb Database:**
    1.  Create a free account on [MaxMind](https://www.maxmind.com/).
    2.  Download the `GeoLite2-Country.mmdb` file.
    3.  Place this file into the `gateway/src/main/resources/` directory. **This step is crucial for the GeoIP service to function.**

### Steps to Run

1.  **Build the Maven Project:**
    In the project's root directory (where the main `pom.xml` is located), execute:
    ```bash
    mvn clean install -DskipTests
    ```
    This command will compile all modules and create the necessary `.jar` files for Docker.

2.  **Build and Run Docker Containers:**
    Still in the project's root directory (where `docker-compose.yml` is located), execute:
    ```bash
    docker-compose up --build
    ```
    *   **To simulate multiple nodes:** If you wish to run more than one node, use the `--scale` flag:    
        ```bash
        docker-compose up --build --scale node=3 # Runs 1 Gateway, 1 Dashboard, 3 Nodes, 1 Master, 1 Postgres
        ```

3.  **Access the Application:**

    *   **Monitoring Dashboard:**
        Open your browser and navigate to: `http://localhost:8087/dashboard`
        *   You will be redirected to a login screen. Use the existing login mechanism (the project does not come with pre-configured users, but the login functionality is present).
        *   **IP Restriction:** Dashboard access is restricted by IP. If you are accessing from an IP other than `[your ip]` (configured in `globalcluster-dashboard/src/main/resources/application.properties`), ensure your IP is included in the `dashboard.allowed-ips` property.

    *   **Continent Servers (Gateway):**
        You can test the continent ports directly in your browser:
        *   Americas: `http://localhost:8081/`
        *   Europe: `http://localhost:8082/`
        *   Africa: `http://localhost:8083/`
        *   Asia: `http://localhost:8084/`
        *   Oceania: `http://localhost:8085/`
        *   Antarctica: `http://localhost:8086/`
        Each should return a "Welcome to the {continent} server" message.

## Important Notes

*   **Persistence:** Node registration data is stored in the PostgreSQL database and persists across service restarts.
*   **Graceful Shutdown:** When shutting down containers (`docker-compose down`), observe the node logs for deregistration messages sent to the Gateway.
*   **Resilience:** By simulating failures (e.g., stopping the `gateway` service and observing node logs), you will see Resilience4j's Retry and Circuit Breaker mechanisms in action.
*   **Node IPs:** Nodes running in Docker containers will have internal Docker network IPs (`172.x.x.x`), and the `getExternalIp()` of the node will return its container's IP, not your machine's public IP. GeoIP will still function with these IPs (either resolving them to "UNKNOWN" or to a continent based on the Docker network).

## How to Shut Down

To stop and remove all services and volumes created by Docker Compose:
```bash
docker-compose down -v
```

## Next Steps (Suggestions for Improvement)

The project can be expanded and improved with the following functionalities:

*   **Heartbeat Mechanism:** Implement regular heartbeats from nodes to the Gateway for more accurate "UP/DOWN" status.
*   **Node Identification (UUID):** Use a persistent UUID to uniquely identify nodes, regardless of IP.   
*   **Centralized Configuration:** Integrate Spring Cloud Config to manage configurations dynamically.    
*   **Metrics and Observability:** Add Actuator and Micrometer to collect real metrics, with integration to Prometheus/Grafana.
*   **Advanced Security:** Implement more robust authentication and authorization between services.       
*   **Kubernetes:** Prepare the deployment for a Kubernetes cluster.

## License

[Add your license information here, e.g., MIT License]

## Contact

[/gmail:abner.pessoal1412@gmail.com/Github:Quantum1377]