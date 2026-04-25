import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class PA5Independent {
    private static int[][] residualGraph;
    private static int c;
    private static int r;
    private static int t;
    private static int p;

    /* Construct the graph based on the input numbers */
    public static int[][] constructGraph(int c, int r, int t, int p, int proctorCapacity, int[] classSizes, int[] roomSizes, ArrayList<ArrayList<Integer>> proctorAvailability) {
        /* there are a total of numberOfNodes nodes, including source and sink */
        int numberOfNodes = 2 + c + r + t + p;
        PA5Independent.c = c;
        PA5Independent.r = r;
        PA5Independent.t = t;
        PA5Independent.p = p;
        int[][] graph = new int[numberOfNodes][numberOfNodes];
        /* source cannot connect to itself; start from the second column */
        for (int i = 1; i < 1 + c; i++) {
            /* source node is connected to every class */
            graph[0][i] = 1;
        }
        /* skip all the columns of classes and rooms and times and the source node; start from proctors */
        for (int i = 1 + c + r + t; i < 1 + c + r + t + p; i++) {
            /* every proctor node is connected to the sink node with the capacity being the proctorCapacity*/
            graph[i][numberOfNodes - 1] = proctorCapacity;
        }
        /* any exam could potentially be at any time, so every room connects to every time */
        for (int i = 1 + c; i < 1 + c + r; i++) {
            for (int j = 1 + c + r; j < 1 + c + r + t; j++) {
                /* for every room i, and every time j */
                graph[i][j] = 1;
            }
        }
        /* can a room hold all the students in a class? let's find out */
        for (int i = 1; i < 1 + c; i++) {
            for (int j = 1 + c; j < 1 + c + r; j++) {
                /* if the size of the class is smaller than the size of the room */
                int classIndex = i - 1;
                int roomIndex = j - (1 + c);
                if (classSizes[classIndex] <= roomSizes[roomIndex]) {
                    graph[i][j] = 1;
                }
            }
        }
        /* are the proctors available at a time? */
        for (int proctorIndex = 0; proctorIndex < p; proctorIndex++) {
            int proctorNode = 1 + c + r + t + proctorIndex;
            for (int time : proctorAvailability.get(proctorIndex)) {
                int availableTime = 1 + c + r + time;
                graph[availableTime][proctorNode] = 1;
            }
        }
        return graph;
    }

    public int bipartiteMatching() {
        return 0;
    }

    /* https://www.geeksforgeeks.org/dsa/ford-fulkerson-algorithm-for-maximum-flow-problem/ */
    public static boolean breadthFirstSearch(int[][] residualGraph, int s, int t, int[] parent) {
        int numberOfNodes = residualGraph.length;
        // Create a visited array and mark all vertices as not visited
        boolean[] visited = new boolean[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            visited[i] = false;
        }
        // Create a queue, enqueue source vertex and mark source vertex as visited
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.add(s);
        visited[s] = true;
        parent[s] = -1;
        while (queue.size() != 0) {
            int u = queue.poll();
            for (int i = 0; i < numberOfNodes; i++) {
                if (visited[i] == false && residualGraph[u][i] > 0) {
                    // If we find a connection to the sink node, then there is no point in BFS anymore, we just have to set its parent and can return true
                    if (i == t) {
                        parent[i] = u;
                        return true;
                    }
                    queue.add(i);
                    parent[i] = u;
                    visited[i] = true;
                }
            }
        }
        // Didn't reach sink in BFS starting from source, so return false
        return false;
    }

    public static int fordFulkerson(int[][] graph, int s, int t) {
        int u, v;
        int numberOfNodes = graph.length;
        // Create a residual graph and fill the residual graph with given capacities in the original graph as residual capacities in residual graph
        // Residual graph where residualGraph[i][j] indicates residual capacity of edge from i to j unless residualGraph[i][j] is 0
        int[][] residualGraph = new int[numberOfNodes][numberOfNodes];
        for (u = 0; u < numberOfNodes; u++) {
            for (v = 0; v < numberOfNodes; v++) {
                residualGraph[u][v] = graph[u][v];
            }
        }
        // This array is filled by BFS and to store path
        int[] parent = new int[numberOfNodes];
        // There is no max flow initially
        int maxFlow = 0;
        // Augment the flow while there is path from source to sink
        while (breadthFirstSearch(residualGraph, s, t, parent)) {
            // Find minimum residual capacity of the edges along the path filled by BFS
            int pathFlow = Integer.MAX_VALUE;
            for (v = t; v != s; v = parent[v]) {
                u = parent[v];
                pathFlow = Math.min(pathFlow, residualGraph[u][v]);
            }
            for (v = t; v != s; v = parent[v]) {
                u = parent[v];
                residualGraph[u][v] -= pathFlow;
                residualGraph[v][u] += pathFlow;
            }
            maxFlow += pathFlow;
        }
        setResidualGraph(residualGraph);
        return maxFlow;
    }

    public static int[][] getResidualGraph() {
        return residualGraph;
    }

    public static void setResidualGraph(int[][] residualGraph) {
        PA5Independent.residualGraph = residualGraph;
    }

    public static String backtrackPath() {
        return "";
    }

    public static void pathFinder(int[][] graph) {

    }

    public static void sinkToClass() {

    }

    public static void classToRoom() {

    }

    public static void roomToTime() {

    }

    public static void timeToProctor() {

    }

    public static void proctorToSink() {

    }

//    public static void main(String[] args) {
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
//            // testing input readability
//            // test successful
//            int[][] graph = constructGraph(c, r, t, p, proctorCapacity, classSizes, roomSizes, proctorAvailability);
//            int numberOfNodes = 2 + c + r + t + p;
//            System.out.println(fordFulkerson(graph, 0, numberOfNodes - 1));
//            // Ford-Fulkerson successful!
//        }
//    }

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
        int[][] graph = constructGraph(hardCodedC, hardCodedR, hardCodedT, hardCodedP, hardCodedProctorCapacity, hardCodedClassSizes, hardCodedRoomSizes, hardCodedProctorAvailability);
        int numberOfNodes = 2 + hardCodedC + hardCodedR + hardCodedT + hardCodedP;
        int maxFlow = fordFulkerson(graph, 0, numberOfNodes - 1);
        System.out.println(maxFlow);
    }
}