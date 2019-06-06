/*
 * GIN - Graph INference Model 
 * CSIRO - Australia e-Health Research Centre.
 * 
 * Copyright 2015 CSIRO Australian e-Health Research Centre.
 * All rights reserved. Use is subject to license terms and conditions.
 * 
 * Contributor(s): Bevan Koopman <bevan.koopman{a.}csiro.au>
 */
package au.csiro.gin.retrieval;

import java.util.ArrayList;
import java.util.List;

import au.csiro.gin.knowledgebase.InfoUnit;

/**
 * Represents a single query, comprised of a number of InfoUnits.
 *
 * @author BevanKoopman
 */
public class GINQuery {

	private int id;
	private String queryText;


	private List<InfoUnit> informationUnits;
	
	public GINQuery(int id, String keywords) {
		this.id = id;
		this.queryText = keywords;
		informationUnits = new ArrayList<InfoUnit>();
		for(String keyword : keywords.trim().split(" ")) {
			informationUnits.add(new InfoUnit(keyword));
		}
	}
	
	/**
	 * Get the query id.
	 * 
	 * @return query id.
	 */
	public int id() {
		return id;
	}

	/**
	 * Get the individual InfoUnits making up this query.
	 * 
	 * @return list of InfoUnits.
	 */
	public List<InfoUnit> getInfoUnits() {
		return informationUnits;
	}
	
	/**
	 * String representing all the query keywords.
	 * 
	 * @return string of query keywords.
	 */
	public String getQueryText() {
		return queryText;
	}
	
	public String toString() {
		return id + ": " + queryText;
	}
}
