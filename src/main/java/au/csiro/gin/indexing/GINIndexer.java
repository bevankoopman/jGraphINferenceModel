package au.csiro.gin.indexing;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;

import au.csiro.gin.Configuration;
import au.csiro.gin.knowledgebase.InfoRelationship;
import au.csiro.gin.knowledgebase.InfoUnit;
import au.csiro.gin.knowledgebase.SQLRelationshipReader;
import au.csiro.gin.retrieval.GINQuery;
import au.csiro.gin.retrieval.QueryPreprocessor;
import au.csiro.gin.retrieval.weighting.DiffusionFactor;

/**
 * Main Indexing class for the GIN that takes a Terrier index and created the GIN graph.
 * 
 * @author BevanKoopman
 *
 */
public class GINIndexer {

	private static final Logger log = Logger.getLogger(GINIndexer.class.getName());

	private static enum RelTypes implements org.neo4j.graphdb.RelationshipType {
		KNOWS
	}

	public GINIndexer(File corpusDir, File indexDir) throws IOException, JDOMException {

		// create / use terrier index
		log.info("Checking if existing index exists at: " + indexDir + "...");
		if (indexDir.exists()) {
			log.info("Terrier indexing found - using this index.");
		} else {
			log.info("No found. Running Terrier indexing.");
			corpusIndexing(corpusDir, indexDir);
		}

		// create the graph
		createGraph(indexDir);
	}

	/**
	 * Perform indexing of the corpus with Terrier.
	 * 
	 * @param corpusDir - where the corpus is located.
	 * @param indexDir - where the index will be created.
	 * @param indexingParameters - specific parameters for indexing, typically Terrier-based.
	 */
	private void corpusIndexing(File corpusDir, File indexDir) {

		log.info("Indexing " + corpusDir);

		// BasicSinglePassIndexer indexer = new BasicSinglePassIndexer(indexDir.toString(), "data"); // TODO:
		// complete
		// indexing
		// code
		// Collection collection = new
		// SimpleFileCollection(Arrays.asList(corpusDir.list()), true);
		// indexer.index(new Collection[] {collection});

	}

	/**
	 * Creates a graph from the Terrier index and domain knowledge.
	 * 
	 * @param indexDir - location of Terrier index.
	 * @throws IOException - occurs when the graph cannot be written to file.
	 * @throws JDOMException - when the queryFile can't be parsed.
	 */
	private void createGraph(File indexDir) throws IOException, JDOMException {
		log.info("Creating graph...");
		long startTime = System.currentTimeMillis();

		// the GIN graph
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(Configuration.getGraphLocation());
		registerShutdownHook(graphDb);

		// Load all Terrier's indices
		Index index = Index.createIndex();
		Lexicon<String> lex = index.getLexicon();

		DiffusionFactor dfCalculator = new DiffusionFactor(index);
		SQLRelationshipReader relationshipReader = new SQLRelationshipReader();
		Label infoUnitLabel = DynamicLabel.label("InformationUnit");

		// Create nodes for each term in the vocabulary
		try (Transaction tx = graphDb.beginTx()) { // all graph additions need to be done in transaction

			if (Configuration.filterIndexingByQueries()) {
				for (GINQuery query : QueryPreprocessor.loadQueries(Configuration.getQueryFile())) {
					for(InfoUnit iu : query.getInfoUnits()) {
						createNode(graphDb, infoUnitLabel, iu.name());
					}
				}
//				throw new NotImplementedException();
			} else {
				// for each term in the collection create a node
				for (Entry<String, LexiconEntry> entry : lex) {
					createNode(graphDb, infoUnitLabel, entry.getKey());

				}
			}

			int totalNodes = IteratorUtil.count(GlobalGraphOperations.at(graphDb).getAllNodes());
			log.info(totalNodes + " nodes created.");

			int nodeProgressCounter = 1;
			DecimalFormat format = new DecimalFormat("#.##");

			
			log.info("Creating edges...");
			// each node in the collection process their relationships
			for (Node node : GlobalGraphOperations.at(graphDb).getAllNodes()) {

				InfoUnit iu = new InfoUnit(node.getProperty(InfoUnit.ID_LABEL).toString());
				List<InfoRelationship> relationshipsForUI = relationshipReader.getRelatedInfoUnits(iu);

				log.info("Processing edges for node " + iu.name() + " ("
						+ format.format((nodeProgressCounter / (double) totalNodes) * 100) + "%) "
						+ relationshipsForUI.size() + " relationships found.");

				int edgeCount = 0;
				for (InfoRelationship rel : relationshipsForUI) {

					if (lex.getLexiconEntry(rel.source().name()) != null) { // only consider nodes in the index
						Node otherNode = graphDb.findNode(infoUnitLabel, InfoUnit.ID_LABEL, rel.source().name());
						
						// if the other code does not exist then create it
						if(otherNode == null) { 
							createNode(graphDb, infoUnitLabel, rel.source().name());
						}

						// check if edge exists - if not, create
						boolean edgeExists = false;
						for (Relationship outEdge : node.getRelationships(Direction.OUTGOING)) {
							if (outEdge.getEndNode().getProperty(InfoUnit.ID_LABEL)
									.equals(otherNode.getProperty(InfoUnit.ID_LABEL))) {
								edgeExists = true;
							}
						}
						if (!edgeExists) {

							log.debug("Creating edge " + otherNode.getProperty(InfoUnit.ID_LABEL) + " --["
									+ rel.type().name() + "]--> " + node.getProperty(InfoUnit.ID_LABEL));

							Relationship relationship = node.createRelationshipTo(otherNode, RelTypes.KNOWS);
							relationship.setProperty(
									au.csiro.gin.knowledgebase.RelationshipType.RELATIONSHIP_TYPE_LABEL, rel.type()
											.name());
							relationship.setProperty(DiffusionFactor.DIFFUSION_FACTOR_LABEL,
									dfCalculator.df(rel.source().name(), rel.target().name()));

						}
						edgeCount++;
					}

				} // for each relationship
				log.debug(edgeCount + " " + node.getDegree(Direction.OUTGOING) + " nodes created for " + iu);

				nodeProgressCounter++;
				tx.success();

			} // for each node
		} // end transaction

		int minutes = (int) (System.currentTimeMillis() - startTime) / 1000 / 60;
		double hours = minutes / (double) 60;
		String time = minutes + " minutes";
		if (hours > 1) {
			time = (int) hours + " hours";
		}
		if (hours > 24) {
			time = hours / 24.0 + " days";
		}

		log.info("Graph complete. Total running time: " + time + ".");

	}

	private Node createNode(GraphDatabaseService graphDb, Label infoUnitLabel, String term) {
		log.info("Creating node for IU " + term + " found in ");
		Node node = graphDb.createNode(infoUnitLabel);
		node.setProperty(InfoUnit.ID_LABEL, term);
		return node;
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

	public static void main(String[] args) throws IOException, JDOMException {

		File corpusDir = new File(Configuration.getCorpusDir());
		File indexDir = new File(Configuration.getIndexDir());

		new GINIndexer(corpusDir, indexDir);

	}
}
