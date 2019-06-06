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

public class InfoRelationship {

	public static final double DF_NOT_SET = -1.0;

	private InfoUnit source;
	private InfoUnit target;
	private RelationshipType type;
	private double diffusionFactor = DF_NOT_SET;

	public InfoRelationship(InfoUnit source, RelationshipType type, InfoUnit target) {
		this.source = source;
		this.target = target;
		if(type == null) {
			type = RelationshipType.get("");
		}
		this.type = type;
	}


	public InfoUnit source() {
		return source;
	}

	public InfoUnit target() {
		return target;
	}

	public RelationshipType type() {
		return type;
	}

	public double diffusionFactor() {
		return diffusionFactor;
	}
	
	public void setDiffusionFactor(double df) {
		this.diffusionFactor = df;
	}
	
	public String toString() {
		return source + " --[" + type +"]--> " + target;
	}

	@SuppressWarnings("unused")
	private InfoRelationship() { // disable empty constructor
	}

	
	
	
}
