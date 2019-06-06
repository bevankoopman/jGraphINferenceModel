package au.csiro.gin.service;

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.File;
import java.io.IOException;

import au.csiro.gin.Configuration;
import au.csiro.gin.retrieval.GINQuery;
import au.csiro.gin.retrieval.GINRetrieval;
import au.csiro.gin.retrieval.QueryPreprocessor;

public class GINWebService {

	public static void main(String[] args) throws IOException {

		QueryPreprocessor queryPreprocessor = new QueryPreprocessor();
		GINRetrieval ginRetrieval = new GINRetrieval(new File(Configuration.getIndexDir()));

		get("/hello", (req, res) -> "Hello World");
		
		post("/conceptsearch", (request, response) -> {
			GINQuery query = new GINQuery(0, request.body());
			int maxDepth = Configuration.getDepth();
			return ginRetrieval.retrievalForSingleQuery(query, maxDepth);
		});

		post("/termsearch", (request, response) -> {
			GINQuery query = queryPreprocessor.processQuery(request.body());
			int maxDepth = Configuration.getDepth();
			return ginRetrieval.retrievalForSingleQuery(query, maxDepth);
		});

	}
}
