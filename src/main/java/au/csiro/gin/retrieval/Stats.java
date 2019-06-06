package au.csiro.gin.retrieval;

import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;

public class Stats {

	
	public static void main(String[] args) {
		
		System.setProperty("terrier.home", "/Users/koo01a/tools/terrier-4.0");
		String home = System.getProperty("terrier.home");
		Index index = Index.createIndex();
		Lexicon<String> lex = index.getLexicon();
		LexiconEntry le = lex.getLexiconEntry("term");
		if (le != null)
			System.out.println("Term term occurs in "+ le.getDocumentFrequency() + " documents");
		else
			System.out.println("Term term does not occur");
	}
}
