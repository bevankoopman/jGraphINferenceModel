package au.csiro.gin.retrieval;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import au.csiro.aehrc.conceptmapper.Concept;
import au.csiro.aehrc.conceptmapper.ConceptMapper;

/**
 * Create a GINQuery from a user's free-text entry. Currently this involves mapping the query to SNOMED concepts.
 *
 * @author BevanKoopman
 */
public class QueryPreprocessor {

	static Logger log = Logger.getLogger(QueryPreprocessor.class);

	private ConceptMapper conceptMapper;

	/**
	 * Public constructor.
	 */
	public QueryPreprocessor() {
		conceptMapper = new ConceptMapper();
	}

	public GINQuery processQuery(String keywords) throws IOException {
		return processQuery(0, keywords);
	}

	public GINQuery processQuery(int id, String keywords) throws IOException {
		StringBuffer sb = new StringBuffer(keywords.length());
		for (Concept concept : conceptMapper.mapToConcepts(keywords)) {
			sb.append(concept.conceptId);
			sb.append(" ");
		}

		GINQuery query = new GINQuery(id, sb.toString().trim());

		log.info("Query processed + '" + keywords + "' -> " + query);

		return query;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(new QueryPreprocessor().processQuery("155254002"));
	}

	/**
	 * Load the queries from a query file.
	 * 
	 * @param queryFile - TREC style query file.
	 * @return list of the queries
	 * @throws IOException - when the query file cannot be read.
	 * @throws JDOMException - when the query file cannot be parsed.
	 */
	public static List<GINQuery> loadQueries(String queryFile) throws IOException, JDOMException {
		List<GINQuery> queryList = new ArrayList<GINQuery>();

		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File(queryFile);
		try {
			Document document = (Document) builder.build(xmlFile);
			Element rootNode = document.getRootElement();
			List<Element> queryElementList = rootNode.getChildren("query");
			for (int i = 0; i < queryElementList.size(); i++) {
				Element queryElement = queryElementList.get(i);
				String queryId = queryElement.getChild("number").getText();
				String queryText = queryElement.getChild("text").getText();
				queryList.add(new GINQuery(Integer.parseInt(queryId), queryText));
			}
		} catch (IOException io) {
			log.fatal("Unable to read the query file", io);
			throw io;
		} catch (JDOMException jdomex) {
			log.fatal("Unable to parse the query file", jdomex);
			throw jdomex;
		}

		return queryList;
	}

}
