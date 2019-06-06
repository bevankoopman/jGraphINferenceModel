package au.csiro.gin.knowledgebase;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

import au.csiro.gin.Configuration;
import au.csiro.gin.retrieval.weighting.DiffusionFactor;

public class Neo4JRelationshipReader extends RelationshipReader {

	static Logger log = Logger.getLogger(Neo4JRelationshipReader.class);

	private GraphDatabaseService graphDb;
	private Label infoUnitLabel = DynamicLabel.label("InformationUnit");

	public Neo4JRelationshipReader() {
		this(Configuration.get("neo4jdb.dir"));
	}

	public Neo4JRelationshipReader(String graphDir) {
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(graphDir)
				.setConfig(GraphDatabaseSettings.read_only, "true").newGraphDatabase();
		registerShutdownHook(graphDb);

		try (Transaction tx = graphDb.beginTx()) {
			int totalNodes = IteratorUtil.count(GlobalGraphOperations.at(graphDb).getAllNodes());
			int totalRelationships = IteratorUtil.count(GlobalGraphOperations.at(graphDb).getAllRelationships());
			log.info("Graph loaded: " + totalNodes + " nodes, " + totalRelationships + " relationships.");
			if (totalNodes == 0) {
				log.fatal("ERROR: graph loaded but contained no nodes", new Exception(
						"ERROR: graph loaded but contained no nodes"));
			}
			tx.success();
		}

	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	@Override
	protected List<InfoRelationship> getRelatedInfoUnitsImpl(InfoUnit targetInformationUnit) {
		List<InfoRelationship> relationships = new ArrayList<InfoRelationship>();

		try (Transaction tx = graphDb.beginTx()) {
			org.neo4j.graphdb.Node node = graphDb.findNode(infoUnitLabel, InfoUnit.ID_LABEL, targetInformationUnit
					.name().toLowerCase());

			if (node != null) {

				for (Relationship inEdge : node.getRelationships(Direction.INCOMING)) {

					InfoUnit sourceIU = new InfoUnit(inEdge.getStartNode().getProperty(InfoUnit.ID_LABEL).toString());
					RelationshipType type = RelationshipType.get((String) inEdge.getProperty("relationshipType"));
					InfoRelationship infoRel = new InfoRelationship(sourceIU, type, targetInformationUnit);
					infoRel.setDiffusionFactor(Double.parseDouble(inEdge.getProperty(
							DiffusionFactor.DIFFUSION_FACTOR_LABEL).toString()));
					relationships.add(infoRel);
				}
			}
			tx.success();
		}

		return relationships;
	}

}
