package graphapp;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class BridgeWordsFinder {

    // 定义一个静态变量graph，表示有向带权重的图，节点类型为String，边类型为DefaultWeightedEdge
    private static Graph<String, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<>(
            DefaultWeightedEdge.class);

    // main方法是程序的入口点
    public static void main(String[] args) {
        String filePath = "C:\\Users\\Miss.Yu\\Desktop\\lab1-2021111811\\graph.txt"; // 请将此路径改为你的文本文件路径
        Map<String, Map<String, Integer>> graphData = parseGraphFile(filePath);

        if (graphData == null) {
            System.err.println("Error parsing graph file.");
            return;
        }

        buildGraph(graphData);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter word1: ");
        String word1 = scanner.nextLine().trim();
        System.out.println("Enter word2: ");
        String word2 = scanner.nextLine().trim();

        String result = findBridgeWords(word1, word2);
        System.out.println(result);

        scanner.close();
    }

    // 读取文件内容并返回图数据
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

    // 根据图数据构建有向带权重的图
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

    // 查找桥接词
    private static String findBridgeWords(String word1, String word2) {
        boolean word1InGraph = graph.containsVertex(word1);
        boolean word2InGraph = graph.containsVertex(word2);

        if (!word1InGraph && !word2InGraph) {
            return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
        } else if (!word1InGraph) {
            return "No \"" + word1 + "\" in the graph!";
        } else if (!word2InGraph) {
            return "No \"" + word2 + "\" in the graph!";
        }

        Set<String> bridgeWords = new HashSet<>();
        for (DefaultWeightedEdge edge1 : graph.outgoingEdgesOf(word1)) {
            String potentialBridge = graph.getEdgeTarget(edge1);
            if (graph.containsEdge(potentialBridge, word2)) {
                bridgeWords.add(potentialBridge);
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        }

        StringBuilder result = new StringBuilder("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: ");
        result.append(String.join(", ", bridgeWords));
        return result.toString();
    }
}