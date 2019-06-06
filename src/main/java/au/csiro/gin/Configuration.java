package au.csiro.gin;

import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import au.csiro.gin.retrieval.GINRetrieval;

/**
 * Class for reading properties files and setting configuration values.
 * 
 */
public class Configuration {
	private static Logger log = Logger.getLogger(Configuration.class.getName());

	// default prop file
	private static final String PROPERTIES_FILE = "gin.properties";

	// where properties are stored
	private static Properties properties;

	static {
		loadProperties();
	}

	/**
	 * Read the PROPERTIES_FILE off the classpath.
	 */
	private static void loadProperties() {

		// load properties from classpath
		properties = new Properties();
		try {
			properties.load(GINRetrieval.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE));
			log.log(Level.INFO, "Loaded configuration values from " + PROPERTIES_FILE);
		} catch (Exception e) {
			log.fatal("Configuration file not found, please ensure " + "there is a '" + PROPERTIES_FILE
					+ "' on the classpath" + e);
		}

		// override any properties that were specified on the command line
		properties.putAll(System.getProperties());

		// Terrier required terrier.home to be set on system property
		System.setProperty("terrier.home", properties.getProperty("terrier.home"));
	}

	public static String getTerrierHome() {
		return properties.getProperty("terrier.home");
	}

	public static String getGraphLocation() {
		return properties.getProperty("neo4jdb.dir", getIndexDir() + "/neo4j/graph.db");

	}

	public static String getIndexDir() {
		return properties.getProperty("index.dir", get("terrier.home") + "/var/index");
	}

	public static String get(String key) {
		return properties.getProperty(key);
	}

	public static String get(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public static double getDiffusionMix() {
		return Double.parseDouble(properties.getProperty("diffusion.mix", "1.0"));
	}

	public static String getCorpusDir() {
		return properties.getProperty("corpus.dir", "corpus");
	}

	public static int getDepth() {
		return Integer.parseInt(properties.getProperty("depth", "0"));
	}

	public static int getResultsSize() {
		return Integer.parseInt(properties.getProperty("resultSize", "1000"));
	}

	public static boolean useRelationshipCaching() {
		return Boolean.parseBoolean(properties.getProperty("relationshipCaching", "true"));
	}

	public static String getQueryFile() {
		return properties.getProperty("query.file");
	}

	public static String getResultsDir() {
		return properties.getProperty("results.dir", "results");
	}

	public static boolean filterIndexingByQueries() {
		assert getQueryFile() != null;
		return Boolean.parseBoolean(properties.getProperty("filter.indexing.by.queries", "false"));
	}

}
