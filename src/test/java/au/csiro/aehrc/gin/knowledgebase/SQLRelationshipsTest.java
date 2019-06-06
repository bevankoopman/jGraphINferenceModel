package au.csiro.aehrc.gin.knowledgebase;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import au.csiro.gin.knowledgebase.InfoRelationship;
import au.csiro.gin.knowledgebase.InfoUnit;
import au.csiro.gin.knowledgebase.SQLRelationshipReader;

public class SQLRelationshipsTest {

	
	public void atestGetRelatedInformationUnits() {
		InfoUnit concept = new InfoUnit("69322001");
		List<InfoRelationship> results = new SQLRelationshipReader().getRelatedInfoUnits(concept);
		for (InfoRelationship row : results) {
			System.out.println(concept + "\t" + row.type() + "\t" + row.source());
		}
		assertEquals(25, results.size());
	}

	
	public void atestGetRelatedInformationUnitsProblem() {
		// INFO - GINIndexer.java:createGraph: - Creating edge 262225004 --[123005000]--> 22943007 16.87%
		InfoUnit conceptTwo = new InfoUnit("22943007");
		List<InfoRelationship> resultsTwo = new SQLRelationshipReader().getRelatedInfoUnits(conceptTwo);
		for (InfoRelationship row : resultsTwo) {
			System.out.println(row.source() + " --[" + row.type().name() + "]--> " + conceptTwo);
		}
		assertEquals(201, resultsTwo.size(), 0.001);
	}

}
