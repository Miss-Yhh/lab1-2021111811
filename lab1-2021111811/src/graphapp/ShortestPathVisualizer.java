package graphapp;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class ShortestPathVisualizer {

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

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter word1: ");
        String word1 = scanner.nextLine().trim();
        System.out.println("Enter word2 (or leave blank to find paths from word1 to all other words): ");
        String word2 = scanner.nextLine().trim();

        if (word2.isEmpty()) {
            findAndDisplayAllShortestPaths(word1);
        } else {
            List<String> shortestPath = findShortestPath(word1, word2);
            if (shortestPath != null) {
                System.out.println("Shortest path: " + String.join(" -> ", shortestPath));
                System.out.println("Path length: " + calculatePathLength(shortestPath));
                showDirectedGraph(graph, "shortest_path_graph.png", shortestPath);
            } else {
                System.out.println("No path found from \"" + word1 + "\" to \"" + word2 + "\".");
            }
        }

        scanner.close();
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

    private static List<String> findShortestPath(String word1, String word2) {
        if (!graph.containsVertex(word1) || !graph.containsVertex(word2)) {
            return null;
        }

        DijkstraShortestPath<String, DefaultWeightedEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        GraphPath<String, DefaultWeightedEdge> path = dijkstraAlg.getPath(word1, word2);

        if (path == null) {
            return null;
        }

        return path.getVertexList();
    }

    private static double calculatePathLength(List<String> path) {
        double length = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            DefaultWeightedEdge edge = graph.getEdge(path.get(i), path.get(i + 1));
            length += graph.getEdgeWeight(edge);
        }
        return length;
    }

    private static void findAndDisplayAllShortestPaths(String word1) {
        if (!graph.containsVertex(word1)) {
            System.out.println("No \"" + word1 + "\" in the graph!");
            return;
        }

        DijkstraShortestPath<String, DefaultWeightedEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultWeightedEdge> paths = dijkstraAlg.getPaths(word1);

        for (String target : graph.vertexSet()) {
            if (!target.equals(word1)) {
                GraphPath<String, DefaultWeightedEdge> path = paths.getPath(target);
                if (path != null) {
                    List<String> vertexList = path.getVertexList();
                    System.out.println("Shortest path from \"" + word1 + "\" to \"" + target + "\": " +
                            String.join(" -> ", vertexList));
                    System.out.println("Path length: " + path.getWeight());
                    showDirectedGraph(graph, "shortest_path_graph_" + word1 + "_to_" + target + ".png", vertexList);
                } else {
                    System.out.println("No path found from \"" + word1 + "\" to \"" + target + "\".");
                }
            }
        }
    }

    public static void showDirectedGraph(Graph<String, DefaultWeightedEdge> graph, String filename,
            List<String> highlightPath) {
        int width = 2000;
        int height = 1000;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2));
        drawGraph(graph, g2d, width, height, highlightPath);

        try {
            ImageIO.write(image, "PNG", new File(filename));
            System.out.println("有向图已保存为 " + filename);
        } catch (IOException e) {
            System.err.println("Error saving graph as image: " + e.getMessage());
            e.printStackTrace();
        } finally {
            g2d.dispose();
        }
    }

    private static void drawGraph(Graph<String, DefaultWeightedEdge> graph, Graphics2D g2d, int width, int height,
            List<String> highlightPath) {
        int margin = 50;
        int nodeSize = 50;

        int nodeIndex = 0;
        int totalNodes = graph.vertexSet().size();
        Map<String, Point> nodePositions = new HashMap<>();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(centerX, centerY) - margin;

        for (String node : graph.vertexSet()) {
            double angle = 2 * Math.PI * nodeIndex / totalNodes;
            int x = centerX + (int) (radius * Math.cos(angle));
            int y = centerY + (int) (radius * Math.sin(angle));
            nodePositions.put(node, new Point(x, y));
            nodeIndex++;
        }

        for (String from : graph.vertexSet()) {
            Point startPoint = nodePositions.get(from);
            for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(from)) {
                String to = graph.getEdgeTarget(edge);
                Point endPoint = nodePositions.get(to);
                boolean highlight = highlightPath != null && highlightPath.contains(from) && highlightPath.contains(to);
                drawCurvedArrow(g2d, startPoint, endPoint, highlight, nodeSize / 2);
                drawWeight(g2d, startPoint, endPoint, (int) graph.getEdgeWeight(edge), nodeSize / 2);
            }
        }

        for (String node : graph.vertexSet()) {
            Point point = nodePositions.get(node);
            drawNode(g2d, point, nodeSize, node);
        }

        if (highlightPath != null) {
            double pathLength = calculatePathLength(highlightPath);
            g2d.setColor(Color.RED);
            g2d.drawString("Path length: " + pathLength, width - 200, height - 50);
        }
    }

    private static void drawNode(Graphics2D g2d, Point center, int size, String label) {
        g2d.setColor(Color.WHITE);
        g2d.fillOval(center.x - size / 2, center.y - size / 2, size, size);

        g2d.setColor(Color.BLACK);
        g2d.drawOval(center.x - size / 2, center.y - size / 2, size, size);

        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(label);
        int labelHeight = fm.getAscent();
        g2d.drawString(label, center.x - labelWidth / 2, center.y + labelHeight / 4);
    }

    private static void drawWeight(Graphics2D g2d, Point from, Point to, int weight, int radius) {
        Point fromEdge = getEdgePoint(from, to, radius);
        Point toEdge = getEdgePoint(to, from, radius);

        double ctrlX = (fromEdge.x + toEdge.x) / 2 + (toEdge.y - fromEdge.y) / 4;
        double ctrlY = (fromEdge.y + toEdge.y) / 2 - (toEdge.x - fromEdge.x) / 4;

        double t = 1.0 / 3.0;
        double x = Math.pow(1 - t, 2) * fromEdge.x + 2 * (1 - t) * t * ctrlX + Math.pow(t, 2) * toEdge.x;
        double y = Math.pow(1 - t, 2) * fromEdge.y + 2 * (1 - t) * t * ctrlY + Math.pow(t, 2) * toEdge.y;

        double dx = 10 * Math.cos(Math.atan2(toEdge.y - ctrlY, toEdge.x - ctrlX));
        double dy = 10 * Math.sin(Math.atan2(toEdge.y - ctrlY, toEdge.x - ctrlX));

        g2d.setColor(Color.BLACK);
        g2d.drawString(String.valueOf(weight), (int) (x + dx), (int) (y + dy));
    }

    private static void drawCurvedArrow(Graphics2D g2d, Point from, Point to, boolean highlight, int radius) {
        if (highlight) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));
        } else {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
        }

        Point fromEdge = getEdgePoint(from, to, radius);
        Point toEdge = getEdgePoint(to, from, radius);

        double ctrlX = (fromEdge.x + toEdge.x) / 2 + (toEdge.y - fromEdge.y) / 4;
        double ctrlY = (fromEdge.y + toEdge.y) / 2 - (toEdge.x - fromEdge.x) / 4;

        QuadCurve2D q = new QuadCurve2D.Double();
        q.setCurve(fromEdge.x, fromEdge.y, ctrlX, ctrlY, toEdge.x, toEdge.y);
        g2d.draw(q);

        // 绘制箭头
        double angle = Math.atan2(toEdge.y - ctrlY, toEdge.x - ctrlX);
        double arrowLength = 10;
        double arrowAngle = Math.PI / 6;

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(toEdge.x, toEdge.y);
        arrowHead.addPoint((int) (toEdge.x - arrowLength * Math.cos(angle - arrowAngle)),
                (int) (toEdge.y - arrowLength * Math.sin(angle - arrowAngle)));
        arrowHead.addPoint((int) (toEdge.x - arrowLength * Math.cos(angle + arrowAngle)),
                (int) (toEdge.y - arrowLength * Math.sin(angle + arrowAngle)));

        g2d.fill(arrowHead);
    }

    private static Point getEdgePoint(Point from, Point to, int radius) {
        double angle = Math.atan2(to.y - from.y, to.x - from.x);
        int x = (int) (from.x + radius * Math.cos(angle));
        int y = (int) (from.y + radius * Math.sin(angle));
        return new Point(x, y);
    }
}
