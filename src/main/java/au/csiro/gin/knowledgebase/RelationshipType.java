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
import java.util.Map;

public class RelationshipType {

	private double weight;
	private String name = "";

	public static final String RELATIONSHIP_TYPE_LABEL = "relationshipType";

	private static Map<String, RelationshipType> types = new HashMap<String, RelationshipType>();

	private RelationshipType(String name) {
		this.name = name == null ? "" : name;
	}

	public static RelationshipType get(String name) {
		if (!types.containsKey(name)) {
			types.put(name, new RelationshipType(name));
		}
		return types.get(name);
	}

	public double weight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String name() {
		return name;
	}

	public String toString() {
		return name();
	}

}
