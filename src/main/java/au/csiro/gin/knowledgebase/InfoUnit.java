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

/**
 * Class representing a single Information Unit - either term, entity or concept. InfoUnits are represented by a name.
 *
 * @author BevanKoopman
 */
public class InfoUnit {

	public static final String ID_LABEL = "IU_ID";
	private String name;

	public InfoUnit(String name) {
		this.name = name;
	}

	/**
	 * The name/id of the information unit. For concepts it would the conceptId; for terms it is the term itself.
	 * 
	 * @return - InfoUnit name.
	 */
	public String name() {
		return this.name;
	}

	public String toString() {
		return name();
	}

}
