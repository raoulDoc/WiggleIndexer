import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.kernel.impl.util.FileUtils;

import tasklisteners.AfterAnalyze;

import com.sun.source.util.JavacTask;


public class WiggleIndexerPlugin implements com.sun.source.util.Plugin{

	private static final String PLUGIN_NAME = "WiggleIndexerPlugin";
	private GraphDatabaseService graphDb;
	
	private String wiggleDbPath;
	private String wiggleClearDb;

	@Override
	public void call(JavacTask task, String[] args) {

		createDb();
		String projectName = getProjectName();
		System.out.println("Running " + PLUGIN_NAME);
		System.out.println("WIGGLE_PROJECT_NAME:" + projectName);
		System.out.println("WIGGLE_DB_PATH:" + wiggleDbPath);
		System.out.println("WIGGLE_CLEAR_DB:" + wiggleClearDb);
		

		task.setTaskListener(new AfterAnalyze(task, graphDb, projectName));
		System.out.println("finished");

	}


	private String getProjectName() {
		String projectName = System.getenv("WIGGLE_PROJECT_NAME");
		if(projectName == null)
			projectName = "NO_NAME";
		return projectName;
	}


	private String getDBPath(){
		String dbPath = System.getenv("WIGGLE_DB_PATH");
		if(dbPath == null)
			dbPath = "./neo4j/data/wiggle.db";
		return dbPath;
	}


	public void createDb()
	{

		this.wiggleDbPath = getDBPath();

		this.wiggleClearDb = System.getenv("WIGGLE_CLEAR_DB");
		if(wiggleClearDb != null && wiggleClearDb.equals("y"))
		{
			clearDb(wiggleDbPath);
		}

		
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder( wiggleDbPath ).
				setConfig( GraphDatabaseSettings.node_keys_indexable, "nodeType" ).
				setConfig( GraphDatabaseSettings.relationship_keys_indexable, "typeKind" ).
				setConfig( GraphDatabaseSettings.node_auto_indexing, "true" ).
				setConfig( GraphDatabaseSettings.relationship_auto_indexing, "true" ).
				newGraphDatabase();

		//registerShutdownHook( graphDb );

	}
	private void clearDb(String dbPath)
	{
		try
		{
			FileUtils.deleteRecursively( new File( dbPath ) );
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				graphDb.shutdown();
			}
		} );
	}

	@Override
	public String getName() {
		return PLUGIN_NAME;
	}

}
