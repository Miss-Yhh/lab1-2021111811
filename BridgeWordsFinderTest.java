package graphapp;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BridgeWordsFinderTest {
    private Graph<String, DefaultWeightedEdge> graph;
    private BridgeWordsFinder bridgeWordsFinder;

    @Before
    public void setUp() {
        graph = new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
        bridgeWordsFinder = new BridgeWordsFinder();
    }

    // 有效等价类 1：两个单词都在图中
    @Test
    public void testValidClass1() {
        graph.addVertex("word1");
        graph.addVertex("word2");
        String result = BridgeWordsFinder.queryBridgeWords(graph, "word1", "word2");
        assertEquals("No bridge words from \"word1\" to \"word2\"!", result);
    }

    // 有效等价类 2：word1 和 word2 都在图中，并且有桥接词
    @Test
    public void testValidClass2() {
        graph.addVertex("word1");
        graph.addVertex("word2");
        graph.addVertex("bridge1");
        graph.addEdge("word1", "bridge1");
        graph.addEdge("bridge1", "word2");

        String result = BridgeWordsFinder.queryBridgeWords(graph, "word1", "word2");
        assertEquals("The bridge words from \"word1\" to \"word2\" are: bridge1", result);
    }

    // 有效等价类 3：word1 和 word2 都在图中，并且存在多个桥接词
    @Test
    public void testValidClass3() {
        graph.addVertex("word1");
        graph.addVertex("word2");
        graph.addVertex("bridge1");
        graph.addVertex("bridge2");
        graph.addEdge("word1", "bridge1");
        graph.addEdge("bridge1", "word2");
        graph.addEdge("word1", "bridge2");
        graph.addEdge("bridge2", "word2");

        String result = BridgeWordsFinder.queryBridgeWords(graph, "word1", "word2");
        assertTrue(result.startsWith("There are two bridge words between \"word1\" and \"word2\". Randomly selected one is: "));
        assertTrue(result.contains("bridge1") || result.contains("bridge2"));
    }

    // 有效等价类 4：word1 和 word2 都在图中，并且不存在桥接词
    @Test
    public void testValidClass4() {
        graph.addVertex("word1");
        graph.addVertex("word2");

        String result = BridgeWordsFinder.queryBridgeWords(graph, "word1", "word2");
        assertEquals("No bridge words from \"word1\" to \"word2\"!", result);
    }

    // 无效等价类 5：word1 为空或 word2 为空或两者都为空
    @Test
    public void testInvalidClass5() {
        graph.addVertex("word1");
        graph.addVertex("word2");

        String result = BridgeWordsFinder.queryBridgeWords(graph, "", "word2");
        assertEquals("Please enter two words!", result);

        result = BridgeWordsFinder.queryBridgeWords(graph, "word1", "");
        assertEquals("Please enter two words!", result);

        result = BridgeWordsFinder.queryBridgeWords(graph, "", "");
        assertEquals("Please enter two words!", result);
    }

    // 无效等价类 6：word1 不在 G 中或者 word2 不在 G 中，或者都不在 G 中
    @Test
    public void testInvalidClass6() {
        graph.addVertex("word1");

        String result = BridgeWordsFinder.queryBridgeWords(graph, "word1", "word2");
        assertEquals("No \"word2\" in the graph!", result);

        result = BridgeWordsFinder.queryBridgeWords(graph, "word3", "word2");
        assertEquals("No \"word3\" and \"word2\" in the graph!", result);

        result = BridgeWordsFinder.queryBridgeWords(graph, "word3", "word1");
        assertEquals("No \"word3\" in the graph!", result);
    }

    // 无效等价类 7：word1 和 word2 都在图中，并且不存在桥接词
    @Test
    public void testInvalidClass7() {
        graph.addVertex("word1");
        graph.addVertex("word2");

        String result = BridgeWordsFinder.queryBridgeWords(graph, "word1", "word2");
        assertEquals("No bridge words from \"word1\" to \"word2\"!", result);
    }
}
