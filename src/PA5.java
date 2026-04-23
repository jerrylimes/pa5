import java.util.ArrayList;

public class PA5 {
    /* Construct the graph based on the input numbers */
    public int[][] constructGraph(int c, int r, int t, int p, int proctorCapacity, int[] classSizes, int[] roomSizes, ArrayList<ArrayList<Integer>> proctorAvailability) {
        /* there are a total of numberOfNodes nodes, including source and sink */
        int numberOfNodes = 2 + c + r + t + p;
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

    public String backtrackPath() {
        return "";
    }

    public static void main(String[] args) {
        System.out.println("Hi");
    }
}