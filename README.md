# Faulty-Finder
## Problem Statement: 
The problem is to design an algorithm and write code to identify the input vector required to detect a fault at a specific node in a given circuit. The circuit represents manufactured chips that may have faults in their structure at certain nodes and must be tested before they are delivered to end users. The circuit file, fault node location, and fault type (SA0 or SA1) are all included in the input. The algorithm should be efficient, robust, and capable of detecting faults quickly.

## Solution:
The solution entails implementing a Java program. It begins by defining the circuit structure, which includes nodes and their expressions. The circuit is read from a file. The fault node and type of fault are identified. The fault is then simulated by determining the input vector required to test the fault. Setting the fault node to the desired value (0 for SA0 and 1 for SA1) while keeping the other inputs as zeros gives the input vector. Finally, the input vector and expected output are saved to a file. Each class in the program's code is responsible for a specific task in the fault identification process, such as Circuit, Node, Gate, CircuitFileReader, FaultSimulator, and OutputWriter.

## Approach:

1. **Reading the Circuit File:**
   - Create a Circuit object to store the circuit's nodes.
   - Open the circuit file for reading.
   - Read each line from the file.
   - If the line starts with "Z =", parse the output node and add it to the Circuit as an input node.
   - If the line follows the format "node_name = expression", parse the internal node and add it to the Circuit.
   - Close the file.

2. **Identify the Fault Location and Type:**
   - Obtain the fault location (node name) and type (SA0 or SA1) from the input.

3. **Find the Input Vector for the Fault:**
   - Create a list to store the input vector for the fault.
   - Iterate over each input node in the circuit.
   - For each input node, set its value to 1 (for SA0 fault type) or 0 (for SA1 fault type).
   - Propagate the values through the circuit by evaluating the gate operations until the output node's value is determined.
   - Append the value of each input node to the input vector list.
   - Reset the input nodes to their original values (0 or 1) for subsequent iterations.

4. **Print the Input Vector and Expected Output:**
   - Write the input vector and expected output to the "output.txt" file.
   - The input vector is the list of input node values, and the expected output is the value of the output node.

## Complexity Analysis:

1. **Reading Circuit File:**
   - Time Complexity: O(N)
   - N is the number of lines in the circuit file.
   - Space Complexity: O(1)

2. **Identifying Fault Location and Type:**
   - Time Complexity: O(1)
   - Space Complexity: O(1)

3. **Finding the Input Vector for the Fault:**
   - Time Complexity:
     - In the worst case, when all internal nodes depend on each other, the time complexity is O(N^2).
     - In the average case, the time complexity is O(N).
     - N is the number of internal nodes in the circuit.
   - Space Complexity: O(N)
     - Additional space is required to store the input vector and intermediate node values.

4. **Printing the Input Vector and Expected Output:**
   - Time Complexity: O(N)
   - M is the number of internal nodes in the circuit.
   - Space Complexity: O(1)

**Overall Time Complexity:**
The time complexity of the algorithm is dominated by the step of finding the input vector for the fault, which has a worst-case time complexity of O(N^2) and an average-case time complexity of O(N), where N is the number of internal nodes in the circuit.

**Overall Space Complexity:**
The space complexity of the algorithm is primarily determined by the size of the circuit and the intermediate data structures. It is O(N), where N is the number of internal nodes in the circuit.

## Implementation:
**Circuit Class:**
The Circuit class represents the circuit and contains a list of nodes. It provides methods to add nodes and retrieve nodes by name.

![image](https://github.com/geetika001/faulty-finder/assets/98944568/5c7636df-0066-4b0a-a3d7-aa3291b48492)

**Node Class:**
The Node class represents a node in the circuit. It contains information such as the node name, gate type, input nodes, and node value.

![image](https://github.com/geetika001/faulty-finder/assets/98944568/138dda77-d6b8-4086-b95a-19e614dafa10)

**CircuitFileReader Class:**
The CircuitFileReader class is responsible for reading the circuit file and constructing the circuit object. It parses each line of the file, identifies the input and output nodes, and creates Node objects accordingly.

![image](https://github.com/geetika001/faulty-finder/assets/98944568/b92b7106-1f27-4648-b9c0-f999d5b35ab9)

**Finding Input Vector for Fault:**
To find the input vector for the fault, the algorithm iterates over the input nodes and assigns appropriate values (1 or 0) based on the fault type (SA0 or SA1). It then propagates the values through the circuit by evaluating the gate operations until the output node's value is determined.

**Writing Output to File:**
The algorithm writes the input vector and expected output to the "output.txt" file. The input vector is the list of input node values, and the expected output is the value of the output node.

