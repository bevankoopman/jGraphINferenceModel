package au.csiro.gin.retrieval.weighting;

import au.csiro.gin.knowledgebase.InfoUnit;
import au.csiro.gin.retrieval.GINQuery;

public class LemurTFIDFWeightingModel extends WeightingModel {
	
	public static double k1 	= 1.2;
	public static double b 	= 0.75;

	public LemurTFIDFWeightingModel(double k1, double b) {
		LemurTFIDFWeightingModel.k1 = k1;
		LemurTFIDFWeightingModel.b = b;
	}

	@Override
	/**
	 * Score according to Indri/lemur tfidf.
	 * 
	 * 				(IDF * (K1 + 1)) * occurrences
	 * score = ------------------------------------------------------------------------
	 * 			occurrences + (K1 * (1-B)) + (K1 * B * 1/avgDocLength) * documentLength
	 * 
	 * @param query - the over-arching query to which this InfoUnit belongs.
	 * @param iu - InfoUnit being scored.
	 * @param docId - Terrier document id.
	 * @param docLength - length of the documents in number of terms / InfoUnits.
	 * @param termFreq - number of occurrences of iu in the document.
	 * @param docFreq - number of documents that iu appears in.
	 * @param termCollectionFreq - number of occurrences of iu in the collection.
	 * @param numDocuments - number of documents in the collection.
	 * @param avgDocLength - avg doc length across the collection.
	 * @param df - diffusion factor.
	 */
	public double scoreDocument(GINQuery query, InfoUnit iu, int docId, int docLength, int termFreq, int docFreq,
			int termCollectionFreq, int numDocuments, double avgDocLength, double df) {

		double numerator = (idf(numDocuments, docFreq) * (k1 + 1)) * termFreq;
		double demoninator = termFreq + (k1 * (1 - b)) + (k1 * b * (1 / avgDocLength)) * docLength;

		return numerator / demoninator;
	}

	public String name() {
		return "LemurTFIDF";
	}
	
	

}
