//q5

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class NetworkOptimizer extends JFrame {
    // GUI components for user interaction and visualization
    private JPanel graphPanel;                            // Panel to draw the network graph
    private JButton addNodeButton, addEdgeButton,        // Buttons to add nodes, edges, optimize, and find paths
                    optimizeButton, findPathButton;
    private JTextField costField, bandwidthField;        // Text fields for edge cost and bandwidth input
    private JLabel totalCostLabel, latencyLabel;         // Labels to display optimization results

    // Data structures to store the network
    private List<Node> nodes = new ArrayList<>();        // List of nodes (servers or clients)
    private List<Edge> edges = new ArrayList<>();        // List of edges (network connections)
    private Graph graph;                                 // Graph object for optimization algorithms

    // Constructor to set up the GUI
    public NetworkOptimizer() {
        setTitle("Network Optimizer");                   // Set window title
        setSize(800, 600);                              // Set window size to 800x600 pixels
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close application when window is closed
        setLayout(new BorderLayout());                  // Use BorderLayout for component arrangement

        // Initialize graph panel for visualization
        graphPanel = new GraphPanel();                  // Create custom panel for drawing graph
        add(graphPanel, BorderLayout.CENTER);           // Add graph panel to center of window

        // Initialize control panel for user input
        JPanel controlPanel = new JPanel();             // Create panel for buttons and fields
        addNodeButton = new JButton("Add Node");        // Button to add a new node
        addEdgeButton = new JButton("Add Edge");        // Button to add a new edge
        optimizeButton = new JButton("Optimize Network"); // Button to optimize network (MST)
        findPathButton = new JButton("Find Shortest Path"); // Button to find shortest path
        costField = new JTextField(5);                  // Text field for edge cost (5 chars wide)
        bandwidthField = new JTextField(5);             // Text field for edge bandwidth (5 chars wide)
        totalCostLabel = new JLabel("Total Cost: 0");   // Label to show total cost, initially 0
        latencyLabel = new JLabel("Latency: N/A");      // Label to show latency, initially N/A

        // Add components to control panel
        controlPanel.add(addNodeButton);                // Add node button to control panel
        controlPanel.add(addEdgeButton);                // Add edge button to control panel
        controlPanel.add(new JLabel("Cost:"));          // Add cost label before text field
        controlPanel.add(costField);                    // Add cost text field
        controlPanel.add(new JLabel("Bandwidth:"));     // Add bandwidth label before text field
        controlPanel.add(bandwidthField);               // Add bandwidth text field
        controlPanel.add(optimizeButton);               // Add optimize button
        controlPanel.add(findPathButton);               // Add find path button
        controlPanel.add(totalCostLabel);               // Add total cost label
        controlPanel.add(latencyLabel);                 // Add latency label
        add(controlPanel, BorderLayout.SOUTH);          // Add control panel to bottom of window

        // Add action listeners to buttons
        addNodeButton.addActionListener(e -> addNode());      // Call addNode() when button clicked
        addEdgeButton.addActionListener(e -> addEdge());      // Call addEdge() when button clicked
        optimizeButton.addActionListener(e -> optimizeNetwork()); // Call optimizeNetwork() when clicked
        findPathButton.addActionListener(e -> findShortestPath()); // Call findShortestPath() when clicked

        setVisible(true);                               // Make the window visible
    }

    // Node class to represent servers or clients
    static class Node {
        int id;                                         // Unique identifier for the node
        int x, y;                                       // X, Y coordinates for drawing
        Node(int id, int x, int y) {
            this.id = id;                               // Assign node ID
            this.x = x;                                 // Assign X coordinate
            this.y = y;                                 // Assign Y coordinate
        }
    }

    // Edge class to represent network connections
    static class Edge {
        Node src, dest;                                 // Source and destination nodes
        int cost;                                       // Cost of the connection
        int bandwidth;                                  // Bandwidth of the connection
        Edge(Node src, Node dest, int cost, int bandwidth) {
            this.src = src;                             // Assign source node
            this.dest = dest;                           // Assign destination node
            this.cost = cost;                           // Assign cost
            this.bandwidth = bandwidth;                 // Assign bandwidth
        }
    }

    // Custom panel to draw the network graph
    class GraphPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {     // Override to custom draw the graph
            super.paintComponent(g);                    // Call parent method to clear panel
            // Draw all edges
            for (Edge edge : edges) {
                g.setColor(Color.BLACK);                // Set color to black for edges
                g.drawLine(edge.src.x, edge.src.y, edge.dest.x, edge.dest.y); // Draw line between nodes
                // Calculate midpoint for label
                int midX = (edge.src.x + edge.dest.x) / 2; // X coordinate of midpoint
                int midY = (edge.src.y + edge.dest.y) / 2; // Y coordinate of midpoint
                g.drawString("C:" + edge.cost + " B:" + edge.bandwidth, midX, midY); // Draw cost and bandwidth
            }
            // Draw all nodes
            for (Node node : nodes) {
                g.setColor(Color.BLUE);                 // Set color to blue for nodes
                g.fillOval(node.x - 10, node.y - 10, 20, 20); // Draw node as a filled circle
                g.setColor(Color.BLACK);                // Set color to black for text
                g.drawString("Node " + node.id, node.x - 15, node.y - 15); // Label node with ID
            }
        }
    }

    // Method to add a new node
    private void addNode() {
        int id = nodes.size();                          // Assign next available ID
        int x = 50 + (int)(Math.random() * 700);       // Random X position within bounds
        int y = 50 + (int)(Math.random() * 500);       // Random Y position within bounds
        nodes.add(new Node(id, x, y));                 // Add new node to list
        graphPanel.repaint();                          // Redraw graph panel
    }

    // Method to add a new edge
    private void addEdge() {
        if (nodes.size() < 2) {                        // Check if there are enough nodes
            JOptionPane.showMessageDialog(this, "Need at least two nodes to add an edge.");
            return;                                    // Exit if not enough nodes
        }
        String costStr = costField.getText();          // Get cost from text field
        String bandwidthStr = bandwidthField.getText(); // Get bandwidth from text field
        try {
            int cost = Integer.parseInt(costStr);      // Parse cost as integer
            int bandwidth = Integer.parseInt(bandwidthStr); // Parse bandwidth as integer
            // Connect the last two nodes for simplicity
            Node src = nodes.get(nodes.size() - 2);    // Second-to-last node as source
            Node dest = nodes.get(nodes.size() - 1);   // Last node as destination
            edges.add(new Edge(src, dest, cost, bandwidth)); // Add new edge to list
            graphPanel.repaint();                      // Redraw graph panel
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid cost or bandwidth."); // Show error if parsing fails
        }
    }

    // Method to optimize the network (minimize cost with MST)
    private void optimizeNetwork() {
        if (nodes.isEmpty()) return;                   // Exit if no nodes exist
        graph = new Graph(nodes.size());               // Create new graph with number of nodes
        for (Edge edge : edges) {
            graph.addEdge(edge.src.id, edge.dest.id, edge.cost); // Add edges with cost as weight
        }
        int totalCost = graph.kruskalMST();            // Compute MST and get total cost
        totalCostLabel.setText("Total Cost: " + totalCost); // Update total cost label
        latencyLabel.setText("Latency: Optimized");    // Indicate latency is optimized (simplified)
    }

    // Method to find shortest path (minimize latency using bandwidth)
    private void findShortestPath() {
        if (nodes.size() < 2) {                        // Check if there are enough nodes
            JOptionPane.showMessageDialog(this, "Need at least two nodes to find a path.");
            return;                                    // Exit if not enough nodes
        }
        int srcId = nodes.get(0).id;                   // Use first node as source
        int destId = nodes.get(nodes.size() - 1).id;   // Use last node as destination
        graph = new Graph(nodes.size());               // Create new graph with number of nodes
        for (Edge edge : edges) {
            // Use inverse bandwidth as weight (higher bandwidth = lower latency)
            graph.addEdge(edge.src.id, edge.dest.id, 1.0 / edge.bandwidth); // Add edges with latency weight
        }
        double latency = graph.dijkstra(srcId, destId); // Compute shortest path latency
        latencyLabel.setText("Latency: " + latency);   // Update latency label
    }

    // Graph class for optimization algorithms
    static class Graph {
        private int V;                                 // Number of vertices (nodes)
        private List<Edge> edges;                      // List of edges in the graph

        Graph(int V) {
            this.V = V;                                // Initialize number of vertices
            edges = new ArrayList<>();                 // Initialize edge list
        }

        void addEdge(int src, int dest, double weight) { // Add an edge with given weight
            edges.add(new Edge(new Node(src, 0, 0), new Node(dest, 0, 0), (int)weight, 0)); // Add edge
        }

        // Placeholder for Kruskal's MST algorithm
        int kruskalMST() {
            // Simplified: should implement Kruskal's algorithm to find MST based on cost
            return 0;                                  // Return 0 as placeholder
        }

        // Placeholder for Dijkstra's algorithm
        double dijkstra(int src, int dest) {
            // Simplified: should implement Dijkstra's algorithm to find shortest path based on weight
            return 0.0;                                // Return 0.0 as placeholder
        }
    }

    // Main method to launch the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(NetworkOptimizer::new); // Run GUI on Event Dispatch Thread
}
}