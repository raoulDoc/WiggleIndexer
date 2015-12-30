BUILD INSTRUCTIONS
------------------

Requirements: 

* JDK8
* gradle 2.2.1

HOW TO USE
----------

There are three optional ENV variables to set up

- *WIGGLE_PROJECT_NAME*: name of the project to index (default: NO_NAME]
- *WIGGLE_DB_PATH*: path where to locate neo4j database (default: `./neo4j/data/wiggle.db`)
- *WIGGLE_CLEAR_DB* [y/n]: erase existing database before indexing (default: n)

```
$ gradle build
$ gradle copyDeps
$ javac -cp build/libs/WiggleIndexer.jar:"build/libs/*" -Xplugin:WiggleIndexerPlugin samples/Test.java
```

Generated jar is available at `./WiggleIndexer/build/libs`

By default the generated database is located under `./neo4j/data/wiggle.db`. You can then select the full path using the Neo4j frontend to access the database.

EXAMPLE QUERIES
---------------

Example queries:

1) Wildcards

    START n=node:node_auto_index(nodeType='JCWildcard') MATCH p-[:ENCLOSES]->c-[*]->n WHERE has(p.nodeType) AND p.nodeType='JCCompilationUnit' AND has(c.nodeType) AND c.nodeType='JCClassDecl' RETURN n.typeBoundKind, p.fileName, p.projectName, n.lineNumber, c.name LIMIT 10;

2) countDistinctMethod

    START n=node:node_auto_index(nodeType ='JCMethodDecl') MATCH p-[*]->c-[:DECLARES]->n WHERE NOT(n.name = '<init>') AND has(c.nodeType) AND c.nodeType='JCClassDecl' AND c.name <> "" AND has(p.nodeType) AND p.nodeType='JCCompilationUnit' WITH n.name +"[sep]"+ c.name + "[sep]"+ p.fileName as method RETURN COUNT(method) as total

3) countOverloaded

    START n=node:node_auto_index(nodeType ='JCMethodDecl') MATCH p-[*]->c-[:DECLARES]->n WHERE NOT(n.name = '<init>') AND has(c.nodeType) AND c.nodeType='JCClassDecl' AND c.name <> "" AND has(p.nodeType) AND p.nodeType='JCCompilationUnit' WITH n.name +"[sep]"+ c.name + "[sep]"+ p.fileName as method, count(*) as overloadedCount WHERE overloadedCount > 1 RETURN SUM(overloadedCount) as total;

4) fetchOverloaded

    START n=node:node_auto_index(nodeType ='JCMethodDecl') MATCH p-[*]->c-[:DECLARES]->n WHERE NOT(n.name = '<init>') AND has(c.nodeType) AND c.nodeType='JCClassDecl' AND c.name <> "" AND has(p.nodeType) AND p.nodeType='JCCompilationUnit' WITH n.name +"[sep]"+ c.name + "[sep]"+ p.fileName as method, count(*) as overloadedCount WHERE overloadedCount > 1 RETURN method, overloadedCount ORDER BY overloadedCount desc LIMIT 10;

5) covariant assignments

    start n=node:node_auto_index(nodeType='JCAssign') match 
    	n-[:LHS]->lhsNode-[relLeft:TYPE]->lhsType, 
    	n-[:RHS]->rhsNode-[relRight:TYPE]->rhsType, 
    	u-[*]->c-[*]->temp-[:ENCLOSES]->n 
    	WHERE(has(u.nodeType) AND u.nodeType='JCCompilationUnit' AND 
    	has(c.nodeType) AND c.nodeType='JCClassDecl' AND has(relLeft.typeKind) AND 
    	relLeft.typeKind='ArrayType' AND has(relRight.typeKind) AND 
    	relRight.typeKind='ArrayType' AND lhsType.actualType <> rhsType.actualType) 
    	return distinct u.projectName, u.fileName, c.name, rhsNode.lineNumber, lhsType.actualType, rhsType.actualType LIMIT 10;
		
