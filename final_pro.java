package circuit;
import java.io.*;
import java.util.*;

public class final_pro {
    public static void main(String[] args) {
        Circuit circuit = readCircuitFromFile("circuit_file.txt");

        // Find the input vector for fault at net_f
        int[] inputVector = findInputVectorForFault(circuit, "net_f");

        // Output the input vector and Z value
        System.out.print("[A, B, C, D] = ");
        System.out.print(Arrays.toString(inputVector));
        System.out.println(", Z = " + circuit.evaluate(inputVector));
    }

    public static Circuit readCircuitFromFile(String filename) {
        Circuit circuit = new Circuit();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                String outputNodeName = parts[0].trim();
                String expression = parts[1].trim();
                circuit.addNode(outputNodeName, expression);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return circuit;
    }

    public static int[] findInputVectorForFault(Circuit circuit, String faultNodeName) {
        circuit.reset();
        Node faultNode = circuit.getNodeByName(faultNodeName);

        // Set the input values to all zeros
        int[] inputVector = new int[4];
        circuit.setInputValuesToZeros(inputVector);

        // Evaluate the circuit
        int output = circuit.evaluate(inputVector);

        // If the output is not 0, there is a stuck-at-0 fault
        if (output == 0) {
            return inputVector;
        }

        // Increment the input vector until the fault is detected
        while (true) {
            incrementInputVector(inputVector);

            // Evaluate the circuit
            output = circuit.evaluate(inputVector);

            // If the output is not 0, the fault is detected
            if (output != 0) {
                return inputVector;
            }
        }
    }

    public static void writeOutputToFile(int[] inputVector, int output) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            writer.write("[A, B, C, D] = " + Arrays.toString(inputVector) + ", Z = " + output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void incrementInputVector(int[] inputVector) {
        for (int i = inputVector.length - 1; i >= 0; i--) {
            if (inputVector[i] == 0) {
                inputVector[i] = 1;
                return;
            } else {
                inputVector[i] = 0;
            }
        }
    }

    static class Circuit {
        private Map<String, Node> nodes;
        private List<Node> inputNodes;

        public Circuit() {
            nodes = new HashMap<>();
            inputNodes = new ArrayList<>();
        }

        public void addNode(String nodeName, String expression) {
            Node node = new Node(nodeName);
            nodes.put(nodeName, node);

            if (nodeName.startsWith("net_")) {
                node.setGateType(GateType.fromString(expression));
                node.addInputNodes(getInputNodesFromExpression(expression));
            } else if (nodeName.equals("Z")) {
                node.addInputNodes(getInputNodesFromExpression(expression));
            } else {
                inputNodes.add(node);
            }
        }

        private List<Node> getInputNodesFromExpression(String expression) {
            List<Node> inputNodes = new ArrayList<>();
            String[] nodeNames = expression.split("\\s*[&|^~]\\s*");
            for (String nodeName : nodeNames) {
                inputNodes.add(nodes.get(nodeName.trim()));
            }
            return inputNodes;
        }

        public Node getNodeByName(String nodeName) {
            return nodes.get(nodeName);
        }

        public void setInputValuesToZeros(int[] inputVector) {
            for (Node inputNode : inputNodes) {
                String name = inputNode.getName();
                int index = Integer.parseInt(name.substring(name.length() - 1));
                inputNode.setValue(inputVector[index - 1]);
            }
        }

        public int evaluate(int[] inputVector) {
            setInputValuesToZeros(inputVector);

            for (Node inputNode : inputNodes) {
                inputNode.evaluate();
            }

            Node outputNode = getNodeByName("Z");
            return outputNode.getValue();
        }

        public void reset() {
            for (Node node : nodes.values()) {
                node.reset();
            }
        }
    }

    static class Node {
        private String name;
        private List<Node> inputNodes;
        private int value;
        private GateType gateType;

        public Node(String name) {
            this.name = name;
            this.inputNodes = new ArrayList<>();
            this.value = -1; // Uninitialized value
            this.gateType = GateType.UNKNOWN; // Uninitialized gate type
        }

        public String getName() {
            return name;
        }

        public List<Node> getInputNodes() {
            return inputNodes;
        }

        public void addInputNodes(List<Node> inputNodes) {
            this.inputNodes.addAll(inputNodes);
        }

        public GateType getGateType() {
            return gateType;
        }

        public void setGateType(GateType gateType) {
            this.gateType = gateType;
        }

        public int evaluate() {
            if (value != -1) {
                return value; // Node value already evaluated
            }

            // Evaluate input nodes recursively
            for (Node inputNode : inputNodes) {
                inputNode.evaluate();
            }

            // Perform the logical operation based on the node's gate type
            int inputValue1 = inputNodes.isEmpty() ? 0 : inputNodes.get(0).getValue(); // Assuming single-input gates
            int inputValue2 = inputNodes.size() < 2 ? 0 : inputNodes.get(1).getValue(); // Assuming at most two-input gates

            switch (gateType) {
                case AND:
                    value = inputValue1 & inputValue2;
                    break;
                case OR:
                    value = inputValue1 | inputValue2;
                    break;
                case NOT:
                    value = ~inputValue1;
                    break;
                case XOR:
                    value = inputValue1 ^ inputValue2;
                    break;
                case UNKNOWN:
                default:
                    value = inputValue1;
                    break;
            }

            return value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public void reset() {
            value = -1;
        }
    }

    enum GateType {
        AND("&"),
        OR("|"),
        NOT("~"),
        XOR("^"),
        UNKNOWN("");

        private final String symbol;

        GateType(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public static GateType fromString(String symbol) {
            for (GateType gateType : values()) {
                if (gateType.symbol.equals(symbol)) {
                    return gateType;
                }
            }
            return UNKNOWN;
        }
    }
}
