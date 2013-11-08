package tasklisteners;


import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import visitors.WiggleVisitor;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

import java.util.Map;

public class AfterAnalyze implements TaskListener{

	private final WiggleVisitor visitor; 
	private final GraphDatabaseService graphDb;

	public AfterAnalyze(JavacTask task, GraphDatabaseService graphDb, Map<String, String> cuProps) {
		this.visitor = new WiggleVisitor(task, graphDb, cuProps);
		this.graphDb = graphDb;
	}

	@Override
	public void finished(TaskEvent arg0) {

		if(arg0.getKind().toString().equals("ANALYZE"))
		{
				CompilationUnitTree u = arg0.getCompilationUnit();
				visitor.scan(u, null);
		}
	}

	@Override
	public void started(TaskEvent arg0) {
		// TODO Auto-generated method stub

	}

}
