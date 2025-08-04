# Disclaimer: Design Choices and Trade-offs

This project implements a basic digital wallet service using Java and the Helidon MicroProfile framework. The design prioritizes clarity, modularity, and adherence to common architectural patterns, while also addressing key functional and non-functional requirements.

**1. Design Choices and Architecture:**

*   **Layered Architecture:** The application follows a classic layered architecture:
    *   **Controller Layer (`WalletResource`):** Handles incoming HTTP requests, parses input, and returns responses. It acts as the entry point for the REST API.
    *   **Service Layer (`WalletService`):** Contains the core business logic for wallet operations. It orchestrates interactions with the repository and enforces business rules.
    *   **Repository Layer (`WalletRepository`, `InMemoryWalletRepository`):** Defines the contract for data access and provides an in-memory implementation for simplicity and rapid development.
    *   **Model Layer (`Wallet`, `Transaction`):** Represents the core data entities of the application.
    This separation of concerns promotes maintainability, testability, and scalability.

*   **RESTful Endpoints:** The `WalletResource` exposes clear and intuitive RESTful endpoints for each functional requirement (create, retrieve balance, historical balance, deposit, withdraw, transfer). This design promotes interoperability and ease of consumption by client applications.

*   **Dependency Injection (CDI):** Helidon's MicroProfile capabilities are leveraged for dependency injection (`@Inject`, `@ApplicationScoped`), simplifying component management and promoting loose coupling.

*   **Concurrency Control (`ReentrantLock`):** To ensure data consistency in a multi-threaded environment, a `ReentrantLock` is used per wallet within the `WalletService`. This prevents race conditions during concurrent modifications (deposits, withdrawals, transfers) to the same wallet, ensuring that monetary transactions are atomic.

*   **Traceability (Logging):** Detailed logging (`java.util.logging.Logger`) has been integrated into the `WalletService` for all critical monetary operations (create, deposit, withdraw, transfer). This provides a comprehensive audit trail, crucial for meeting the traceability requirement for financial transactions.

*   **Health Checks (MicroProfile Health):** A custom liveness health check (`WalletHealthCheck`) is implemented using MicroProfile Health. This allows external monitoring systems (e.g., Kubernetes) to verify the application's operational status by checking the accessibility of the `WalletRepository`. This directly addresses the non-functional requirement for high availability and mission-critical status.

**2. How Requirements are Met:**

*   **Functional Requirements:**
    *   **Create Wallet:** `POST /wallets/{userId}` handled by `WalletResource.createWallet()`.
    *   **Retrieve Balance:** `GET /wallets/{userId}/balance` handled by `WalletResource.getBalance()`.
    *   **Retrieve Historical Balance:** `GET /wallets/{userId}/balance/historical?timestamp={timestamp}` handled by `WalletResource.getHistoricalBalance()`.
    *   **Deposit Funds:** `POST /wallets/{userId}/deposit` handled by `WalletResource.deposit()`.
    *   **Withdraw Funds:** `POST /wallets/{userId}/withdraw` handled by `WalletResource.withdraw()`.
    *   **Transfer Funds:** `POST /wallets/transfer?fromUserId={fromUserId}&toUserId={toUserId}` handled by `WalletResource.transfer()`.

*   **Non-functional Requirements:**
    *   **Mission-Critical / Downtime:** The inclusion of a MicroProfile Health check allows for external monitoring and orchestration (e.g., Kubernetes readiness/liveness probes) to ensure the service is healthy and to manage its lifecycle effectively, minimizing downtime.
    *   **Full Traceability:** Comprehensive logging of all wallet operations in the `WalletService` provides an audit trail, fulfilling the traceability requirement for auditing wallet balances.
    *   **Concurrency Safety:** The `ReentrantLock` mechanism in `WalletService` ensures that concurrent modifications to the same wallet are synchronized, preventing data corruption and maintaining the integrity of monetary transactions.

**3. Compromises and Trade-offs (due to time constraints):**

*   **In-Memory Data Storage:** The current implementation uses an `InMemoryWalletRepository`. This is a significant trade-off for a mission-critical financial service, as all data is lost when the application restarts. For a production environment, this would need to be replaced with a persistent database solution (e.g., PostgreSQL, Oracle, Cassandra) to ensure data durability.
*   **Error Handling Granularity:** While basic error handling is present (e.g., "Wallet not found", "Insufficient funds"), a production-grade application would require more granular and standardized error responses (e.g., custom exception mapping, specific HTTP status codes for different error types, detailed error payloads).
*   **Authentication and Authorization:** No security mechanisms (authentication or authorization) are implemented. In a real-world scenario, especially for financial transactions, robust security measures would be paramount.
*   **Scalability of Locks:** While `ReentrantLock` provides thread safety, managing a `ConcurrentHashMap` of locks for every single user might become a bottleneck in extremely high-concurrency scenarios with a massive number of active users. For extreme scale, alternative approaches like optimistic locking or event sourcing might be considered, but `ReentrantLock` is generally sufficient for many concurrent applications.
*   **Transaction Management:** The current "transactions" are simply records within the `Wallet` object. A more robust system would integrate with a proper database transaction manager to ensure atomicity, consistency, isolation, and durability (ACID properties) across multiple operations, especially for transfers.
*   **Historical Balance Performance:** The `getHistoricalBalance` method iterates through all past transactions. For wallets with a very large number of transactions, this could become a performance bottleneck. In a production system, this might be optimized with pre-aggregated historical data or a more efficient data structure/query.

These compromises were made to deliver a functional prototype within the given time constraints, focusing on the core business logic and immediate non-functional requirements. For a production deployment, these areas would require further development and robust solutions.
