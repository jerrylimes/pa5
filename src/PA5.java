import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import org.jgrapht.Graph;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.alg.interfaces.MaximumFlowAlgorithm;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class PA5 {
    private static int classSize;
    private static int roomSize;
    private static int timeSlot;
    private static int proctor;

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
        return graph;
    }

    public static MaximumFlowAlgorithm.MaximumFlow<DefaultWeightedEdge> bipartiteMatching(Graph<Integer, DefaultWeightedEdge> graph, int source, int sink) {
        EdmondsKarpMFImpl<Integer, DefaultWeightedEdge> mf = new EdmondsKarpMFImpl<>(graph);
        return mf.getMaximumFlow(source, sink);
    }

    public static void backtrackPath(Graph<Integer, DefaultWeightedEdge> graph, int source, int sink) {
        var result = bipartiteMatching(graph, source, sink);
        double maxFlow = result.getValue();
        int maxFlowInt = (int) maxFlow;
        System.out.println(maxFlowInt);
        Map<DefaultWeightedEdge, Double> flowMap = result.getFlowMap();
        for (DefaultWeightedEdge e : graph.edgeSet()) {
            double flow = flowMap.get(e);
            if (flow > 0) {
                int u = graph.getEdgeSource(e);
                int v = graph.getEdgeTarget(e);
                if (u >= 1 && u < 1 + classSize && v >= 1 + classSize && v < 1 + classSize + roomSize) {
                    int roomNode = v;
                    int timeNode = -1;
                    for (DefaultWeightedEdge e2 : graph.outgoingEdgesOf(roomNode)) {
                        int next = graph.getEdgeTarget(e2);
                        if (flowMap.get(e2) == 1.0 && next >= 1 + classSize + roomSize && next < 1 + classSize + roomSize + timeSlot) {
                            timeNode = next;
                            break;
                        }
                    }
                    int proctorNode = -1;
                    if (timeNode != -1) {
                        for (DefaultWeightedEdge e3 : graph.outgoingEdgesOf(timeNode)) {
                            int next = graph.getEdgeTarget(e3);
                            if (flowMap.get(e3) == 1.0 & next >= 1 + classSize + roomSize + timeSlot && next < 1 + classSize + roomSize + timeSlot + proctor) {
                                proctorNode = next;
                                break;
                            }
                        }
                    }
                    if (timeNode != -1 && proctorNode != -1) {
                        int classIndex = u - 1;
                        int roomIndex = v - (1 + classSize);
                        int timeIndex = timeNode - (1 + classSize + roomSize);
                        int proctorIndex = proctorNode - (1 + classSize + roomSize + timeSlot);
                        System.out.println("c" + classIndex + " - r" + roomIndex + " - t" + timeIndex + " - p" + proctorIndex);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int testCases = scanner.nextInt();
        scanner.nextLine();
        for (int i = 0; i < testCases; i++) {
            String line = scanner.nextLine();
            Scanner lineScan = new Scanner(line);
            int c = lineScan.nextInt();
            int r = lineScan.nextInt();
            int t = lineScan.nextInt();
            int p = lineScan.nextInt();
            int proctorCapacity = lineScan.nextInt();
            String classSize = scanner.nextLine();
            String roomSize = scanner.nextLine();
            Scanner classSizeLineScan = new Scanner(classSize);
            Scanner roomSizeLineScan = new Scanner(roomSize);
            int[] classSizes = new int[c];
            int[] roomSizes = new int[r];
            for (int j = 0; j < c; j++) {
                classSizes[j] = classSizeLineScan.nextInt();
            }
            for (int j = 0; j < r; j++) {
                roomSizes[j] = roomSizeLineScan.nextInt();
            }
            ArrayList<ArrayList<Integer>> proctorAvailability = new ArrayList<>();
            for (int j = 0; j < p; j++) {
                String proctor = scanner.nextLine();
                Scanner proctorLineScan = new Scanner(proctor);
                int numberOfTimesAvailable = proctorLineScan.nextInt();
                ArrayList<Integer> thisProctor = new ArrayList<>();
                for (int k = 0; k < numberOfTimesAvailable; k++) {
                    thisProctor.add(proctorLineScan.nextInt());
                }
                proctorAvailability.add(thisProctor);
            }
            // testing input readability
            // test successful
            // testing Ford-Fulkerson
            Graph<Integer, DefaultWeightedEdge> graph = constructGraph(c, r, t, p, proctorCapacity, classSizes, roomSizes, proctorAvailability);
            int numberOfNodes = 2 + c + r + t + p;
            backtrackPath(graph, 0, numberOfNodes - 1);
            // Ford-Fulkerson successful!
        }
    }
}