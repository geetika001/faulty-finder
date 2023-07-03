import java.io.*;

public class Main {
    public static void main(String[] args) {
        Circuit circuit = new Circuit();


        CircuitFileReader reader = new CircuitFileReader("circuit_file.txt",circuit );
        circuit = reader.readCircuitFile("circuit_file.txt");

        FaultSimulator simulator = new FaultSimulator(circuit);
        int[] inputVector = simulator.determineInputVector("net_f", FaultType.SA0);

        int output = circuit.evaluate(inputVector);

        OutputWriter writer = new OutputWriter();
        writer.writeOutputFile(inputVector, output);
    }

    static class Circuit {
        private Node[] nodes;
        private int numNodes;

        public Circuit() {
            this.numNodes = numNodes;
            this.nodes = new Node[numNodes];
        }

        public void addNode(Node node) {
            nodes[node.getIndex()] = node;
        }

        public Node getNodeByName(String name) {
            for (Node node : nodes) {
                if (node.getName().equals(name)) {
                    return node;
                }
            }
            return null;
        }

        public void reset() {
            for (Node node : nodes) {
                node.reset();
            }
        }

        public int evaluate(int[] inputVector) {
            for (Node node : nodes) {
                node.evaluate(inputVector);
            }
            return nodes[nodes.length - 1].getValue();
        }
    }

    static class Node {
        private String name;
        private Gate gate;
        private Node[] inputNodes;
        private int value;
        private int index;

        public Node(String name, Gate gate, Node[] inputNodes, int index) {
            this.name = name;
            this.gate = gate;
            this.inputNodes = inputNodes;
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public void reset() {
            value = -1;
        }

        public void evaluate(int[] inputVector) {
            if (value != -1) {
                return; // Node value already evaluated
            }

            if (gate == Gate.INPUT) {
                int nodeIndex = Integer.parseInt(name.substring(name.length() - 1));
                value = inputVector[nodeIndex - 1];
            } else {
                int[] inputValues = new int[inputNodes.length];
                for (int i = 0; i < inputNodes.length; i++) {
                    inputNodes[i].evaluate(inputVector);
                    inputValues[i] = inputNodes[i].getValue();
                }

                switch (gate) {
                    case AND:
                        value = LogicUtil.and(inputValues);
                        break;
                    case OR:
                        value = LogicUtil.or(inputValues);
                        break;
                    case NOT:
                        value = LogicUtil.not(inputValues[0]);
                        break;
                    case XOR:
                        value = LogicUtil.xor(inputValues);
                        break;
                }
            }
        }

        public int getValue() {
            return value;
        }
    }

    static class CircuitFileReader {
        private final String filename;
        private final Circuit circuit;
        public CircuitFileReader(String filename,Circuit circuit){
            this.filename=filename;
            this.circuit=circuit;
        }
        public Circuit readCircuitFile(String filename) {
            Circuit circuit = new Circuit(); // Instantiate the Circuit object

            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader("circuit_file.txt"));

                String line;
                int numNodes = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.startsWith("Z = ")) {
                        // Parse output node line
                        String[] parts = line.split(" ");
                        String outputNodeName = parts[2];
                        circuit.addNode(new Node(outputNodeName, Gate.INPUT, null, numNodes));
                    } else if (line.contains(" = ")) {
                        // Parse internal node line
                        String[] parts = line.split(" = ");
                        if (parts.length == 2) {
                            String nodeName = parts[0];
                            String expression = parts[1];
                            Node[] inputNodes = parseInternalNode(expression, circuit);
                            circuit.addNode(new Node(nodeName, getGate(expression), inputNodes, numNodes));
                        } else {
                            System.out.println("Invalid line format: " + line);
                        }
                    } else {
                        System.out.println("Invalid line format: " + line);
                    }

                    numNodes++;
                }

                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return circuit;
        }


        private Gate getGate(String expression) {
            if (expression.contains("&")) {
                return Gate.AND;
            } else if (expression.contains("|")) {
                return Gate.OR;
            } else if (expression.contains("~")) {
                return Gate.NOT;
            } else if (expression.contains("^")) {
                return Gate.XOR;
            } else {
                return Gate.INPUT;
            }
        }

        private Node[] parseInternalNode(String expression, Circuit circuit) {
            String[] tokens = expression.split(" ");
            Node[] inputNodes = new Node[tokens.length - 1];

            for (int i = 0; i < inputNodes.length; i++) {
                String inputNodeName = tokens[i];
                inputNodes[i] = circuit.getNodeByName(inputNodeName);
            }

            return inputNodes;
        }
    }

    static class FaultSimulator {
        private Circuit circuit;

        public FaultSimulator(Circuit circuit) {
            this.circuit = circuit;
        }

        public int[] determineInputVector(String faultNodeName, FaultType faultType) {
            Node faultNode = circuit.getNodeByName(faultNodeName);
            circuit.reset();
            int[] inputVector = new int[4];

            // Try all possible input vectors until fault is detected
            while (true) {
                int output = circuit.evaluate(inputVector);
                if (faultType == FaultType.SA0 && output == 1) {
                    break;
                } else if (faultType == FaultType.SA1 && output == 0) {
                    break;
                }

                // Increment input vector
                int index = 3;
                while (index >= 0) {
                    inputVector[index]++;
                    if (inputVector[index] <= 1) {
                        break;
                    } else {
                        inputVector[index] = 0;
                        index--;
                    }
                }

                if (index < 0) {
                    break; // All input vectors tested
                }
            }

            return inputVector;
        }
    }

    static class OutputWriter {
        private String outputFile = "output.txt";

        public void writeOutputFile(int[] inputVector, int output) {
            try {
                FileWriter writer = new FileWriter(outputFile);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);

                bufferedWriter.write("[A, B, C, D] = " + inputVectorToString(inputVector) + ", Z = " + output);

                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String inputVectorToString(int[] inputVector) {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            for (int i = 0; i < inputVector.length; i++) {
                builder.append(inputVector[i]);
                if (i < inputVector.length - 1) {
                    builder.append(", ");
                }
            }
            builder.append("]");
            return builder.toString();
        }
    }

    static class LogicUtil {
        public static int and(int... inputs) {
            int result = inputs[0];
            for (int i = 1; i < inputs.length; i++) {
                result &= inputs[i];
            }
            return result;
        }

        public static int or(int... inputs) {
            int result = inputs[0];
            for (int i = 1; i < inputs.length; i++) {
                result |= inputs[i];
            }
            return result;
        }

        public static int not(int input) {
            return input == 0 ? 1 : 0;
        }

        public static int xor(int... inputs) {
            int result = inputs[0];
            for (int i = 1; i < inputs.length; i++) {
                result ^= inputs[i];
            }
            return result;
        }
    }

    enum Gate {
        AND, OR, NOT, XOR, INPUT
    }

    enum FaultType {
        SA0, SA1
    }
}
