package au.csiro.gin.retrieval.weighting;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.terrier.structures.Index;

import au.csiro.gin.Configuration;

public class DiffusionFactorTest {
	
	private Index index;
	
	@Before
	public void setUp() throws Exception {
		Configuration.getTerrierHome();
		File indexDir = new File("target/test-classes/test-index");
		index = Index.createIndex(indexDir.getAbsolutePath(), "data");
		
	}


	@Test
	public void testDf() throws IOException {
		DiffusionFactor df = new DiffusionFactor(index);
		assertEquals(1.0, df.df("foo", "foo"), 0.001);
		assertEquals(1.0, df.df("bar", "bar"), 0.001);
		assertEquals(0.5, df.df("foo", "bar"), 0.001);
	}

}
