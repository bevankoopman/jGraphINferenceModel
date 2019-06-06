/*
 * GIN - Graph INference Model 
 * CSIRO - Australia e-Health Research Centre.
 * 
 * GIN is a retrieval model which integrates structured knowledge resources, statistical information retrieval methods
 * and inference in a unified framework. Key components of the model are a graph-based representation of the corpus
 * and retrieval driven by an inference mechanism achieved as a traversal over the graph.
 * 
 * Copyright 2008 CSIRO Australian e-Health Research Centre.
 * All rights reserved. Use is subject to license terms and conditions.
 * 
 * Contributor(s): Bevan Koopman <bevan.koopman{a.}csiro.au>
 */
package au.csiro.gin.retrieval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.terrier.matching.CollectionResultSet;
import org.terrier.matching.ResultSet;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.IterablePosting;

import au.csiro.gin.Configuration;
import au.csiro.gin.knowledgebase.InfoRelationship;
import au.csiro.gin.knowledgebase.InfoUnit;
import au.csiro.gin.knowledgebase.Neo4JRelationshipReader;
import au.csiro.gin.knowledgebase.RelationshipReader;
import au.csiro.gin.knowledgebase.SQLRelationshipReader;
import au.csiro.gin.retrieval.weighting.BM25WeightingModel;
import au.csiro.gin.retrieval.weighting.LemurTFIDFWeightingModel;
import au.csiro.gin.retrieval.weighting.TFIDFWeigthingModel;
import au.csiro.gin.retrieval.weighting.WeightingModel;

public class GINRetrieval {

	public static final Logger log = Logger.getLogger(GINRetrieval.class.getName());
	// private static Properties parameters;

	// Terrier data index data structures
	private Index index;
	private Lexicon<String> lexicon;
	String[] docNames;

	private RelationshipReader relationshipReader;
	private WeightingModel weightingModel;

	/**
	 * Public constructor using default index location.
	 */
	public GINRetrieval() throws IOException {
		this(new File(Configuration.getIndexDir()));
	}

	/**
	 * Public constructor.
	 * 
	 * @param indexDir - location of the Terrier index.
	 * @throws IOException - when the index cannot be found/read.
	 */
	public GINRetrieval(File indexDir) throws IOException {

		// setup
		loadIndex(indexDir);
		initialiseRelationshipReader();
		initialiseWeightingModel();

	}

	/**
	 * Run retrieval from a query set file; write results to TREC output file.
	 *
	 * @throws IOException - when querySetFile cannot be read or results file written.
	 * @throws JDOMException - when querySetFile cannot be XML parsed.
	 */
	public void retrievalForQuerySet() throws IOException, JDOMException {
		if(!new File(Configuration.getResultsDir()).mkdir()) {
			throw new IOException("Unable to create results directory "+Configuration.getResultsDir());
		}

		// for printing results to TREC format
		String resultsFileName = Configuration.getResultsDir() + "/gin-" +new File(Configuration.getGraphLocation()).getName() + "." + Configuration.getDepth()+".results";
		FileWriter fw = new FileWriter(resultsFileName);

		// load and iterate over each query for retrieval
		List<GINQuery> queries = QueryPreprocessor.loadQueries(Configuration.get("query.file"));
		for (GINQuery query : queries) {

			// perform RETRIEVAL for this query, returning scored docs
			double[] scores = processQuery(query, Configuration.getDepth());

			// print the score in TREC format.
			ResultSet resultSet = new CollectionResultSet(index.getDocumentIndex().getNumberOfDocuments());
			resultSet.initialise(scores);
			resultSet.sort();

			// write the sorted scores to file
			for (int i = 0; i < scores.length && i < Configuration.getResultsSize(); i++) {
				// ignore negative infinity values
				if (resultSet.getScores()[i] == Double.NEGATIVE_INFINITY) {
					continue;
				}
				fw.write(formatRetrievalResultsTrecEval(query.id(), docNames[resultSet.getDocids()[i]], i,
						resultSet.getScores()[i], resultSet.getDocids()[i], weightingModel.name()));
			}

		}
		fw.close();
		log.info("Retrievel complete. Results written to: " + resultsFileName);
	}

	public JSONArray retrievalForSingleQuery(GINQuery query, int maxDepth) throws IOException {

		double[] scores = processQuery(query, maxDepth);

		ResultSet resultSet = new CollectionResultSet(index.getDocumentIndex().getNumberOfDocuments());
		resultSet.initialise(scores);
		resultSet.sort();

		JSONArray jsonArray = new JSONArray();

		// write the sorted scores to file
		for (int i = 0; i < scores.length && i < Configuration.getResultsSize(); i++) {
			// ignore negative infinity values
			if (resultSet.getScores()[i] == Double.NEGATIVE_INFINITY) {
				continue;
			}

			JSONObject json = new JSONObject();
			json.put("docId", docNames[resultSet.getDocids()[i]]);
			json.put("rank", i);
			json.put("score", resultSet.getScores()[i]);
			json.put("model", weightingModel.name());
			jsonArray.add(json);

		}

		return jsonArray;
	}

	/**
	 * Main retrieval method for a single query.
	 * 
	 * @param query - Query object representing a single query, @see Query.
	 * @param maxDepth - the depth parameter k, representing how deep to traverse.
	 * @throws IOException - when the Terrier index cannot be read.
	 */
	private double[] processQuery(GINQuery query, int maxDepth) throws IOException {
		log.info("Query: " + query.id());

		double[] scores = new double[index.getDocumentIndex().getNumberOfDocuments()];
		Arrays.fill(scores, Double.NEGATIVE_INFINITY);

		// currently, query terms are consider independent, hence score
		// one-by-one
		for (InfoUnit infoUnit : query.getInfoUnits()) {
			log.info("\tProcessing query IU " + infoUnit.name());
			int numNodesVisited = traverse(query, infoUnit, scores, 0, maxDepth, new ArrayList<InfoRelationship>(), 0);
			log.info("\t  (" + numNodesVisited + " nodes visited.)");
		}

		return scores;
	}

	/**
	 * Retrieval for a single InfoUnit by traversing its edges to maxDepth.
	 * 
	 * @param query - the over-arching query to which this InfoUnit belongs.
	 * @param iu - the InfoUnit to traverse from.
	 * @param scores - an array of scores for each document, indexed by docId
	 * @param depth - the current depth.
	 * @param maxDepth - the depth parameter k, representing how deep to traverse.
	 * @param pathBackToQueryIU - represents the sequence of InformationRelations leading from current IU to root query
	 *            IU; used to calculate the diffusion factor.
	 * @throws IOException - when the Terrier index cannot be read.
	 */
	private int traverse(GINQuery query, InfoUnit iu, double[] scores, int depth, int maxDepth,
			List<InfoRelationship> pathBackToQueryIU, int numNodesVisited) throws IOException {
		score(query, iu, scores, pathBackToQueryIU);
		numNodesVisited++;
		if (depth < maxDepth) {
			//log.debug("\t\tTraverse " + relationshipReader.getRelatedInfoUnits(iu).size() + " children of " + iu);
			for (InfoRelationship relationships : relationshipReader.getRelatedInfoUnits(iu)) {
				pathBackToQueryIU.add(relationships);
				numNodesVisited = traverse(query, relationships.source(), scores, depth + 1, maxDepth,
						pathBackToQueryIU, numNodesVisited);
				pathBackToQueryIU.remove(relationships);
			}
		}
		return numNodesVisited;
	}

	/**
	 * Score all the documents attached to this InfoUnit node.
	 * 
	 * @param query - the over-arching query to which this InfoUnit belongs.
	 * @param iu - the current InfoUnit being scored.
	 * @param scores - an array of scores for each document, indexed by docId
	 * @param pathBackToQueryIU - path back to the query InfoUnit; used to calculate diffusion factor.
	 * @throws IOException - when the Terrier index cannot be read.
	 */
	private void score(GINQuery query, InfoUnit iu, double[] scores, List<InfoRelationship> pathBackToQueryIU)
			throws IOException {
		log.trace("\t\tScoring node " + iu + "...");
		LexiconEntry lexEntry = lexicon.getLexiconEntry(iu.name());
		if (lexEntry != null) {
			IterablePosting postingList = index.getInvertedIndex().getPostings(lexEntry);
			// for each document in the posting list
			while (!postingList.endOfPostings()) {
				int docId = postingList.next();
				double df = diffusionFactor(pathBackToQueryIU);
				double score = weightingModel.scoreDocument(query, iu, docId, index.getDocumentIndex()
						.getDocumentLength(docId), postingList.getFrequency(), lexEntry.getDocumentFrequency(),
						lexEntry.getFrequency(), index.getDocumentIndex().getNumberOfDocuments(), index
								.getCollectionStatistics().getAverageDocumentLength(), df);
				if (scores[docId] == Double.NEGATIVE_INFINITY) {
					scores[docId] = 0.0;
				}
				scores[docId] += score;
				log.trace("\t\t\tscore(" + docId + "/" + docNames[docId] + "|" + iu.name() + ") = " + score);
			}
		}
	}

	/**
	 * Diffusion factor calculation.
	 * 
	 * @param pathBackToQueryIU - list of InfoRelationships leading from current InfoUnit to query InfoUnit.
	 * @return diffusion factor value.
	 * @throws IOException - when the Terrier index cannot be read.
	 */
	private double diffusionFactor(List<InfoRelationship> pathBackToQueryIU) throws IOException {
		double df = 1.0;
		for (InfoRelationship rel : pathBackToQueryIU) {
			df *= (Configuration.getDiffusionMix() * rel.diffusionFactor()) + (1 - Configuration.getDiffusionMix())
					* rel.type().weight();
		}
		return df;
	}

	// ////////////////////////////////////////////////////////////////////////
	// Setup and initialize methods.
	// ////////////////////////////////////////////////////////////////////////

	

	/**
	 * Reads all the Terrier indices and data structures.
	 * 
	 * @param indexDir - location of Terrier index.
	 * @throws IOException - if the index cannot be read.
	 */
	private void loadIndex(File indexDir) throws IOException {
		log.info("Loading terrier index: " + indexDir);

		// Load all Terrier's indices
		index = Index.createIndex(indexDir.getAbsolutePath(), "data");
		lexicon = index.getLexicon();

		// This method returns a <Integer, String> map with Integer corresponding to the filename String.
		docNames = new String[index.getDocumentIndex().getNumberOfDocuments()];
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
				Configuration.get("terrier.home") + "/etc/collection.spec"), "UTF-8"));
		String line;
		int id = 0;
		String docName;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#") || line.trim().length() == 0) { // ignore comment & blank lines
				continue;
			}
			docName = line.substring(line.lastIndexOf(File.separatorChar) + 1).replace(".txt", "");
			docNames[id++] = docName;
		}
		br.close();

	}

	/**
	 * Instantiate a class for the WeightingModel. Defaults to {@link TFIDFWeigthingModel}.
	 */
	private void initialiseWeightingModel() {
		String weightingModelClassName = Configuration
				.get("weighting.model", TFIDFWeigthingModel.class.getSimpleName());

		if (TFIDFWeigthingModel.class.getName().equals(weightingModelClassName)) {
			weightingModel = new TFIDFWeigthingModel();
		} else if (LemurTFIDFWeightingModel.class.getName().equals(weightingModelClassName)) {
			double k1 = Double.parseDouble(Configuration.get(LemurTFIDFWeightingModel.class.getName() + ".k1",
					Double.toString(LemurTFIDFWeightingModel.k1)));
			double b = Double.parseDouble(Configuration.get(LemurTFIDFWeightingModel.class.getName() + ".b",
					Double.toString(LemurTFIDFWeightingModel.b)));
			weightingModel = new LemurTFIDFWeightingModel(k1, b);
		} else if (BM25WeightingModel.class.getName().equals(weightingModelClassName)) {
			double k1 = Double.parseDouble(Configuration.get(BM25WeightingModel.class.getName() + ".k1",
					Double.toString(BM25WeightingModel.k1)));
			double k3 = Double.parseDouble(Configuration.get(BM25WeightingModel.class.getName() + ".k3",
					Double.toString(BM25WeightingModel.k3)));
			double b = Double.parseDouble(Configuration.get(BM25WeightingModel.class.getName() + ".b",
					Double.toString(BM25WeightingModel.b)));
			weightingModel = new BM25WeightingModel(k1, k3, b);
		} else {
			weightingModel = new TFIDFWeigthingModel();
		}
		log.info("Weighting model set to " + weightingModel.getClass().getName());
	}

	/**
	 * Instantiate a class for the RelationshipReader. Defaults to {@link SQLRelationshipReader}.
	 */
	private void initialiseRelationshipReader() {
		log.info("Loading GIN graph.");
		String readerClassName = Configuration.get("relationshipReader", Neo4JRelationshipReader.class.getName());
		try {
			relationshipReader = (RelationshipReader) Class.forName(readerClassName).newInstance();
		} catch (Exception e) {
			log.fatal("Unable to instanciate the RelationshipReader " + readerClassName, e);
		}

	}

	/**
	 * Format result for single doc in trec_eval format.
	 * 
	 * @param qId - queryID
	 * @param docName - name of the document on disk.
	 * @param rank - rank of the document.
	 * @param score - retrieval score.
	 * @param docId - terrier's doc id.
	 * @param weightingModel - name of the weighting model (e.g., BM25).
	 * @return results in trec format.
	 */
	public static String formatRetrievalResultsTrecEval(int qId, String docName, int rank, double score,
			int docId, String weightingModel) {
		return qId + " Q0 " + docName + " " + rank + " " + score + " " + docId + "_" + weightingModel + "\n";
	}

	/**
	 * MAIN.
	 * 
	 * @param args - command line arguments.
	 * 
	 * @throws IOException - when the index dir or query file cannot be found/read.
	 * @throws JDOMException - when the query file cannot be parsed.
	 */
	public static void main(String[] args) throws IOException, JDOMException {

		File indexDir = new File(Configuration.getIndexDir());
		new GINRetrieval(indexDir).retrievalForQuerySet();
	}
}
