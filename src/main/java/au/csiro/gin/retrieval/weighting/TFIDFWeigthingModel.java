package au.csiro.gin.retrieval.weighting;

import au.csiro.gin.knowledgebase.InfoUnit;
import au.csiro.gin.retrieval.GINQuery;

public class TFIDFWeigthingModel extends WeightingModel {

	/**
	 * Score according to TFIDF.
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
		return termFreq * idf(numDocuments, docFreq) * df;
	}

	public String name() {
		return "TFIDF";
	}
}
