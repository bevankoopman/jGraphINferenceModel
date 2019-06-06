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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import au.csiro.gin.Configuration;

/**
 * Relationships taken from a SQLite DB.
 *
 */
public class SQLRelationshipReader extends RelationshipReader {

	private static Logger log = Logger.getLogger(SQLRelationshipReader.class.getName());

	private Statement stmt = null;

	public SQLRelationshipReader() {
		this(Configuration.get("relation.db.file"));
	}

	public SQLRelationshipReader(String dbFile) {
		setupConnection(dbFile);

	}

	protected List<InfoRelationship> getRelatedInfoUnitsImpl(InfoUnit targetInformationUnit) {
		List<InfoRelationship> relatedInformationUnits = new ArrayList<InfoRelationship>();
		ResultSet rs;
		try {
			String sql = "SELECT cui1, reltype FROM crel where cui2 = '" + targetInformationUnit.name()
					+ "' AND (relcharacteristic=0 or relcharacteristic=3);";

			sql = "SELECT s_cui, predicate FROM RELATIONSHIPS where o_cui = '"
					+ targetInformationUnit.name().toUpperCase() + "' and s_cui like 'C%' LIMIT "
					+ RelationshipReader.MAX_RELATIONSHIP + ";";

			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				// InfoUnit source = new InfoUnit(rs.getString("cui1"));
				// RelationshipType reltype = RelationshipType.get(rs.getString("reltype"));
				InfoUnit source = new InfoUnit(rs.getString("s_cui").toLowerCase());
				RelationshipType reltype = RelationshipType.get(rs.getString("predicate"));
				relatedInformationUnits.add(new InfoRelationship(source, reltype, targetInformationUnit));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return relatedInformationUnits;
	}

	private void setupConnection(String dbFile) {
		Connection conn = null;

		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
			conn.setAutoCommit(false);

			stmt = conn.createStatement();

			log.info("Successfully connected to SQL db of relationships");

		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

}