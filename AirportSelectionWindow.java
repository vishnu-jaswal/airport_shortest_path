package MiniProject;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AirportSelectionWindow extends JFrame {
    private JComboBox<String> startComboBox;
    private JComboBox<String> endComboBox;
    private Map<Integer, Map<Integer, Integer>> graph;

    // Constructor for the Airport Selection Window
    public AirportSelectionWindow() {
        setTitle("Select Airports");
        setSize(1000, 600); // Adjust size as needed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create the graph using real-world airports and distances
        graph = createRealAirportGraph();

        // Define real airport names
        String[] airports = {
                "DED (Dehradun)",
                "NNS (Pithoragarh)",
                "DHM (Kangra-Gaggal)",
                "GYA (GYA Airport)",
                "AMD (SVP International Airport)",
                "DEL (Delhi International Airport)",
                "BHO (Bhopal Airport)",
                "GOI (Dabolim Airport)",
                "GAU (Guwahati Airport)", // New Airport
                "PEK (Beijing Capital International Airport)" // New Airport
        };

        // Create a custom panel with a background image
        BackgroundPanel backgroundPanel = new BackgroundPanel("air.jpg"); // Path to your background image
        backgroundPanel.setLayout(new GridBagLayout()); // Using GridBagLayout to center components

        // Show airport index mapping
        showAirportIndexMapping(airports);

        startComboBox = new JComboBox<>(airports);
        endComboBox = new JComboBox<>(airports);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            int startAirport = startComboBox.getSelectedIndex();
            int endAirport = endComboBox.getSelectedIndex();
            if (startAirport == endAirport) {
                JOptionPane.showMessageDialog(this, "Take-off and landing airports cannot be the same.");
            } else {
                runSimulation(startAirport, endAirport);
            }
        });

        // Add components to the panel
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false); // Make panel transparent to see the background image
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Set padding

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Select Take-off Airport:"), gbc);

        gbc.gridx = 1;
        formPanel.add(startComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Select Landing Airport:"), gbc);

        gbc.gridx = 1;
        formPanel.add(endComboBox, gbc);

        gbc.gridy = 2;
        formPanel.add(submitButton, gbc);

        backgroundPanel.add(formPanel, gbc);

        // Add the background panel to the frame
        add(backgroundPanel);

        setVisible(true);
    }

    // Custom panel class to display a background image
    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        // Constructor to load the background image
        public BackgroundPanel(String imagePath) {
            try {
                backgroundImage = new ImageIcon(imagePath).getImage();
            } catch (Exception e) {
                System.out.println("Error loading background image: " + e.getMessage());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this); // Draw the image to fill the panel
            }
        }
    }

    // Function to show airport index mapping
    private void showAirportIndexMapping(String[] airports) {
        StringBuilder mappingInfo = new StringBuilder("Airport Index Mapping:\n\n");
        for (int i = 0; i < airports.length; i++) {
            mappingInfo.append("Index ").append(i).append(": ").append(airports[i]).append("\n");
        }
        JOptionPane.showMessageDialog(this, mappingInfo.toString(), "Airport Index Information", JOptionPane.INFORMATION_MESSAGE);
    }

    // Function to create a graph with real airports and distances
    private Map<Integer, Map<Integer, Integer>> createRealAirportGraph() {
        Map<Integer, Map<Integer, Integer>> graph = new HashMap<>();

        // Define distances in km (including PEK)
        int[][] distances = {
                {0, 226, 260, 921, 975, 2132, 787, 1717, 1419, 2345}, // DED
                {226, 0, 469, 712, 1054, 2147, 756, 1706, 1519, 4537}, // NNS
                {260, 469, 0, 1158, 1073, 2138, 989, 1881, 1529, 3456}, // DHM
                {921, 712, 1158, 0, 1278, 1791, 789, 1552, 693, 4536}, // GYA
                {975, 1054, 1073, 1278, 0, 1047, 592, 933, 1462, 4532}, // AMD
                {2132, 2147, 2138, 1791, 1047, 0, 1346, 491, 2254, 3456}, // DEL
                {787, 756, 989, 789, 592, 1346, 0, 950, 1418, 5364}, // BHO
                {1717, 1706, 1881, 1552, 933, 491, 950, 0, 2189, 5121}, // GOI
                {1419, 1478, 1529, 693, 1462, 2354, 1418, 2189, 0, 4800}, // GAU
                {2345, 4537, 3456, 4536, 4532, 3456, 5364, 4765, 4800, 0 } // PEK
        };

        // Initialize the graph with real distances
        for (int i = 0; i < distances.length; i++) {
            graph.put(i, new HashMap<>());
            for (int j = 0; j < distances[i].length; j++) {
                if (i != j) {
                    graph.get(i).put(j, distances[i][j]);
                }
            }
        }

        return graph;
    }

    // Method to generate random distance between 4800 and 5800
    private int randDistance() {
        Random rand = new Random();
        return rand.nextInt(1000) + 4800; // Generate random distance between 4800 and 5800
    }

    // Method to run the air traffic control simulation
    private void runSimulation(int startAirport, int endAirport) {
        int actualDistance = graph.get(startAirport).get(endAirport); // Get actual distance from matrix
        Result result = dijkstra(graph, startAirport, endAirport);

        if (result.path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No available path between selected airports.");
        } else {
            String route = result.path.toString(); // Route as a list of node indices
            StringBuilder namedRoute = new StringBuilder();
            for (int airport : result.path) {
                namedRoute.append(getAirportName(airport)).append(" -> ");
            }
            namedRoute.setLength(namedRoute.length() - 4); // Remove trailing arrow

            double flightTime = calculateFlightTime(result.distance);
            double fuelConsumption = calculateFuelConsumption(result.distance);

            JOptionPane.showMessageDialog(this, "Best Route (Node indices): " + route + "\n"
                    + "Best Route (Airports): " + namedRoute + "\n"
                    + "Actual Distance: " + actualDistance + " km\n"
                    + "Shortest Distance (after Dijkstra): " + result.distance + " km\n"
                    + "Flight Time: " + String.format("%.2f", flightTime) + " hours\n"
                    + "Fuel Consumption: " + String.format("%.2f", fuelConsumption) + " liters\n"
                    + "Traffic Status: " + getTrafficStatus());
        }
    }

    // Helper method to get airport name based on index
    private String getAirportName(int index) {
        String[] airports = {
                "DED (Dehradun)",
                "NNS (Pithoragarh)",
                "DHM (Kangra-Gaggal)",
                "GYA (GYA Airport)",
                "AMD (SVP International Airport)",
                "DEL (Delhi International Airport)",
                "BHO (Bhopal Airport)",
                "GOI (Dabolim Airport)",
                "GAU (Guwahati Airport)",
                "PEK (Beijing Capital International Airport)"
        };
        return airports[index];
    }

    // Calculate flight time based on distance
    private double calculateFlightTime(double distance) {
        double averageSpeed = 800; // Average speed in km/h
        return distance / averageSpeed;
    }

    // Calculate fuel consumption based on distance
    private double calculateFuelConsumption(double distance) {
        double fuelConsumptionRate = 20; // Rate in liters/km
        return distance * fuelConsumptionRate;
    }

    // Get random traffic status
    private String getTrafficStatus() {
        String[] statuses = {"Light", "Moderate", "Heavy"};
        Random rand = new Random();
        return statuses[rand.nextInt(statuses.length)];
    }

    // Dijkstra's algorithm implementation
    private Result dijkstra(Map<Integer, Map<Integer, Integer>> graph, int start, int end) {
        int numAirports = graph.size();
        double[] distances = new double[numAirports];
        boolean[] visited = new boolean[numAirports];
        int[] previous = new int[numAirports];
        Arrays.fill(distances, Double.POSITIVE_INFINITY);
        Arrays.fill(previous, -1); // Initialize previous to -1 to indicate no predecessor
        distances[start] = 0;

        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(node -> node.distance));
        queue.add(new Node(start, 0));

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (visited[current.id]) continue;
            visited[current.id] = true;

            if (current.id == end) break; // Early exit if we reached the end airport

            for (Map.Entry<Integer, Integer> neighbor : graph.get(current.id).entrySet()) {
                int neighborId = neighbor.getKey();
                double newDist = distances[current.id] + neighbor.getValue();
                if (newDist < distances[neighborId]) {
                    distances[neighborId] = newDist;
                    previous[neighborId] = current.id;
                    queue.add(new Node(neighborId, newDist));
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        for (int at = end; at != -1; at = previous[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return new Result(path, distances[end]);
    }

    // Result class to hold the path and distance
    private static class Result {
        List<Integer> path;
        double distance;

        public Result(List<Integer> path, double distance) {
            this.path = path;
            this.distance = distance;
        }
    }

    // Node class for Dijkstra's algorithm
    private static class Node {
        int id;
        double distance;

        public Node(int id, double distance) {
            this.id = id;
            this.distance = distance;
        }
    }

    // Main method to run the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(AirportSelectionWindow::new);
    }
}