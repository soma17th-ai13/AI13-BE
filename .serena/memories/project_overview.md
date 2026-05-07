# AI13-BE Project Overview

## Purpose
Spring Boot REST API service for AI knowledge graph agent - manages user knowledge nodes and domains, integrates with Solar API for persona-based chat.

## Tech Stack
- **Framework**: Spring Boot 4.0.6 (Java 21)
- **ORM**: JPA with Lombok
- **Database**: PostgreSQL (runtime), H2 (testing)
- **API Documentation**: SpringDoc OpenAPI 3.0.3
- **Testing**: JUnit 5, Mockito, AssertJ

## Key Components
- **Entities**: KnowledgeNode (title, content, domainName, nodeType, analyzed)
- **DTOs**: SolarChatMessage (record with role, content fields)
- **Repositories**: Spring Data JPA pattern
- **Services**: Service classes with business logic

## Directory Structure
```
src/main/java/com/soma/ai13be/
  - knowledge/
    - entity/KnowledgeNode.java
    - repository/KnowledgeNodeRepository.java
    - service/
    - controller/
    - dto/
  - chat/service/ChatService.java
  - persona/
  - common/client/dto/SolarChatMessage.java

src/test/java/com/soma/ai13be/
  - knowledge/service/
  - persona/service/
  - chat/service/
  - chat/controller/
```

## Code Conventions
- Package structure: `com.soma.ai13be.<domain>.<layer>`
- Test pattern: Direct instantiation with mocked dependencies (no @SpringBootTest)
- Mockito usage: `org.mockito.Mockito.mock()` for dependency injection
- AssertJ for assertions
- Lombok @Builder pattern for entity construction
- Korean comments and documentation

## Test Running
```bash
./gradlew test --tests "package.Class"
./gradlew test
```

## Language/Localization
- Korean commit messages and class documentation
- Korean field names and comments throughout codebase
