package au.csiro.gin.retrieval.weighting;

import org.terrier.matching.models.WeightingModelLibrary;

import au.csiro.gin.knowledgebase.InfoUnit;
import au.csiro.gin.retrieval.GINQuery;

public class BM25WeightingModel extends WeightingModel {

	public static double k1 = 1.2d;
	public static double k3 = 8d;
	public static double b = 0.75d;

	public BM25WeightingModel(double k1, double k3, double b) {
		BM25WeightingModel.k1 = k1;
		BM25WeightingModel.k3 = k3;
		BM25WeightingModel.b = b;
	}

	@Override
	public double scoreDocument(GINQuery query, InfoUnit iu, int docId, int docLength, int termFreq, int docFreq,
			int termCollectionFreq, int numDocuments, double avgDocLength, double df) {
		//double numerator = (termFreq * (k1 + 1));
		//double denominator = termFreq + k1 * (1 - b + b * (docLength / avgDocLength));
		// return idf(numDocuments, docFreq) * (numerator / denominator);

		double termFreqQuery = 1; // term freq in the query.
		double K = k1 * ((1 - b) + b * docLength / avgDocLength) + termFreq;
		return (termFreq * (k3 + 1d) * termFreqQuery / ((k3 + termFreqQuery) * K))
				* WeightingModelLibrary.log((numDocuments - docFreq + 0.5d) / (docFreq + 0.5d));
	}

	public String name() {
		return "BM25(k1=" + k1 + ",k3=" + k3 + ",b=" + b + ")";
	}

}
