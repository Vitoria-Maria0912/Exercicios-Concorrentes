import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    
    // Read a file from a filepath and return a byte array
    public static byte[] readFile(String filePath) throws IOException {
        return Files.readAllBytes(Paths.get(filePath));
    }
    
    // Sum all bytes of a file
    public static int sum(String filePath) {
        try {
            byte[] data = readFile(filePath);
            int sum = 0;
            for (byte b : data) {
                sum += Byte.toUnsignedInt(b);
            }
            return sum;
        } catch (IOException e) {
            System.out.printf("Error reading file %s: %s%n", filePath, e.getMessage());
            return 0;
        }
    }
    
    // Print the total sum for all files and the files with equal sum
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Main <file1> <file2> ...");
            return;
        }
        
        long totalSum = 0;
        Map<Integer, List<String>> sums = new HashMap<>();
        
        for (String path : args) {
            int fileSum = sum(path);
            totalSum += fileSum;
            
            sums.computeIfAbsent(fileSum, k -> new ArrayList<>()).add(path);
        }
        
        System.out.println(totalSum);
        
        for (Map.Entry<Integer, List<String>> entry : sums.entrySet()) {
            if (entry.getValue().size() > 1) {
                System.out.printf("Sum %d: %s%n", entry.getKey(), entry.getValue());
            }
        }
    }
}
