package au.csiro.gin.retrieval.weighting;

import java.io.IOException;
import java.util.Arrays;

import org.terrier.structures.Index;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.IterablePosting;

public class DiffusionFactor {

	public static final String DIFFUSION_FACTOR_LABEL = "diffusionFactor";
	private Index index;

	public DiffusionFactor(Index index) {
		this.index = index;
	}

	public double df(String term1, String term2) throws IOException {
		return cosine(documentVector(term1), documentVector(term2));
	}

	private double cosine(double[] firstVector, double[] secondVector) {
		double dotProduct = 0.0;
		for (int i = 0; i < firstVector.length; i++) {
			dotProduct += firstVector[i] * secondVector[i];
		}
		return dotProduct / (norm(firstVector) * norm(secondVector));
	}

	private double norm(double[] vector) {
		double norm = 0.0;
		for (int i = 0; i < vector.length; i++) {
			norm += Math.pow(vector[i], 2);
		}
		return Math.sqrt(norm);
	}

	private double[] documentVector(String term) throws IOException {
		double[] vector = new double[index.getDocumentIndex().getNumberOfDocuments()];
		Arrays.fill(vector, 0.0);

		LexiconEntry lexEntry = index.getLexicon().getLexiconEntry(term);
		if (lexEntry != null) {
			IterablePosting postingList = index.getInvertedIndex().getPostings(lexEntry);
			while (!postingList.endOfPostings()) {
				int docId = postingList.next();
				vector[docId] = postingList.getFrequency()
						* Math.log(vector.length / (double) lexEntry.getDocumentFrequency());
			}
		}

		return vector;
	}
}
