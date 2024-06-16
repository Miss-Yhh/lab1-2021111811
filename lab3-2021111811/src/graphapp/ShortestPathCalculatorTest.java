package graphapp;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

public class ShortestPathCalculatorTest {
    private Graph<String, DefaultWeightedEdge> graph;
    private ShortestPathCalculator calculator;

    @Before
    public void setUp() {
        graph = new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
        graph.addVertex("to");
        graph.addVertex("explore");
        graph.addVertex("new");
        graph.addVertex("seek");
        graph.addVertex("strange");
        graph.addVertex("worlds");
        graph.addVertex("out");
        graph.addVertex("life");
        graph.addVertex("civilizations");
        graph.addVertex("and");

        graph.addEdge("to", "explore", new DefaultWeightedEdge());
        graph.addEdge("to", "new", new DefaultWeightedEdge());
        graph.addEdge("to", "seek", new DefaultWeightedEdge());
        graph.addEdge("explore", "strange", new DefaultWeightedEdge());
        graph.addEdge("strange", "new", new DefaultWeightedEdge());
        graph.addEdge("new", "worlds", new DefaultWeightedEdge());
        graph.addEdge("new", "out", new DefaultWeightedEdge());
        graph.addEdge("new", "life", new DefaultWeightedEdge());
        graph.addEdge("new", "civilizations", new DefaultWeightedEdge());
        graph.addEdge("worlds", "seek", new DefaultWeightedEdge());
        graph.addEdge("seek", "to", new DefaultWeightedEdge());
        graph.addEdge("seek", "out", new DefaultWeightedEdge());
        graph.addEdge("out", "to", new DefaultWeightedEdge());
        graph.addEdge("out", "new", new DefaultWeightedEdge());
        graph.addEdge("life", "and", new DefaultWeightedEdge());
        graph.addEdge("and", "new", new DefaultWeightedEdge());

        calculator = new ShortestPathCalculator(graph);
    }
    @Test
    public void testInvalidPath1() {
        String result = calculator.calcShortestPath("to", "xyz");
        assertEquals("No path between to and xyz!", result);
    }

    @Test
    public void testPath1() {
        String result = calculator.calcShortestPath("to", "civilizations");
        assertEquals("Shortest path: to -> new -> civilizations", result);
    }


    @Test
    public void testPath2() {
        String result = calculator.calcShortestPath("to", "new");
        assertEquals("Shortest path: to -> new", result);
    }

    @Test
    public void testPath3() {
        String result = calculator.calcShortestPath("to", "out");
        assertEquals("Shortest path: to -> seek -> out", result);
    }

    @Test
    public void testPath4() {
        String result = calculator.calcShortestPath("civilizations", "explore");
        assertEquals("No path between civilizations and explore!", result);
    }



}
