package au.csiro.gin.retrieval;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class QueryPreprocessorTest {

	@Test
	public void test() throws IOException {
		final String HEARING_LOSS_CONCEPT = "155254002";
		
		QueryPreprocessor qp = new QueryPreprocessor();
		GINQuery query = qp.processQuery("hearing loss");
		assertEquals(HEARING_LOSS_CONCEPT, query.getQueryText());
	}

}
