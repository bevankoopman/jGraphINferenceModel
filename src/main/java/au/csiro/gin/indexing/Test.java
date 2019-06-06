package au.csiro.gin.indexing;

import java.io.IOException;
import java.text.DecimalFormat;

import org.graphstream.graph.Edge;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.stream.file.FileSinkGML;

public class Test {

	public static void main(String[] args) throws IOException, GraphParseException {
		testCounter();
	}

	public static void smallGraph() throws IOException, GraphParseException {
		SingleGraph g = new SingleGraph("G", false, false);

		g.addNode("A");
		g.addNode("B");

		Edge e = g.addEdge("AB", "A", "B");

		e.addAttribute("df", 1.0);

		g.write(new FileSinkGML(), "/Users/koo01a/tmp/g.gml");

		SingleGraph otherG = new SingleGraph("oG");
		otherG.read("/Users/koo01a/tmp/g.gml");

		System.out.println(otherG.getEdge("AB").getAttribute("df").toString());

	}

	public static void testCounter() {

		int totalNodes = 300000;
		DecimalFormat format = new DecimalFormat("#.##");

		for (int nodeProgressCounter = 0; nodeProgressCounter < totalNodes; nodeProgressCounter++) {
			if (nodeProgressCounter % (totalNodes / 1000) == 0) {
				System.out.println(nodeProgressCounter+" Edge creation progress: " + format.format((nodeProgressCounter / (double)totalNodes) * 100) + "%");
			}

		}

	}
}
