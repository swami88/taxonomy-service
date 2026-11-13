# Taxonomy Service

A Spring Boot service for storing and querying hierarchical taxonomy nodes with a closure table in SQLite.

## Requirements

- Java 21

## Run locally

```powershell
.\mvnw.cmd spring-boot:run
```

The service starts on `http://localhost:8080` and stores data in `taxonomy.db` in the project root. The database schema is created or updated automatically at startup.

## Test

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean verify
.\mvnw.cmd -Dtest=TaxonomyNodeServiceTests test
```

## API

### Create a root node

```powershell
curl.exe -X POST http://localhost:8080/api/nodes `
  -H "Content-Type: application/json" `
  -d '{"name":"Animals"}'
```

### Create a child node

```powershell
curl.exe -X POST http://localhost:8080/api/nodes `
  -H "Content-Type: application/json" `
  -d '{"name":"Mammals","parentId":1}'
```

The response contains the created node and its direct parent:

```json
{
  "id": 2,
  "name": "Mammals",
  "parentId": 1
}
```

### Read a node

```powershell
curl.exe http://localhost:8080/api/nodes/2
```

### Read ancestor and descendant paths

```powershell
curl.exe http://localhost:8080/api/nodes/2/ancestors
curl.exe http://localhost:8080/api/nodes/1/descendants
```

Each path result contains `id`, `name`, and `depth`. A direct relation has depth `1`; the node itself is excluded.

### Rename a node

```powershell
curl.exe -X PATCH http://localhost:8080/api/nodes/2 `
  -H "Content-Type: application/json" `
  -d '{"name":"Mammalia"}'
```

Requests for a node that does not exist return `404 Not Found`.

## Closure-table storage

`taxonomy_nodes` stores node IDs and names. `taxonomy_node_closure` stores every ancestor/descendant pair:

| ancestor_id | descendant_id | depth |
| --- | --- | --- |
| 1 | 1 | 0 |
| 1 | 2 | 1 |
| 2 | 2 | 0 |

When a child is created, the service writes its self-reference (`depth = 0`) and one new closure row for every ancestor of its parent. This makes ancestor and descendant queries independent of tree depth.
