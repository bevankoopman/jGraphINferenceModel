package au.csiro.gin.indexing;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkGML;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.indexing.singlepass.BasicSinglePassIndexer;

import au.csiro.gin.Configuration;
import au.csiro.gin.knowledgebase.InfoRelationship;
import au.csiro.gin.knowledgebase.InfoUnit;
import au.csiro.gin.knowledgebase.RelationshipType;
import au.csiro.gin.knowledgebase.SQLRelationshipReader;
import au.csiro.gin.retrieval.weighting.DiffusionFactor;

/**
 * @author BevanKoopman
 *
 */
public class GINIndexerGraphStream {

	private static final Logger log = Logger.getLogger(GINIndexer.class.getName());
	public static final String GRAPH_FILENAME = "graph.gml";

	public GINIndexerGraphStream(File corpusDir, File indexDir) throws IOException {

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

		BasicSinglePassIndexer indexer = new BasicSinglePassIndexer(indexDir.toString(), "data"); // TODO:
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
	 */
	private void createGraph(File indexDir) throws IOException {
		log.info("Creating graph...");
		long startTime = System.currentTimeMillis();

		// the GIN graph
		SingleGraph GINgraph = new SingleGraph("GIN", false, false);

		// Load all Terrier's indices
		Index index = Index.createIndex();
		Lexicon<String> lex = index.getLexicon();

		DiffusionFactor dfCalculator = new DiffusionFactor(index);
		SQLRelationshipReader relationshipReader = new SQLRelationshipReader();

		// Create nodes for each term in the vocabulary
		String term;
		LexiconEntry termLexEntry;
		List<String> terms = new ArrayList<String>();
		for (Entry<String, LexiconEntry> entry : lex) { // each term in the
														// collection
			term = entry.getKey();
			termLexEntry = entry.getValue();
			log.info("Creating node for IU " + term + " found in " + termLexEntry.getFrequency() + " documents.");
			GINgraph.addNode(term);
			terms.add(term);
		}

		// create edges between nodes
		double nodeProgressCounter = 1;
		DecimalFormat format = new DecimalFormat("#.##");
		for (String theTerm : terms) {

			Node node = GINgraph.getNode(theTerm);

			for (InfoRelationship rel : relationshipReader.getRelatedInfoUnits(new InfoUnit(node.getId()))) {

				// check if related node exists - if note, create
				Node otherNode = GINgraph.getNode(rel.source().name());
				if (otherNode == null) {
					log.info("Creating node for IU " + rel.source().name() + " found in 0 documents.");
					otherNode = GINgraph.addNode(rel.source().name());
				}

				// check if edge exists - if note, create
				String edgeId = otherNode + "-" + rel.type().name() + "-" + node; // node1-rel-node2
				if (GINgraph.getEdge(edgeId) == null) {

					log.info("Creating edge " + otherNode + " --[" + rel.type().name() + "]--> " + node + "\t"
							+ format.format((nodeProgressCounter / GINgraph.getNodeCount()) * 100) + "%");

					Edge edge = GINgraph.addEdge(edgeId, otherNode, node);
					if (edge != null) {
						if (rel != null) {
							
							if(rel.type() != null) {
								
								edge.addAttribute(RelationshipType.RELATIONSHIP_TYPE_LABEL, rel.type().name());
								edge.addAttribute(DiffusionFactor.DIFFUSION_FACTOR_LABEL,
										dfCalculator.df(rel.source().name(), rel.target().name()));
							} else {
								log.fatal("Rel type null");
							}
						} else {
							log.fatal("Rel is null");
						}
					} else {
						log.fatal("Edge is null");
					}

				}

			}
			nodeProgressCounter++;
		}

		GINgraph.write(new FileSinkGML(), indexDir + "/" + GRAPH_FILENAME);
		int minutes = (int) (System.currentTimeMillis() - startTime) / 1000 / 60;
		log.info("Serialisation of graph complete. Total running time: " + minutes);

	}

	public static void main(String[] args) throws IOException {

		File corpusDir = new File(Configuration.getCorpusDir());
		File indexDir = new File(Configuration.getIndexDir());

		new GINIndexerGraphStream(corpusDir, indexDir);

	}
}
