package graphapp;

// 导入JGraphT库中的Graph接口
import org.jgrapht.Graph;
// 导入JGraphT库中的DefaultWeightedEdge类，表示带权重的边
import org.jgrapht.graph.DefaultWeightedEdge;
// 导入JGraphT库中的DirectedWeightedMultigraph类，表示有向带权重的多重图
import org.jgrapht.graph.DirectedWeightedMultigraph;

import java.io.*; // 导入java.io包中的所有类，用于文件读写操作
import java.nio.file.*; // 导入java.nio.file包中的所有类，用于文件路径操作
import java.util.*; // 导入java.util包中的所有类，用于数据结构和集合操作

public class TextToGraph {

    private static Graph<String, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<>(
            DefaultWeightedEdge.class);

    // main方法是程序的入口点
    public static void main(String[] args) {
        String filePath; // 定义一个字符串变量filePath，用于存储文件路径

        // 如果程序启动时提供了文件路径作为参数
        if (args.length > 0) {
            filePath = args[0]; // 将第一个参数作为文件路径
        } else {
            // 如果没有提供参数，则提示用户输入文件路径
            Scanner scanner = new Scanner(System.in); // 创建一个Scanner对象，用于从控制台读取输入
            System.out.println("Please enter the file path: "); // 提示用户输入文件路径
            filePath = scanner.nextLine(); // 读取用户输入的文件路径
            scanner.close(); // 关闭Scanner对象
        }

        // 读取文件内容并将其转换为单词列表
        List<String> words = readFile(filePath);
        // 调用 readFile 方法读取指定路径的文件内容，并将其转换为单词列表，readFile 方法会返回一个包含文件中所有单词的列表。
        if (words != null) { // 如果读取到的单词列表不为空
            buildGraph(words); // 构建图
            try {
                saveGraphAsText(); // 将图保存为文本文件
            } catch (IOException e) { // 捕获保存过程中可能发生的IO异常
                System.err.println("Error saving graph as text: " + e.getMessage()); // 打印错误信息
            }
        }
    }

    private static List<String> readFile(String filePath) {
        List<String> words = new ArrayList<>();
        try {

            String content = new String(Files.readAllBytes(Paths.get(filePath)))
                    .toLowerCase()
                    .replaceAll("[^a-z\\s]", " ") // 将非字母字符替换为空格
                    .replaceAll("\\s+", " "); // 将多个空白字符合并为一个空格
            // 使用空白字符拆分文件内容，得到单词数组，并将其转换为列表
            words = Arrays.asList(content.split("\\s+"));
        } catch (IOException e) { // 捕获读取文件过程中可能发生的IO异常
            System.err.println("Error reading file: " + e.getMessage()); // 打印错误信息
        }
        return words; // 返回单词列表
    }

    // 根据单词列表构建有向带权重的图
    private static void buildGraph(List<String> words) {
        // 遍历单词列表中的每一个单词
        for (int i = 0; i < words.size() - 1; i++) {
            String word1 = words.get(i); // 当前单词
            String word2 = words.get(i + 1); // 下一个单词
            graph.addVertex(word1); // 将当前单词添加为图的一个节点
            graph.addVertex(word2); // 将下一个单词添加为图的一个节点
            DefaultWeightedEdge edge = graph.getEdge(word1, word2); // 获取当前单词到下一个单词的边
            if (edge == null) { // 如果边不存在
                edge = graph.addEdge(word1, word2); // 添加一条从当前单词到下一个单词的边
                graph.setEdgeWeight(edge, 1.0); // 设置边的权重为1.0
            } else { // 如果边已经存在
                double currentWeight = graph.getEdgeWeight(edge); // 获取边的当前权重
                graph.setEdgeWeight(edge, currentWeight + 1.0); // 将边的权重增加1.0
            }
        }
    }

    // 将图保存为文本文件
    private static void saveGraphAsText() throws IOException {
        // 使用BufferedWriter创建一个文件输出流
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("graph.txt"))) {
            // 遍历图中的每一个节点
            for (String vertex : graph.vertexSet()) {
                writer.write("Node " + vertex + " has edges:\n"); // 写入节点信息
                // 遍历从该节点出发的每一条边
                for (DefaultWeightedEdge edge : graph.outgoingEdgesOf(vertex)) {
                    String target = graph.getEdgeTarget(edge); // 获取边的目标节点
                    double weight = graph.getEdgeWeight(edge); // 获取边的权重
                    writer.write("  to " + target + " with weight " + weight + "\n"); // 写入边的信息
                }
            }
        }
        System.out.println("Graph saved as graph.txt"); // 打印保存成功信息
    }
}
