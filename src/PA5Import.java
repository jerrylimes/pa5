import org.jgrapht.Graph;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PA5Import {
    private static int classSize;
    private static int roomSize;
    private static int timeSlot;
    private static int proctor;
    private static Graph<Integer, DefaultWeightedEdge> constructedGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    private static String path;
    private static List<DefaultWeightedEdge> sortedEdges = new ArrayList<>();
    private static Map<Integer, Integer> timeUsage = new HashMap<>();

    /* Construct the graph based on the input numbers */
    public static Graph<Integer, DefaultWeightedEdge> constructGraph(int c, int r, int t, int p, int proctorCapacity, int[] classSizes, int[] roomSizes, ArrayList<ArrayList<Integer>> proctorAvailability) {
        /* there are a total of numberOfNodes nodes, including source and sink */
        int numberOfNodes = 2 + c + r + t + p;
        classSize = c;
        roomSize = r;
        timeSlot = t;
        proctor = p;
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        for (int i = 0; i < numberOfNodes; i++) {
            graph.addVertex(i);
        }
        /* source cannot connect to itself; start from the second column */
        for (int i = 1; i < 1 + c; i++) {
            /* source node is connected to every class */
            DefaultWeightedEdge e = graph.addEdge(0, i);
            if (e != null) {
                graph.setEdgeWeight(e, 1);
            }
        }
        /* skip all the columns of classes and rooms and times and the source node; start from proctors */
        int sink = numberOfNodes - 1;
        for (int i = 1 + c + r + t; i < 1 + c + r + t + p; i++) {
            /* every proctor node is connected to the sink node with the capacity being the proctorCapacity*/
            DefaultWeightedEdge e = graph.addEdge(i, sink);
            if (e != null) {
                graph.setEdgeWeight(e, proctorCapacity);
            }
        }
        /* any exam could potentially be at any time, so every room connects to every time */
        for (int i = 1 + c; i < 1 + c + r; i++) {
            for (int j = 1 + c + r; j < 1 + c + r + t; j++) {
                /* for every room i, and every time j */
                DefaultWeightedEdge e = graph.addEdge(i, j);
                if (e != null) {
                    graph.setEdgeWeight(e, 1);
                }
            }
        }
        /* can a room hold all the students in a class? let's find out */
        for (int i = 1; i < 1 + c; i++) {
            for (int j = 1 + c; j < 1 + c + r; j++) {
                /* if the size of the class is smaller than the size of the room */
                int classIndex = i - 1;
                int roomIndex = j - (1 + c);
                if (classSizes[classIndex] <= roomSizes[roomIndex]) {
                    DefaultWeightedEdge e = graph.addEdge(i, j);
                    if (e != null) {
                        graph.setEdgeWeight(e, 1);
                    }
                }
            }
        }
        /* are the proctors available at a time? */
        for (int proctorIndex = 0; proctorIndex < p; proctorIndex++) {
            int proctorNode = 1 + c + r + t + proctorIndex;
            for (int time : proctorAvailability.get(proctorIndex)) {
                int availableTime = 1 + c + r + time;
                DefaultWeightedEdge e = graph.addEdge(availableTime, proctorNode);
                if (e != null) {
                    graph.setEdgeWeight(e, 1);
                }
            }
        }
        setConstructedGraph(graph);
        return graph;
    }

    public static MaximumFlowAlgorithm.MaximumFlow<DefaultWeightedEdge> bipartiteMatching(int source, int sink) {
        Graph<Integer, DefaultWeightedEdge> graph = getConstructedGraph();
        EdmondsKarpMFImpl<Integer, DefaultWeightedEdge> mf = new EdmondsKarpMFImpl<>(graph);
        return mf.getMaximumFlow(source, sink);
    }

    public static ArrayList<String> backtrackPath(int source, int sink) {
        sortedEdges.clear();
        timeUsage.clear();
        Graph<Integer, DefaultWeightedEdge> graph = getConstructedGraph();
        ArrayList<String> paths = new ArrayList<>();
        var result = bipartiteMatching(source, sink);
        Map<DefaultWeightedEdge, Double> flowMap = result.getFlowMap();

        for (DefaultWeightedEdge e : graph.edgeSet()) {
            if (flowMap.get(e) > 0.0) {
                sortedEdges.add(e);
            }
        }
        sortedEdges.sort(Comparator.comparingInt(graph::getEdgeSource).thenComparingInt(graph::getEdgeTarget));
        for (DefaultWeightedEdge e : sortedEdges) {
            path = "";
            if (graph.getEdgeSource(e) >= 1 && graph.getEdgeSource(e) < 1 + classSize) {
                path += "c" + (graph.getEdgeSource(e) - 1) + " - ";
                processClass(e);
                paths.add(path);
            }
        }
        // System.out.println(classSize + " " + roomSize + " " + timeSlot + " " + proctor);
        // instance variables test successful
        return paths;
    }

    public static void processClass(DefaultWeightedEdge e) {
        Graph<Integer, DefaultWeightedEdge> graph = getConstructedGraph();
        int classNode = graph.getEdgeSource(e);
        int roomNode = graph.getEdgeTarget(e);
        int roomIndex = roomNode - (1 + classSize);
        path += "r" + roomIndex + " - ";
        processRoom(roomNode);
    }

    public static void processRoom(int roomNode) {
        Graph<Integer, DefaultWeightedEdge> graph = getConstructedGraph();
        for (DefaultWeightedEdge e : sortedEdges) {
            if (graph.getEdgeSource(e) == roomNode) {
                int timeNode = graph.getEdgeTarget(e);
                if (timeNode >= 1 + classSize + roomSize && timeNode < 1 + classSize + roomSize + timeSlot) {
                    int timeIndex = timeNode - (1 + classSize + roomSize);
                    path += "t" + timeIndex + " - ";
                    processTime(timeNode);
                    return;
                }
            }
        }
    }

    public static void processTime(int timeNode) {
        Graph<Integer, DefaultWeightedEdge> graph = getConstructedGraph();
        for (DefaultWeightedEdge e : sortedEdges) {
            if (graph.getEdgeSource(e) == timeNode) {
                int proctorNode = graph.getEdgeTarget(e);
                if (proctorNode >= 1 + classSize + roomSize + timeSlot && proctorNode < 1 + classSize + roomSize + timeSlot + proctor) {
                    processProctor(proctorNode);
                    return;
                }
            }
        }
    }

    public static void processProctor(int proctorNode) {
        int proctorIndex = proctorNode - (1 + classSize + roomSize + timeSlot);
        path += "p" + proctorIndex;
    }

    public static Graph<Integer, DefaultWeightedEdge> getConstructedGraph() {
        return constructedGraph;
    }

    public static void setConstructedGraph(Graph<Integer, DefaultWeightedEdge> constructedGraph) {
        PA5Import.constructedGraph = constructedGraph;
    }


    public static void main(String[] args) {
        int hardCodedC = 9;
        int hardCodedR = 5;
        int hardCodedT = 4;
        int hardCodedP = 7;
        int hardCodedProctorCapacity = 5;
        int[] hardCodedClassSizes = {12, 20, 34, 57, 63, 63, 87, 153, 725};
        int[] hardCodedRoomSizes = {18, 48, 72, 100, 850};
        ArrayList<ArrayList<Integer>> hardCodedProctorAvailability = new ArrayList<>();
        int[][] hardCodedProctor = {{0, 1}, {0}, {0, 2}, {0, 1, 2}, {1, 2, 3}, {2, 3}, {1, 3}};
        for (int j = 0; j < hardCodedProctor.length; j++) {
            ArrayList<Integer> a = new ArrayList<>();
            for (int k = 0; k < hardCodedProctor[j].length; k++) {
                a.add(hardCodedProctor[j][k]);
            }
            hardCodedProctorAvailability.add(a);
        }
//            Graph<Integer, DefaultWeightedEdge> graph = constructGraph(c, r, t, p, proctorCapacity, classSizes, roomSizes, proctorAvailability);
        constructGraph(hardCodedC, hardCodedR, hardCodedT, hardCodedP, hardCodedProctorCapacity, hardCodedClassSizes, hardCodedRoomSizes, hardCodedProctorAvailability);
        int numberOfNodes = 2 + hardCodedC + hardCodedR + hardCodedT + hardCodedP;
        ArrayList<String> result = backtrackPath(0, numberOfNodes - 1);
        for (String str : result) {
            System.out.println(str);
        }
//        Scanner scanner = new Scanner(System.in);
//        int testCases = scanner.nextInt();
//        scanner.nextLine();
//        for (int i = 0; i < testCases; i++) {
//            String line = scanner.nextLine();
//            Scanner lineScan = new Scanner(line);
//            int c = lineScan.nextInt();
//            int r = lineScan.nextInt();
//            int t = lineScan.nextInt();
//            int p = lineScan.nextInt();
//            int proctorCapacity = lineScan.nextInt();
//            String classSize = scanner.nextLine();
//            String roomSize = scanner.nextLine();
//            Scanner classSizeLineScan = new Scanner(classSize);
//            Scanner roomSizeLineScan = new Scanner(roomSize);
//            int[] classSizes = new int[c];
//            int[] roomSizes = new int[r];
//            for (int j = 0; j < c; j++) {
//                classSizes[j] = classSizeLineScan.nextInt();
//            }
//            for (int j = 0; j < r; j++) {
//                roomSizes[j] = roomSizeLineScan.nextInt();
//            }
//            ArrayList<ArrayList<Integer>> proctorAvailability = new ArrayList<>();
//            for (int j = 0; j < p; j++) {
//                String proctor = scanner.nextLine();
//                Scanner proctorLineScan = new Scanner(proctor);
//                int numberOfTimesAvailable = proctorLineScan.nextInt();
//                ArrayList<Integer> thisProctor = new ArrayList<>();
//                for (int k = 0; k < numberOfTimesAvailable; k++) {
//                    thisProctor.add(proctorLineScan.nextInt());
//                }
//                proctorAvailability.add(thisProctor);
//            }

        // testing input readability
        // test successful
        // testing Ford-Fulkerson
        // testing JGraphT
        // example input will be hard-coded
//            int numberOfNodes = 2 + c + r + t + p;
        // Max Flow is processing the graph correctly
//            System.out.println(bipartiteMatching(graph, 0, numberOfNodes - 1));
        // Ford-Fulkerson successful!
//        }
    }
}
