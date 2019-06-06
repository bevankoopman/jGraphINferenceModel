package au.csiro.gin.retrieval.weighting;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import au.csiro.gin.knowledgebase.InfoUnit;
import au.csiro.gin.retrieval.GINQuery;

public class LemurTFIDFWeightingModelTest {

	@Test
	public void testScoreDocument() {
		GINQuery query = new GINQuery(1, "foo");
		InfoUnit iu = new InfoUnit("foo");
		int docId = 1;
		int docLength = 1;
		int termFreq = 1;
		int docFreq = 2;
		int termCollectionFreq = 2;
		int numDocuments = 3;
		double avgDocLength = 4 / 3.0;
		double df = 1;
		double score = new LemurTFIDFWeightingModel(5.2, 0.4).scoreDocument(query, iu, docId, docLength, termFreq, docFreq,
				termCollectionFreq, numDocuments, avgDocLength, df);
		assertEquals(0.44258515321665837, score, 0.001);
	}

}
