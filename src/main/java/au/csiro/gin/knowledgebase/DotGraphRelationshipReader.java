/*
 * GIN - Graph INference Model 
 * CSIRO - Australia e-Health Research Centre.
 * 
 * Copyright 2015 CSIRO Australian e-Health Research Centre.
 * All rights reserved. Use is subject to license terms and conditions.
 * 
 * Contributor(s): Bevan Koopman <bevan.koopman{a.}csiro.au>
 */
package au.csiro.gin.knowledgebase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;
import org.terrier.structures.Index;

import au.csiro.gin.retrieval.weighting.DiffusionFactor;

/**
 * Relationships taken from a DOT graph.
 *
 */
public class DotGraphRelationshipReader extends RelationshipReader {

	private Logger log = Logger.getLogger(DotGraphRelationshipReader.class);

	private SingleGraph graph;
	private DiffusionFactor dfCalculator;

	public DotGraphRelationshipReader(File graphFile, Index index) throws IOException, GraphParseException {
		graph = new SingleGraph("GIN");
		log.info("Loading graph: " + graphFile + " ...");
		graph.read(graphFile.getAbsolutePath());
		dfCalculator = new DiffusionFactor(index);
		log.info("Loading graph complete.");
	}

	protected List<InfoRelationship> getRelatedInfoUnitsImpl(InfoUnit targetInformationUnit) {
		List<InfoRelationship> relationships = new ArrayList<InfoRelationship>();

		Node targetNode = graph.getNode(targetInformationUnit.name());
		for (Edge inEdge : targetNode.getEachEnteringEdge()) {
			InfoUnit sourceIU = new InfoUnit(inEdge.getNode0().getId());
			RelationshipType type = RelationshipType.get((String) inEdge.getAttribute("relationshipType"));
			InfoRelationship infoRel = new InfoRelationship(sourceIU, type, targetInformationUnit);
			try {
				infoRel.setDiffusionFactor(dfCalculator.df(sourceIU.name(), targetInformationUnit.name()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			relationships.add(infoRel);
		}

		return relationships;
	}
}
