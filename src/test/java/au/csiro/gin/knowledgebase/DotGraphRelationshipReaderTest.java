package au.csiro.gin.knowledgebase;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.graphstream.stream.GraphParseException;
import org.junit.Test;

public class DotGraphRelationshipReaderTest {

	@Test
	public void testGetRelatedInformationUnits() throws IOException, GraphParseException {
		InfoUnit concept = new InfoUnit("69322001");
		File graphFile = new File("target/test-classes/test.dot");
		List<InfoRelationship> results = new DotGraphRelationshipReader(graphFile, null).getRelatedInfoUnits(concept);
		for (InfoRelationship row: results) {
			System.out.println(concept+"\t"+row.type()+"\t"+row.source());
		}
		assertEquals(25, results.size());
	}

}
