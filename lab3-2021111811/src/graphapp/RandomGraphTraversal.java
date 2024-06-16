package graphapp;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class RandomGraphTraversal {
    private static Graph<String, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<>(
            DefaultWeightedEdge.class);

    public static void main(String[] args) {
        String filePath = "C:\\Users\\Miss.Yu\\Desktop\\lab1-2021111811\\graph.txt"; // 请将此路径改为你的文本文件路径
        Map<String, Map<String, Integer>> graphData = parseGraphFile(filePath);

        if (graphData == null) {
            System.err.println("Error parsing graph file.");
            return;
        }

        buildGraph(graphData);

        Random random = new Random();
        List<String> vertices = new ArrayList<>(graph.vertexSet());
        if (vertices.isEmpty()) {
            System.out.println("The graph is empty.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        String currentNode = vertices.get(random.nextInt(vertices.size()));
        Set<DefaultWeightedEdge> visitedEdges = new HashSet<>();
        List<String> traversalPath = new ArrayList<>();
        traversalPath.add(currentNode);

        System.out.println("Starting random traversal from node: " + currentNode);

        while (true) {
            Set<DefaultWeightedEdge> outgoingEdges = graph.outgoingEdgesOf(currentNode);
            if (outgoingEdges.isEmpty()) {
                System.out.println("Reached a node with no outgoing edges.");
                break;
            }

            List<DefaultWeightedEdge> edges = new ArrayList<>(outgoingEdges);
            DefaultWeightedEdge nextEdge = edges.get(random.nextInt(edges.size()));
            if (visitedEdges.contains(nextEdge)) {
                System.out.println("Encountered a previously visited edge. Stopping traversal.");
                break;
            }

            visitedEdges.add(nextEdge);
            currentNode = graph.getEdgeTarget(nextEdge);
            traversalPath.add(currentNode);

            System.out.println("Traversed to node: " + currentNode);
            System.out.println("Press 'q' to stop traversal, any other key to continue.");
            String input = scanner.nextLine().trim();
            if ("q".equalsIgnoreCase(input)) {
                System.out.println("Traversal stopped by user.");
                break;
            }
        }

        scanner.close();

        saveTraversalPath(traversalPath, "traversal_path.txt");
    }

    private static Map<String, Map<String, Integer>> parseGraphFile(String filePath) {
        Map<String, Map<String, Integer>> graph = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            String currentNode = null;

            for (String line : lines) {
                if (line.startsWith("Node ")) {
                    currentNode = line.substring(5, line.indexOf(" has edges:")).trim();
                    graph.putIfAbsent(currentNode, new HashMap<>());
                } else if (line.startsWith("  to ")) {
                    String[] parts = line.split(" with weight ");
                    String targetNode = parts[0].substring(5).trim();
                    int weight = (int) Double.parseDouble(parts[1].trim());
                    graph.get(currentNode).put(targetNode, weight);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return graph;
    }

    private static void buildGraph(Map<String, Map<String, Integer>> graphData) {
        for (String from : graphData.keySet()) {
            graph.addVertex(from);
            for (String to : graphData.get(from).keySet()) {
                graph.addVertex(to);
                DefaultWeightedEdge edge = graph.getEdge(from, to);
                if (edge == null) {
                    edge = graph.addEdge(from, to);
                    graph.setEdgeWeight(edge, graphData.get(from).get(to));
                }
            }
        }
    }

    private static void saveTraversalPath(List<String> path, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String node : path) {
                writer.write(node);
                writer.write(" ");
            }
            System.out.println("Traversal path saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing traversal path to file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
