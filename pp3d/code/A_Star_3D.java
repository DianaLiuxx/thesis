/*
 * Code generated by MIMOS tool.
 * Date: 2024/04/08 09:22:37
 */

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;

public class A_Star_3D {
    // Map settings
    String filePath = "D:\\lxx\\thesis\\cases\\lxx\\pp3d\\data\\input-maps\\freiburg.map";
    String outfilePath = "D:\\lxx\\thesis\\cases\\lxx\\pp3d\\data\\outputs\\path.txt";

    // A* settings
    List<Integer> startPos; // Input from the former MIMOS node
    List<Integer> goalPos = Arrays.asList(40, 5, 6);
    String aStarHeuristicStr = "Euclidean"; // "Euclidean" or "Manhattan" distance
    double heuristicWeight = 1.0;
    long maxExpansions = 100_000_000L;
    List<Integer> path;

    // Neighbors
    int[] dX = { -1, 1, 0, 0, 0, 0 };
    int[] dY = { 0, 0, -1, 1, 0, 0 };
    int[] dZ = { 0, 0, 0, 0, -1, 1 };
    ArrayList<Double> movementCost = new ArrayList<>();
    int numNeighbors = dX.length;

    /* Inputs: */
    double[] input1;

    /* Outputs: */

    private int minX, maxX;
    private int minY, maxY;
    private int minZ, maxZ;
    private boolean[][][] occGrid;
    int bootTime = 1;

    public A_Star_3D() {
        // Calculate movement costs
        for (int i = 0; i < dX.length; i++) {
            int xMove = dX[i];
            int yMove = dY[i];
            int zMove = dZ[i];
            movementCost.add(Math.sqrt(Math.abs(xMove) + Math.abs(yMove) + Math.abs(zMove)));
        }

    }

    /* Node computation: */
    public boolean run() {
        if (bootTime == 1) {
            readMap(filePath);
            bootTime = 0;

            System.out.println("minX: " + minX + " maxX: " + maxX);
            System.out.println("minY: " + minY + " maxY: " + maxY);
            System.out.println("minZ: " + minZ + " maxZ: " + maxZ);
            System.out.println("occGrid: " + occGrid.length + " " + occGrid[0].length + " " + occGrid[0][0].length);
        }

        if ((input1 != null && input1.length != 0)) {
            startPos = Arrays.asList((int) input1[0], (int) input1[1], (int) input1[2]);
            System.out.println(
                    "\nStarting state: (x: " + startPos.get(0) + ", y: " + startPos.get(1) + ", z: " + startPos.get(2)
                            + ") -> Goal state: (x: " + goalPos.get(0) + ", y: " + goalPos.get(1) + ", z: "
                            + goalPos.get(2) + ")");
            AStar aStar = new AStar(numNeighbors, startPos, goalPos, aStarHeuristicStr, heuristicWeight);
            path = aStar.run(maxExpansions);

            // Output the path
            try (PrintWriter writer = new PrintWriter(new FileWriter(outfilePath, StandardCharsets.UTF_8))) {
                for (int value : path) {
                    writer.println(value);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /* Setters (for inputs): */
    public void set_input1(double[] i) {
        input1 = i;
    }

    /* Getters (for outputs): */

    public void readMap(String fileName) {
        try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
            // Read X range
            String line = file.readLine();
            String[] xTokens = line.split("\\s+");
            minX = Integer.parseInt(xTokens[1]);
            maxX = Integer.parseInt(xTokens[2]);

            // Read Y range
            line = file.readLine();
            String[] yTokens = line.split("\\s+");
            minY = Integer.parseInt(yTokens[1]);
            maxY = Integer.parseInt(yTokens[2]);

            // Read Z range
            line = file.readLine();
            String[] zTokens = line.split("\\s+");
            minZ = Integer.parseInt(zTokens[1]);
            maxZ = Integer.parseInt(zTokens[2]);

            int xS = maxX - minX + 1;
            int yS = maxY - minY + 1;
            int zS = maxZ - minZ + 1;
            occGrid = new boolean[xS][yS][zS];

            // Occupied grids
            while ((line = file.readLine()) != null && !line.isEmpty()) {
                String[] parts = line.split("\\s+");
                int x = Integer.parseInt(parts[0]) - minX;
                int y = Integer.parseInt(parts[1]) - minY;
                int z = Integer.parseInt(parts[2]) - minZ;
                occGrid[x][y][z] = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read the map file.");
        }
    }

    public class AStar {
        // Node class for A* algorithm
        class Node implements Comparable<Node> {
            List<Integer> state; // X, Y, Z in 3D path planning
            double g;
            double f; // f = g + h * weight
            Node parent; // Parent node
            int dir;

            Node(List<Integer> state, double g, double f, Node parent, int dir) {
                this.state = state;
                this.g = g;
                this.f = f;
                this.parent = parent;
                this.dir = dir;
            }

            @Override
            public int compareTo(Node other) {
                return Double.compare(this.f, other.f);
            }
        }

        private int numNeighbors;
        private List<Integer> initialState;
        private List<Integer> goalState;
        private String aStarHeuristicStr;
        private double heuristicWeight;
        private double execTime;

        public AStar(int numNeighbors, List<Integer> initialState, List<Integer> goalState, String aStarHeuristicStr,
                double heuristicWeight) {
            this.numNeighbors = numNeighbors;
            this.initialState = initialState;
            this.goalState = goalState;
            this.aStarHeuristicStr = aStarHeuristicStr;
            this.heuristicWeight = heuristicWeight;
        }

        public List<Integer> run(long maxExpansions) {
            PriorityQueue<Node> openList = new PriorityQueue<>();
            Set<List<Integer>> visited = new HashSet<>(); // The set of explored vertices
            Map<List<Integer>, Double> gVals = new HashMap<>(); // Key: state, Value: g value

            Node startNode = new Node(initialState, 0, heuristicWeight * getH(initialState), null, -1);
            openList.add(startNode);

            long startTime = System.nanoTime();
            long numExpansions = 0;
            while (!openList.isEmpty() && numExpansions < maxExpansions) {
                Node expNode = openList.poll();

                if (visited.contains(expNode.state))
                    continue;
                visited.add(expNode.state);

                // System.out.println("======");
                // visited.stream()
                //         .limit(10)
                //         .forEach(System.out::println);

                numExpansions++;

                if (isGoal(expNode.state)) {
                    execTime = (System.nanoTime() - startTime) / 1e9;
                    return getPath(expNode); // When arrived at the goal, return the path
                }

                List<List<Integer>> neighbors = getNeighbors(expNode.state);
                for (int dir = 0; dir < neighbors.size(); dir++) {
                    List<Integer> neighborState = neighbors.get(dir);
                    if (visited.contains(neighborState))
                        continue;

                    if (isValid(neighborState)) {
                        double g = getG(expNode.state, neighborState, expNode.g, dir);
                        double f = g + heuristicWeight * getH(neighborState);


                        if (!gVals.containsKey(neighborState) || g < gVals.get(neighborState)) {
                            gVals.put(neighborState, g);
                            openList.add(new Node(neighborState, g, f, expNode, dir));
                            System.out.println("dir: "+ dir);
                            System.out.println("neighborState: "+ neighborState);
                            System.out.println("g: "+ g + " f: "+ f);
                            
                        }
                    }
                }
            }

            execTime = (System.nanoTime() - startTime) / 1e9;
            return new ArrayList<Integer>();

        }

        public double getExecTime() {
            return execTime;
        }

        private List<Integer> getPath(Node n) {
            List<Integer> path = new ArrayList<>();
            while (n != null) {
                path.add(n.dir);
                n = n.parent;
            }
            path.remove(path.size() - 1); // Remove the direction of the start node
            Collections.reverse(path);
            return path;
        }

        private boolean isGoal(List<Integer> s) {
            return s.equals(goalState);
        }

        public List<List<Integer>> getNeighbors(List<Integer> s) {
            List<List<Integer>> neighbors = new ArrayList<>();
            for (int i = 0; i < numNeighbors; i++) {
                List<Integer> neighbor = new ArrayList<>();
                neighbor.add(s.get(0) + dX[i]); // X coordinate
                neighbor.add(s.get(1) + dY[i]); // Y coordinate
                neighbor.add(s.get(2) + dZ[i]); // Z coordinate
                neighbors.add(neighbor);
            }
            return neighbors;
        }

        public double getG(List<Integer> prevS, List<Integer> currS, double prevG, int dir) {
            // Assuming isValid method checks if a given STATE is within bounds and not an
            // obstacle
            assert isValid(prevS);
            assert isValid(currS);
            // assert dir >= 0 && dir < numNeighbors;

            return prevG + movementCost.get(dir);
        }

        double getH(List<Integer> s) {
            double dist = 0;
            if (aStarHeuristicStr.toLowerCase().equals("none")) {
                return 0;
            } else if (aStarHeuristicStr.toLowerCase().equals("euclidean")) {
                for (int i = 0; i < s.size(); i++) {
                    dist += Math.pow(s.get(i) - goalPos.get(i), 2);
                }
                return Math.sqrt(dist);
            } else if (aStarHeuristicStr.toLowerCase().equals("manhattan")) {
                for (int i = 0; i < s.size(); i++) {
                    dist += Math.abs(s.get(i) - goalPos.get(i));
                }
                return dist;
            } else {
                System.out.println("Invalid heuristic type: " + aStarHeuristicStr);
            }
            return dist;
        }

        public boolean isObstacle(List<Integer> s) {
            int x = s.get(0);
            int y = s.get(1);
            int z = s.get(2);

            return occGrid[x - minX][y - minY][z - minZ];
        }

        // Check if the given postion is within the map bounds and not an obstacle
        public boolean isValid(List<Integer> s) {
            int x = s.get(0);
            int y = s.get(1);
            int z = s.get(2);

            if (x <= minX || x >= maxX || y <= minY || y >= maxY || z <= minZ || z >= maxZ) {
                return false;
            }

            return !isObstacle(s);
        }

    }
}
