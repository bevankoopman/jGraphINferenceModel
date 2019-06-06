package au.csiro.gin.retrieval;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GINRetrievalTest {

	private GINRetrieval ginRetrieval;

	@Before
	public void setUp() throws Exception {
		this.ginRetrieval = new GINRetrieval();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException {
		GINQuery query = new GINQuery(0, "15188001"); // 15188001 = hearing loss
		JSONArray results = this.ginRetrieval.retrievalForSingleQuery(query, 0);
		System.out.println(results.toJSONString());
		assertTrue(results.size() > 0);
	}

}
