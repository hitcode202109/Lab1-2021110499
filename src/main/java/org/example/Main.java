package org.example;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileWriter;



public class Main {
    public class Graph {
        private Set<String> nodeSet;
        private Map<String, Map<String, Integer>> edgeSet;

        public Graph() {
            nodeSet = new HashSet<String>();
            edgeSet = new HashMap<String, Map<String, Integer>>();
        }

        public void addNode(String node) {
            nodeSet.add(node);
        }

        public void addEdge(String src, String dest) {
            // 检查源节点是否已经存在于edgeSet中
            if (!edgeSet.containsKey(src)) {
                edgeSet.put(src, new HashMap<>());
            }
            // 检查从src到dest的边是否已经存在
            if (edgeSet.get(src).containsKey(dest)) {
                // 如果存在，增加权重
                edgeSet.get(src).put(dest, edgeSet.get(src).get(dest) + 1);
            } else {
                // 如果不存在，添加新的边，权重为1
                edgeSet.get(src).put(dest, 1);
            }
        }




        //有向图表示
        public void showDirectedGraph(Graph graph) {
            // 打印节点集合
            System.out.println("Node Set:");
            System.out.println(graph.nodeSet);

            // 打印带权重的边
            for (Map.Entry<String, Map<String, Integer>> entry : graph.edgeSet.entrySet()) {
                System.out.println("Node: " + entry.getKey());
                for (Map.Entry<String, Integer> weightEntry : entry.getValue().entrySet()) {
                    System.out.println(" -> " + weightEntry.getKey() + " (weight: " + weightEntry.getValue() + ")");
                }
            }
        }

        ///查询桥接词（bridge words）
        public String queryBridgeWords(String word1, String word2) {
            //首先检查word1，word2是否存在
            if (!nodeSet.contains(word1) || !nodeSet.contains(word2)) {
                System.out.println("No " + word1 + " or " + word2 + " in the graph!");
                return " ";
            }

            Set<String> bridgeWords = new HashSet<>();
            for (Map.Entry<String, Map<String, Integer>> entry : edgeSet.entrySet()) {
                String src = entry.getKey();
                Map<String, Integer> dests = entry.getValue();
                if (src.equals(word1)) {
                    for (String dest : dests.keySet()) {
                        if (edgeSet.containsKey(dest) && edgeSet.get(dest).containsKey(word2)) {
                            bridgeWords.add(dest);
                        }
                    }
                }
            }

            if (bridgeWords.isEmpty()) {
                System.out.println("No bridge words from " + word1 + " to " + word2 + "!");
                return " ";
            }

            StringBuilder result = new StringBuilder();
            for (String word : bridgeWords) {
                if (result.length() > 0) {
                    result.append(", ");
                }
                result.append(word);
            }
            System.out.println("The bridge words from " + word1 + " to " + word2 + " is(are): " + result);
            return result.toString();
        }

        //基于桥接词查询生成新文本
        public String generateNewText(String inputext) {
            List<String> StringList = INPUT_text(inputext);
            int size = StringList.size();
            StringBuilder newWordsBuilder = new StringBuilder();
            for (int i = 0; i < size - 1; i++) {
                String word = queryBridgeWords(StringList.get(i), StringList.get(i + 1));
                newWordsBuilder.append(StringList.get(i));
                newWordsBuilder.append(" ");
                if (!Objects.equals(word, " ")) {
                    newWordsBuilder.append(word);
                    newWordsBuilder.append(" ");
                }
            }
            newWordsBuilder.append(StringList.get(size - 1));
            return newWordsBuilder.toString();
        }

        //获取节点的邻居节点和权重
        private Map<String, Integer> getNeighbors(String node) {
            return edgeSet.getOrDefault(node, Collections.emptyMap());
        }

        //最短路径
        public String calcShortestPath(String start, String end) {
            if (!nodeSet.contains(start) || !nodeSet.contains(end)) {
                return "No " + start + " or " + end + " in the graph!";
            }

            // 初始化距离数组，默认无穷大
            Map<String, Integer> distances = new HashMap<>();
            for (String node : nodeSet) {
                distances.put(node, Integer.MAX_VALUE);
            }
            distances.put(start, 0);

            // 优先队列，用于存储待处理的节点和它们的距离
            PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
                    (e1, e2) -> Integer.compare(e1.getValue(), e2.getValue())
            );
            pq.add(new AbstractMap.SimpleEntry<>(start, 0));

            // 存储最短路径
            Map<String, String> prev = new HashMap<>();

            while (!pq.isEmpty()) {
                Map.Entry<String, Integer> current = pq.poll();
                String currentNode = current.getKey();
                int currentDistance = current.getValue();

                // 如果当前节点已经被处理过更短的距离，则跳过
                if (currentDistance > distances.get(currentNode)) {
                    continue;
                }

                // 检查是否到达目标节点
                if (currentNode.equals(end)) {
                    StringBuilder path = new StringBuilder();
                    for (String node = end; !node.equals(start); node = prev.get(node)) {
                        if (path.length() > 0) {
                            path.append("→");
                        }
                        path.append(node);
                    }
                    path.append("→").append(start);
                    return path.toString() + " (length: " + distances.get(end) + ")";
                }

                // 遍历当前节点的所有邻居
                for (Map.Entry<String, Integer> neighbor : getNeighbors(currentNode).entrySet()) {
                    String neighborNode = neighbor.getKey();
                    int newDistance = currentDistance + neighbor.getValue();

                    // 如果通过当前节点到达邻居节点的距离更短，则更新距离和最短路径
                    if (newDistance < distances.get(neighborNode)) {
                        distances.put(neighborNode, newDistance);
                        prev.put(neighborNode, currentNode);
                        pq.add(new AbstractMap.SimpleEntry<>(neighborNode, newDistance));
                    }
                }
            }

            return "No path from " + start + " to " + end + "!";
        }

        //随机游走
        public String randomWalk() {
            List<String> walk = new ArrayList<>();
            Random rand = new Random();
            Set<String> visitedEdges = new HashSet<>();

            // 随机选择起始节点
            if (nodeSet.isEmpty()) {
                throw new IllegalArgumentException("The graph is empty, no nodes to start from");
            }
            String[] nodes = nodeSet.toArray(new String[0]);
            String currentNode = nodes[rand.nextInt(nodes.length)];

            walk.add(currentNode);
            Scanner scanner = new Scanner(System.in);
            boolean flag = true;
            while(flag) {
                Map<String, Integer> neighbors = edgeSet.get(currentNode);

                if (neighbors == null || neighbors.isEmpty()) {
                    break; // 当前节点没有出边，停止随机游走
                }

                // 从邻居中随机选择一个节点
                int totalWeight = 0;
                for (int weight : neighbors.values()) {
                    totalWeight += weight;
                }

                int randomValue = rand.nextInt(totalWeight);
                int cumulativeWeight = 0;
                String nextNode = null;

                for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
                    cumulativeWeight += entry.getValue();
                    if (randomValue < cumulativeWeight) {
                        nextNode = entry.getKey();
                        break;
                    }
                }

                // 检查是否遇到重复边
                String edge = currentNode + "->" + nextNode;
                if (visitedEdges.contains(edge)) {
                    break; // 遇到重复边，停止随机游走
                }

                // 记录边和节点
                visitedEdges.add(edge);
                currentNode = nextNode;
                walk.add(currentNode);


            }


            // 将walk列表转换为字符串
            System.out.println(walk);
            String walking_path = String.join(" -> ", walk);
            System.out.println(walking_path);
            return String.join(" -> ", walk);

        }
        //有向图可视化
        public static void writeSVGToHTML(String svgContent) {
            String htmlContent = "<!DOCTYPE html>\n<html>\n<head>\n<title>SVG Output</title>\n</head>\n<body>\n"
                    + svgContent + "\n</body>\n</html>";

            try {
                // 将SVG内容写入到index.html
                FileWriter writer = new FileWriter("index.html");
                writer.write(htmlContent);
                writer.close();

                // 在浏览器中打开index.html
                Runtime.getRuntime().exec("cmd /c start index.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*public String generateSVG() {
            StringBuilder svg = new StringBuilder();
            svg.append("<svg xmlns='http://www.w3.org/2000/svg' width='4500' height='5120'>");

            // Define constants for tree layout
            int nodeRadius = 20;
            int verticalSpacing = 100;
            int horizontalSpacing = 200;
            int rootX = 3000;
            int rootY = 100;

            // Map to hold the positions of nodes
            Map<String, double[]> positions = new HashMap<>();

            // Identify the root node
            Random rand = new Random();
            String[] nodes = nodeSet.toArray(new String[0]);
            String rootNode = nodes[rand.nextInt(nodes.length)];

            // Assign positions to nodes in a tree layout
            assignPositions(rootNode, rootX, rootY, horizontalSpacing, positions, verticalSpacing);

            // Draw edges with arrows
            for (String src : edgeSet.keySet()) {
                for (String dest : edgeSet.get(src).keySet()) {
                    double[] srcPos = positions.get(src);
                    double[] destPos = positions.get(dest);
                    double dx = destPos[0] - srcPos[0];
                    double dy = destPos[1] - srcPos[1];
                    double angle = Math.atan2(dy, dx);
                    double midX = (srcPos[0] + destPos[0]) / 2;
                    double midY = (srcPos[1] + destPos[1]) / 2;

                    // Calculate arrow points
                    double arrowLength = 10;
                    double arrowX1 = midX - arrowLength * Math.cos(angle - Math.PI / 6);
                    double arrowY1 = midY - arrowLength * Math.sin(angle - Math.PI / 6);
                    double arrowX2 = midX - arrowLength * Math.cos(angle + Math.PI / 6);
                    double arrowY2 = midY - arrowLength * Math.sin(angle + Math.PI / 6);

                    // Draw line
                    svg.append("<line x1='").append(srcPos[0]).append("' y1='").append(srcPos[1])
                            .append("' x2='").append(destPos[0]).append("' y2='").append(destPos[1])
                            .append("' stroke='black'/>");

                    // Draw arrowhead at the midpoint
                    svg.append("<polygon points='").append(midX).append(",").append(midY)
                            .append(" ").append(arrowX1).append(",").append(arrowY1)
                            .append(" ").append(arrowX2).append(",").append(arrowY2)
                            .append("' fill='black'/>");

                    // Draw weight near the midpoint, above the arrow
                    int weight = edgeSet.get(src).get(dest);
                    svg.append("<text x='").append(midX + 5).append("' y='").append(midY - 5)
                            .append("' fill='red' font-size='12'>").append(weight).append("</text>");
                }
            }

            // Draw nodes
            for (String node : positions.keySet()) {
                double[] pos = positions.get(node);
                svg.append("<circle cx='").append(pos[0]).append("' cy='").append(pos[1])
                        .append("' r='").append(nodeRadius).append("' fill='lightblue' stroke='black'/>");
                svg.append("<text x='").append(pos[0]).append("' y='").append(pos[1])
                        .append("' text-anchor='middle' dy='.3em' font-size='10'>").append(node)
                        .append("</text>");
            }

            // Close SVG
            svg.append("</svg>");
            writeSVGToHTML(svg.toString());
            return svg.toString();
        }


        // Helper method to assign positions in a tree layout
        private void assignPositions(String node, int x, int y, int spacing, Map<String, double[]> positions, int verticalSpacing) {
            if (positions.containsKey(node)) {
                return; // Position already assigned
            }
            positions.put(node, new double[]{x, y});

            Map<String, Integer> children = edgeSet.getOrDefault(node, Collections.emptyMap());
            int totalWidth = (children.size() - 1) * spacing;
            int startX = x - totalWidth / 2;

            int i = 0;
            for (String child : children.keySet()) {
                int childX = startX + i * spacing;
                int childY = y + i*verticalSpacing;
                assignPositions(child, childX, childY, spacing, positions, verticalSpacing);
                i++;
            }
        }*/

        public String generateSVG() {
            StringBuilder svg = new StringBuilder();
            svg.append("<svg xmlns='http://www.w3.org/2000/svg' width='5120' height='3200'>");

            // Map each node to coordinates for simplicity
            int radius = 1200;
            int centerX = 2560;
            int centerY = 1600;
            int nodeRadius = 40;
            double angleStep = 2 * Math.PI / nodeSet.size();

            // Create node positions
            Map<String, double[]> positions = new HashMap<>();
            int i = 0;
            for (String node : nodeSet) {
                double angle = i * angleStep;
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);
                positions.put(node, new double[]{x, y});
                i++;
            }

            // Draw edges with arrows
            for (String src : edgeSet.keySet()) {
                for (String dest : edgeSet.get(src).keySet()) {
                    double[] srcPos = positions.get(src);
                    double[] destPos = positions.get(dest);
                    double dx = destPos[0] - srcPos[0];
                    double dy = destPos[1] - srcPos[1];
                    double angle = Math.atan2(dy, dx);
                    double midX = (srcPos[0] + destPos[0]) / 2;
                    double midY = (srcPos[1] + destPos[1]) / 2;

                    // Calculate arrow points
                    double arrowLength = 10;
                    double arrowX1 = midX - arrowLength * Math.cos(angle - Math.PI / 6);
                    double arrowY1 = midY - arrowLength * Math.sin(angle - Math.PI / 6);
                    double arrowX2 = midX - arrowLength * Math.cos(angle + Math.PI / 6);
                    double arrowY2 = midY - arrowLength * Math.sin(angle + Math.PI / 6);

                    // Draw line
                    svg.append("<line x1='").append(srcPos[0]).append("' y1='").append(srcPos[1])
                            .append("' x2='").append(destPos[0]).append("' y2='").append(destPos[1])
                            .append("' stroke='black'/>");

                    // Draw arrowhead at the midpoint
                    svg.append("<polygon points='").append(midX).append(",").append(midY)
                            .append(" ").append(arrowX1).append(",").append(arrowY1)
                            .append(" ").append(arrowX2).append(",").append(arrowY2)
                            .append("' fill='black'/>");

                    // Draw weight near the midpoint, above the arrow
                    int weight = edgeSet.get(src).get(dest);
                    svg.append("<text x='").append(midX + 5).append("' y='").append(midY - 5)
                            .append("' fill='red' font-size='24'>").append(weight).append("</text>");
                }
            }

            // Draw nodes
            for (String node : nodeSet) {
                double[] pos = positions.get(node);
                svg.append("<circle cx='").append(pos[0]).append("' cy='").append(pos[1])
                        .append("' r='").append(nodeRadius).append("' fill='lightblue' stroke='black'/>");
                svg.append("<text x='").append(pos[0]).append("' y='").append(pos[1])
                        .append("' text-anchor='middle' dy='.3em' font-size='20'>").append(node)
                        .append("</text>");
            }

            // Close SVG
            svg.append("</svg>");
            writeSVGToHTML(svg.toString());
            return svg.toString();
        }
    }



    public class Text {
        private List<String> words;

        public Text(String text) {
            // 使用空格作为单词分隔符
            words = Arrays.asList(text.split("\\s+"));
        }

        public List<String> getWords() {
            return words;
        }
    }
    public void Text2Graph(Graph graph, Text text){
        List<String> words = text.getWords();
        // 遍历所有单词，但最后一个单词没有后继，所以不用添加到图中
        for (int i = 0; i < words.size() - 1; i++) {
            String src = words.get(i);
            String dest = words.get(i + 1);

            // 添加单词到节点集合
            if (!graph.nodeSet.contains(src)) {
                graph.addNode(src);
            }
            if (!graph.nodeSet.contains(dest)) {
                graph.addNode(dest);
            }

            // 添加有向边
            graph.addEdge(src, dest);
        }
    }
    public static String address_file(String file){
        String fileto = file;
        char[] charArray = file.toCharArray();
        for (int i = 0; i < fileto.length(); i++) {
            char c = fileto.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                charArray[i] = (char)(c+32);
            }
            else if(c >= 'a' && c <= 'z'){
                charArray[i] = c;
            }
            else {
                charArray[i] = ' ';
            }
        }
        file = new String(charArray);
        return file;
    }

    public  static List<String> INPUT_text(String text){
        List<String> stringList = new ArrayList<>();
        char[] textCharArrayArray = text.toCharArray();
        StringBuilder sb = new StringBuilder();
        int flag = -1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                if(flag == i-1){

                }
                else{
                    String str = sb.toString();
                    stringList.add(str);
                    sb.setLength(0);
                }
                flag = i;
            }
            else {
                sb.append(c);
            }
            if(i == text.length()-1){
                //sb.append(c);
                String str = sb.toString();
                stringList.add(str);
                sb.setLength(0);
            }
        }
        return stringList;
    }



    public static void main(String[] args) {
        // 示例文本
        //String text = "To @ explore strange new worlds,To seek out new life and new civilizations?";

        String filePath = "text1.txt";
        try {
            // 读取文件内容
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            // 将所有行合并为一个字符串，并用空格分隔单词
            String text = String.join(" ", lines);
            text = address_file(text);

            // 创建lab1(改为main对象,可移植更改即可)对象和Graph对象
            Main labInstance = new Main();
            Main.Graph graph = labInstance.new Graph();

            // 创建Text对象
            Text textObj = labInstance.new Text(text);

            // 调用Text2Graph方法
            labInstance.Text2Graph(graph, textObj);

            Scanner scanner = new Scanner(System.in);
            boolean flag = true;
            while(flag) {
                System.out.println("输入指令展示对应功能");
                System.out.println("1.有向图节点展示     2.有向图SVG格式展示");
                System.out.println("3.查询桥接词        4.基于桥接词的文本插入");
                System.out.println("5.计算最短路径      6.随机游走");
                System.out.println("7.退出");
                System.out.println("choose your order:");
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1://有向图
                        graph.showDirectedGraph(graph);
                        break;
                    case 2://可视化
                        System.out.println(graph.generateSVG());
                        break;
                    case 3://查询桥接词（bridge words）
                        System.out.println("目标文本");
                        System.out.println(text);
                        System.out.println("please input the first word:");
                        String bri_src_word = scanner.next();
                        System.out.println("please input thr second word:");
                        String bri_dir_word = scanner.next();
                        String o = graph.queryBridgeWords(bri_src_word, bri_dir_word);


                        break;
                    case 4://基于桥接词的文本插入
                        System.out.println("please input the inputtext:");
                        String inputext = scanner.nextLine();
                        inputext = String.join(" ", inputext);
                        inputext = address_file(inputext);
                        inputext = graph.generateNewText(inputext);
                        System.out.println(inputext);

                        break;


                    case 5://计算最短路径
                        System.out.println("目标文本");
                        System.out.println(text);
                        System.out.println("please input the first word:");
                        String les_src_word = scanner.next();
                        System.out.println("please input thr second word:");
                        String les_dir_word = scanner.next();
                        System.out.println(graph.calcShortestPath(les_src_word, les_dir_word));
                        break;

                    case 6:// 随机游走
                        graph.randomWalk();
                        break;
                    case 7:
                        flag = false;
                        break;
                    default:
                        System.out.println("your order is illegal");
                        break;

                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading file.");
        }
    }
}




