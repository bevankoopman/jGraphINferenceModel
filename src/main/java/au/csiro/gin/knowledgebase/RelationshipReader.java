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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.csiro.gin.Configuration;

/**
 * Reads domain knowledge resource relationships from some resource, e.g., an ontology.
 * 
 * @author Bevan Koooman
 *
 */
public abstract class RelationshipReader {

	public static final int CACHE_LIMIT = 10 ^ 4;

	public static final int MAX_RELATIONSHIP = Integer.MAX_VALUE;

	private Map<InfoUnit, List<InfoRelationship>> cache = new HashMap<InfoUnit, List<InfoRelationship>>();

	public List<InfoRelationship> getRelatedInfoUnits(InfoUnit targetInformationUnit) {
		List<InfoRelationship> relationships;

		if (Configuration.useRelationshipCaching() && cache.size() < CACHE_LIMIT) {
			if (cache.containsKey(targetInformationUnit)) { // in the cache
				relationships = cache.get(targetInformationUnit);
			} else { // not in cache; add it
				relationships = getRelatedInfoUnitsImpl(targetInformationUnit);
				if (relationships.size() < MAX_RELATIONSHIP) {
					cache.put(targetInformationUnit, relationships);
				}
			}
		} else { // no cache
			relationships = getRelatedInfoUnitsImpl(targetInformationUnit);
		}

		return relationships;
	}

	protected abstract List<InfoRelationship> getRelatedInfoUnitsImpl(InfoUnit targetInformationUnit);

}
