/*
 * Code generated by MIMOS tool.
 * Date: 2024/04/02 17:39:25
 * 
 * A* algorithm for 2D path planning
 * - 1. read map
 * - 2. A* algorithm, which includes:
 *    - A Node class used in the graph search
 *    - AStar class, the function "run" is the main function of the A* algorithm
 *    - Run while (!openList.isEmpty() && numExpansions < maxExpansions)
 * 
 * Inputs: the starting state of the robot, which is a couple of ints (x, y)
 * Outputs: the path from the starting state to the goal state, which is a list of ints
 */

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.UnaryOperator;

import javax.swing.plaf.nimbus.State;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;

public class A_Star_2D {
    // Map settings
    String filePath = "D:\\lxx\\thesis\\cases\\lxx\\pp2d\\data\\input-maps\\Boston_0_1024.map";
    String outfilePath = "D:\\lxx\\thesis\\cases\\lxx\\pp2d\\data\\outputs\\path.txt";

    boolean[][] env; // Environment map
    int mapX; // Width of the map
    int mapY; // Height of the map
    int scaleMap = 1;
    int bootTime = 1;

    // Robot settings
    int robotLength = 10, robotWidth = 4; // Numbers in resolution unit
    int scaleRobot = 1;

    // A* settings
    List<Integer> startPos; // Input from the former MIMOS node
    List<Integer> goalPos = Arrays.asList(965, 980); // x= 965, y= 980
    String aStarHeuristicStr = "Euclidean"; // "Euclidean" or "Manhattan" distance
    int numDirections = 8;
    double heuristicWeight = 1.0;
    long maxExpansions = 10_000_000L;
    List<Integer> path;

    // Neighbors
    int[] dX = { -1, -1, -1, 0, 0, 1, 1, 1 };
    int[] dY = { -1, 0, 1, -1, 1, -1, 0, 1 };
    ArrayList<Double> movementCost = new ArrayList<>();
    int numNeighbors = numDirections;

    /* Inputs: */
    double[] input1; // The current state of the robot: such as [x = 0.0, y = 0.0, theta = 0.0]

    /* Outputs: */

    public A_Star_2D() {
        // Scaling the robot size
        robotLength *= scaleRobot;
        robotWidth *= scaleRobot;
        // Calculate movement costs
        for (int i = 0; i < dX.length; i++) {
            int xMove = dX[i];
            int yMove = dY[i];
            movementCost.add(Math.sqrt(Math.abs(xMove) + Math.abs(yMove)));
        }

    }

    /* Node computation: */
    public boolean run() {
        if (bootTime == 1) {
            readMap(filePath, scaleMap);
            bootTime = 0;

            System.out.println("\nMap size: x: " + mapX + ", y: " + mapY);
        }
        

        if ((input1 != null && input1.length != 0)) {
            startPos = Arrays.asList((int) input1[0], (int) input1[1]);
            System.out.println("\nStarting state: (x: " + startPos.get(0) + ", y: " + startPos.get(1) + ") -> Goal state: (x: "
                    + goalPos.get(0) + ", y: " + goalPos.get(1) + ")");

            AStar aStar = new AStar(numNeighbors, startPos, goalPos, robotLength, robotWidth, aStarHeuristicStr,
                    heuristicWeight);
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

    /*
     * Read map file:
     * Map Format of Moving AI map:
     * type octile
     * height 1024
     * width 1024
     * map
     * ............
     */

    public void readMap(String fileName, int scale) {
        try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
            String line;
            int pos;

            // UnaryOperator<String> removeCR = str -> {
            //     if (!str.isEmpty() && str.charAt(str.length() - 1) == '\r') {
            //         return str.substring(0, str.length() - 1);
            //     }
            //     return str;
            // };

            line = file.readLine();
            //removeCR.apply(line);

            line = file.readLine();
            //line = removeCR.apply(file.readLine());
            pos = line.indexOf(' ');
            mapY = Integer.parseInt(line.substring(pos + 1));

            line = file.readLine();
            //line = removeCR.apply(file.readLine());
            pos = line.indexOf(' ');
            mapX = Integer.parseInt(line.substring(pos + 1));

            line = file.readLine();
            //removeCR.apply(file.readLine());

            env = new boolean[scale * mapY][scale * mapX];

            for (int y = 0; y < mapY; y++) {
                line = file.readLine();
                //line = removeCR.apply(file.readLine());

                for (int x = 0; x < mapX; x++) {
                    char point = line.charAt(x);
                    boolean obstacle = point == '@';

                    int baseY = scale * y;
                    int baseX = scale * x;
                    for (int sy = 0; sy < scale; sy++) {
                        for (int sx = 0; sx < scale; sx++) {
                            env[baseY + sy][baseX + sx] = obstacle;
                        }
                    }
                }
            }

            mapX *= scale;
            mapY *= scale;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read the map file.");
        }
    }

    public class AStar {
        // Node class for A* algorithm
        class Node implements Comparable<Node> {
            List<Integer> state; // X and Y in 2D path planning
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
        private int robotLength;
        private int robotWidth;
        private String aStarHeuristicStr;
        private double heuristicWeight;
        private double execTime;

        public AStar(int numNeighbors, List<Integer> initialState, List<Integer> goalState, int robotLength,
                int robotWidth, String aStarHeuristicStr, double heuristicWeight) {
            this.numNeighbors = numNeighbors;
            this.initialState = initialState;
            this.goalState = goalState;
            this.robotLength = robotLength;
            this.robotWidth = robotWidth;
            this.aStarHeuristicStr = aStarHeuristicStr;
            this.heuristicWeight = heuristicWeight;
        }

        public List<Integer> run(long maxExpansions) {
            PriorityQueue<Node> openList = new PriorityQueue<>();
            Set<List<Integer>> visited = new HashSet<>(); // The set of explored vertices
            Map<List<Integer>, Double> gVals = new HashMap<>(); // Key: state, Value: g value?

            Node startNode = new Node(initialState, 0, heuristicWeight * getH(initialState), null, -1);
            openList.add(startNode);

            long startTime = System.nanoTime();
            long numExpansions = 0;
            while (!openList.isEmpty() && numExpansions < maxExpansions) {
                Node expNode = openList.poll();

                if (visited.contains(expNode.state))
                    continue;
                visited.add(expNode.state);

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
            for (int i = 0; i <= robotWidth; i++) {
                for (int j = 0; j <= robotLength; j++) {
                    int x = s.get(0) + j;
                    int y = s.get(1) + i;

                    if (env[y][x]) {
                        return true;
                    }
                }
            }
            return false;
        }

        // Check if the given postion is within the map bounds and not an obstacle
        public boolean isValid(List<Integer> s) {
            for (int i = 0; i <= robotWidth; i++) {
                for (int j = 0; j <= robotLength; j++) {
                    int x = s.get(0) + j;
                    int y = s.get(1) + i;

                    if (x < 0 || x >= mapX || y < 0 || y >= mapY) {
                        return false; // Sticks out of the map
                    }
                }
            }
            return !isObstacle(s);
        }

    }

}
