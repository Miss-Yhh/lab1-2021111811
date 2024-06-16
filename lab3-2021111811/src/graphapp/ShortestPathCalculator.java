package graphapp;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ShortestPathCalculator {
    private Graph<String, DefaultWeightedEdge> graph;

    public ShortestPathCalculator(Graph<String, DefaultWeightedEdge> graph) {
        this.graph = graph;
    }

    public String calcShortestPath(String word1, String word2) {
        if (!graph.containsVertex(word1) || !graph.containsVertex(word2)) { // 如果有向图中不包含起始单词或目标单词，则返回无路径信息
            return "No path between " + word1 + " and " + word2 + "!";
        }

        Map<String, Double> distances = new HashMap<>(); // 存储节点到起始节点的距离
        Map<String, String> previous = new HashMap<>(); // 存储节点的前一个节点
        Set<String> visited = new HashSet<>(); // 存储已访问过的节点

        PriorityQueue<String> nodes = new PriorityQueue<>(Comparator.comparingDouble(distances::get)); // 优先队列用于选择距离最近的节点
        for (String vertex : graph.vertexSet()) {
            distances.put(vertex, Double.MAX_VALUE);
        }
        distances.put(word1, 0.0); // 将起始节点的距离设置为0
        nodes.add(word1); // 将起始节点加入优先队列

        while (!nodes.isEmpty()) {
            String closest = nodes.poll(); // 获取距离最近的节点
            if (closest.equals(word2)) { // 如果当前节点是目标节点，则跳出循环
                break;
            }
            visited.add(closest); // 将当前节点标记为已访问

            // 获取当前节点的邻居节点及其距离
            for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(closest)) {
                String neighbor = graph.getEdgeTarget(edge); // 获取邻居节点
                if (!visited.contains(neighbor)) { // 如果邻居节点未访问过
                    double alt = distances.get(closest) + graph.getEdgeWeight(edge); // 计算从起始节点到邻居节点的距离
                    if (alt < distances.get(neighbor)) { // 如果新的距离小于已知的距离
                        distances.put(neighbor, alt); // 更新距离
                        previous.put(neighbor, closest); // 更新前一个节点
                        nodes.add(neighbor); // 将邻居节点加入优先队列
                    }
                }
            }
        }

        List<String> path = new ArrayList<>(); // 存储最短路径节点
        for (String at = word2; at != null; at = previous.get(at)) { // 根据前一个节点反向遍历构建路径
            path.add(at);
        }
        Collections.reverse(path); // 反转路径，使其按起始节点到目标节点的顺序排列
        if (path.size() == 1 && !path.contains(word1)) { // 如果路径长度为1且不包含起始节点，说明无法到达目标节点
            return "No path between " + word1 + " and " + word2 + "!";
        }
        return "Shortest path: " + String.join(" -> ", path); // 返回最短路径字符串
    }

    public static void main(String[] args) {
        Graph<String, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
        String filePath = "C:\\Users\\Miss.Yu\\Desktop\\lab1\\graph.txt";
        Map<String, Map<String, Integer>> graphData = parseGraphFile(filePath);

        if (graphData == null) {
            System.err.println("Error parsing graph file.");
            return;
        }

        buildGraph(graph, graphData);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter word1: ");
        String word1 = scanner.nextLine().trim();
        System.out.println("Enter word2 (or leave blank to find paths from word1 to all other words): ");
        String word2 = scanner.nextLine().trim();
        scanner.close();

        ShortestPathCalculator calculator = new ShortestPathCalculator(graph);
        String result = calculator.calcShortestPath(word1, word2);
        System.out.println(result);
    }

    private static Map<String, Map<String, Integer>> parseGraphFile(String filePath) {
        Map<String, Map<String, Integer>> graphData = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            String currentNode = null;

            for (String line : lines) {
                if (line.startsWith("Node ")) {
                    currentNode = line.substring(5, line.indexOf(" has edges:")).trim();
                    graphData.putIfAbsent(currentNode, new HashMap<>());
                } else if (line.startsWith("  to ")) {
                    String[] parts = line.split(" with weight ");
                    String targetNode = parts[0].substring(5).trim();
                    int weight = (int) Double.parseDouble(parts[1].trim());
                    graphData.get(currentNode).put(targetNode, weight);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return graphData;
    }

    private static void buildGraph(Graph<String, DefaultWeightedEdge> graph, Map<String, Map<String, Integer>> graphData) {
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
}
