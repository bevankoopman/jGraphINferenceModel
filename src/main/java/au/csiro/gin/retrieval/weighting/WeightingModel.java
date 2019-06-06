package au.csiro.gin.retrieval.weighting;

import au.csiro.gin.knowledgebase.InfoUnit;
import au.csiro.gin.retrieval.GINQuery;

/**
 * Representing different retrieval model weighting methods, e.g., BM25.
 * 
 * @author BevanKoopman
 */
public abstract class WeightingModel {

	/**
	 * Score a single document with the current InfoUnit.
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
	public abstract double scoreDocument(GINQuery query, InfoUnit iu, int docId, int docLength, int termFreq, int docFreq,
			int termCollectionFreq, int numDocuments, double avgDocLength, double df);

	/**
	 * Calculate inverse document frequency (IDF).
	 * 
	 * @param numDocuments - number of documents in the collection.
	 * @param docFreq - number of documents that iu appears in. 
	 * @return idf = log (numDocuments / (double)docFreq ).
	 */
	public double idf(int numDocuments, int docFreq) {
		return Math.log(numDocuments / (double) docFreq);
	}
	

	/**
	 * Print the Id/Name of the weighting model.
	 */
	public String name() {
		return this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1);
	}
	
	

}
