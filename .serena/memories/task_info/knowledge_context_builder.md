# KnowledgeContextBuilder Test Task

## Objective
Create failing test file for KnowledgeContextBuilder class (TDD approach).
The class doesn't exist yet - tests should compile with errors.

## Test File Location
`src/test/java/com/soma/ai13be/knowledge/service/KnowledgeContextBuilderTest.java`

## Test Cases Required
1. `returnsEmptyWhenNoNodesExist()` - empty Optional when no nodes found
2. `returnsSystemMessageWithFormattedNodes()` - formats nodes into system message
3. `limitsToMostRecent15Nodes()` - limits results to 15 most recent nodes

## Dependencies to Mock
- `KnowledgeNodeRepository` - findByDomainNameOrderByCreatedAtDesc(String domainName)
- Constructor: `new KnowledgeContextBuilder(nodeRepository)`

## Expected Behaviors
- Returns `Optional<SolarChatMessage>`
- System message format includes: "[사용자 지식 그래프 - {domain} 도메인]"
- Message role: "system"
- Includes node titles and content in formatted output
- Limits to 15 nodes maximum (discards older nodes)

## Related Classes
- `KnowledgeNode`: title, content, domainName, nodeType, analyzed fields
- `SolarChatMessage`: record with role, content
- `KnowledgeNodeRepository`: Spring Data interface
