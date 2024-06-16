package graphapp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphDrawer {

    public static void main(String[] args) {
        String filePath = "C:\\Users\\Miss.Yu\\Desktop\\lab1-2021111811\\graph.txt"; // 请将此路径改为你的文本文件路径
        Map<String, Map<String, Integer>> graph = parseGraphFile(filePath);

        if (graph == null) {
            System.err.println("Error parsing graph file.");
            return;
        }

        // 绘制有向图并保存为PNG文件
        showDirectedGraph(graph, "directed_graph.png");
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

    public static void showDirectedGraph(Map<String, Map<String, Integer>> graph, String filename) {
        // 创建图像
        int width = 1000;
        int height = 1000;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 设置白色背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // 绘制图形
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(2));
        drawGraph(graph, g2d, width, height);

        // 保存图像到文件
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

    private static void drawGraph(Map<String, Map<String, Integer>> graph, Graphics2D g2d, int width, int height) {
        int margin = 50;
        int nodeSize = 50;

        int nodeIndex = 0;
        int totalNodes = graph.size();
        Map<String, Point> nodePositions = new HashMap<>();
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(centerX, centerY) - margin;

        // 计算节点位置
        for (String node : graph.keySet()) {
            double angle = 2 * Math.PI * nodeIndex / totalNodes;
            int x = centerX + (int) (radius * Math.cos(angle));
            int y = centerY + (int) (radius * Math.sin(angle));
            nodePositions.put(node, new Point(x, y));
            nodeIndex++;
        }

        // 绘制边
        g2d.setColor(Color.BLACK); // 设置边颜色为黑色
        for (String from : graph.keySet()) {
            Point startPoint = nodePositions.get(from);
            Map<String, Integer> edges = graph.get(from);
            if (edges != null) {
                for (String to : edges.keySet()) {
                    Point endPoint = nodePositions.get(to);
                    int weight = edges.get(to);
                    drawCurvedArrow(g2d, startPoint, endPoint, nodeSize / 2);
                    drawWeight(g2d, startPoint, endPoint, weight, nodeSize / 2);
                }
            }
        }

        // 绘制节点
        g2d.setColor(Color.BLACK); // 设置节点边框颜色为黑色
        for (String node : graph.keySet()) {
            Point point = nodePositions.get(node);
            drawNode(g2d, point, nodeSize, node);
        }
    }

    private static void drawNode(Graphics2D g2d, Point center, int size, String label) {
        // 绘制白色的圆形
        g2d.setColor(Color.WHITE);
        g2d.fillOval(center.x - size / 2, center.y - size / 2, size, size);

        // 绘制黑色的边框
        g2d.setColor(Color.BLACK);
        g2d.drawOval(center.x - size / 2, center.y - size / 2, size, size);

        // 在圆形内部绘制节点的名字
        g2d.setColor(Color.BLACK); // 设置文本颜色为黑色
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

        double t = 1.0 / 3.0; // 1/3处
        double x = Math.pow(1 - t, 2) * fromEdge.x + 2 * (1 - t) * t * ctrlX + Math.pow(t, 2) * toEdge.x;
        double y = Math.pow(1 - t, 2) * fromEdge.y + 2 * (1 - t) * t * ctrlY + Math.pow(t, 2) * toEdge.y;

        double dx = 10 * Math.cos(Math.atan2(toEdge.y - ctrlY, toEdge.x - ctrlX));
        double dy = 10 * Math.sin(Math.atan2(toEdge.y - ctrlY, toEdge.x - ctrlX));

        g2d.setColor(Color.BLACK); // 设置权重文本颜色为黑色
        g2d.drawString(String.valueOf(weight), (int) (x + dx), (int) (y + dy));
    }

    private static void drawCurvedArrow(Graphics2D g2d, Point from, Point to, int radius) {
        g2d.setColor(Color.BLACK); // 设置箭头颜色为黑色
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

        // 创建箭头形状
        int xArrow1 = (int) (toEdge.x - arrowLength * Math.cos(angle - arrowAngle));
        int yArrow1 = (int) (toEdge.y - arrowLength * Math.sin(angle - arrowAngle));
        int xArrow2 = (int) (toEdge.x - arrowLength * Math.cos(angle + arrowAngle));
        int yArrow2 = (int) (toEdge.y - arrowLength * Math.sin(angle + arrowAngle));

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(toEdge.x, toEdge.y);
        arrowHead.addPoint(xArrow1, yArrow1);
        arrowHead.addPoint(xArrow2, yArrow2);

        g2d.fill(arrowHead); // 使用填充绘制箭头
    }

    private static Point getEdgePoint(Point from, Point to, int radius) {
        double angle = Math.atan2(to.y - from.y, to.x - from.x);
        int x = (int) (from.x + radius * Math.cos(angle));
        int y = (int) (from.y + radius * Math.sin(angle));
        return new Point(x, y);
    }
}
