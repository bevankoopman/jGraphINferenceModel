package au.csiro.gin.retrieval;

import java.io.PrintWriter;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.utility.ApplicationSetup;

public class Test {

	// vars
	protected Index index;
	protected static Logger logger = Logger.getRootLogger();
	protected String managerName = ApplicationSetup.getProperty("interactive.manager", "Manager");
	protected Manager queryingManager;
	protected PrintWriter resultFile = new PrintWriter(System.out);
	protected boolean verbose = true;
	protected static int RESULTS_LENGTH = Integer.parseInt(ApplicationSetup.getProperty(
			"interactive.output.format.length", "1000"));

	// constructor
	public Test() {

		// loadIndex();
		// createManager();
		// runTestQuery();

		double[] scores = new double[10];
		Arrays.fill(scores, 0.0d);

		traverse(scores, 1.0);

		traverse(scores, 5.0);
		
		traverse(scores, 1.0);
		
		for (int i = 0; i < scores.length; i++) {
			System.out.println(i + ": " + scores[i]);
		}
	}

	private void traverse(double[] scores, double v) {
		scores[(int)v] += v;
	}

	void runTestQuery() {
		String query = "osteoporosis";
		SearchRequest srq = queryingManager.newSearchRequest("queryID0", query);
		srq.addMatchingModel("Matching", "PL2");
		queryingManager.runPreProcessing(srq);
		queryingManager.runMatching(srq);
		queryingManager.runPostProcessing(srq);
		queryingManager.runPostFilters(srq);
		// print results
		printResults(resultFile, srq);
	}

	public void printResults(PrintWriter pw, SearchRequest q) {
		ResultSet set = q.getResultSet();
		DocumentIndex docIndex = index.getDocumentIndex();
		int[] docids = set.getDocids();
		double[] scores = set.getScores();
		int minimum = RESULTS_LENGTH;
		// if the minimum number of documents is more than the
		// number of documents in the results, aw.length, then
		// set minimum = aw.length
		if (minimum > set.getResultSize())
			minimum = set.getResultSize();
		if (verbose)
			if (set.getResultSize() > 0)
				pw.write("\n\tDisplaying 1-" + set.getResultSize() + " results\n");
			else
				pw.write("\n\tNo results\n");
		StringBuilder sbuffer = new StringBuilder();
		// the results are ordered in asceding order
		// with respect to the score. For example, the
		// document with the highest score has score
		// score[scores.length-1] and its docid is
		// docid[docids.length-1].
		int start = 0;
		int end = minimum;
		for (int i = start; i < end; i++) {
			sbuffer.append(i);
			sbuffer.append(" ");
			// sbuffer.append(docids);
			// sbuffer.append(docIndex. .getDocumentNumber(docids));
			sbuffer.append(" ");
			sbuffer.append(docids);
			sbuffer.append(" ");
			sbuffer.append(scores);
			sbuffer.append('\n');
		}
		// System.out.println(sbuffer.toString());
		pw.write(sbuffer.toString());
		pw.flush();
		// pw.write("finished outputting\n");
	}

	protected void loadIndex() {
		long startLoading = System.currentTimeMillis();
		index = Index.createIndex();
		if (index == null) {
			logger.fatal("Failed to load index. Perhaps index files are missing");
		}
		long endLoading = System.currentTimeMillis();
		if (logger.isInfoEnabled())
			logger.info("time to intialise index : " + ((endLoading - startLoading) / 1000.0D));
	}

	protected void createManager() {
		try {
			if (managerName.indexOf('.') == -1)
				managerName = "uk.ac.gla.terrier.querying." + managerName;
			queryingManager = (Manager) (Class.forName(managerName).getConstructor(new Class[] { Index.class })
					.newInstance(new Object[] { index }));
		} catch (Exception e) {
			logger.error("Problem loading Manager (" + managerName + "): ", e);
		}
	}

	public static void main(String[] args) {
		System.setProperty("terrier.home", "/Users/koo01a/tools/terrier-4.0");
		String home = System.getProperty("terrier.home");
		new Test();
	}

}