package graphapp;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GraphWithBridgeWords {

    private static Map<String, Map<String, Integer>> graph = new HashMap<>();

    public static void main(String[] args) {
        String graphFilePath = "C:\\Users\\Miss.Yu\\Desktop\\lab1-2021111811\\graph.txt"; // 请将此路径改为你的文本文件路径
        graph = parseGraphFile(graphFilePath);

        if (graph == null) {
            System.err.println("Error parsing graph file.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter a new text: ");
        String newText = scanner.nextLine();
        scanner.close();

        String resultText = insertBridgeWords(newText);
        System.out.println("Resulting text: " + resultText);
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

    private static String insertBridgeWords(String text) {
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length - 1; i++) {
            result.append(words[i]).append(" ");
            String bridgeWord = getBridgeWord(words[i], words[i + 1]);
            if (bridgeWord != null) {
                result.append(bridgeWord).append(" ");
            }
        }
        result.append(words[words.length - 1]);
        return result.toString();
    }

    private static String getBridgeWord(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return null;
        }

        List<String> bridgeWords = new ArrayList<>();
        for (String candidate : graph.get(word1).keySet()) {
            if (graph.get(candidate).containsKey(word2)) {
                bridgeWords.add(candidate);
            }
        }

        if (bridgeWords.isEmpty()) {
            return null;
        }

        Random random = new Random();
        return bridgeWords.get(random.nextInt(bridgeWords.size()));
    }
}
