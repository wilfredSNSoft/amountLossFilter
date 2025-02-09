import java.io.*;
import java.util.*;

public class providerFilter {
    public static void main(String[] args) {
        // Create a scanner object to read user input
        Scanner scanner = new Scanner(System.in);

        // Prompt user to enter the input file path
        System.out.print("Enter the path of the CSV input file: ");
        String inputFilePath = scanner.nextLine().trim();  // Get the input file path from the user

        // Define the path for the categories text file
        String categoriesFilePath = "categories.txt";  // Path to the categories text file

        // Read the categories from the text file into a Set
        Set<String> categories = readCategories(categoriesFilePath);

        // Create a map to hold writers for each category dynamically
        Map<String, BufferedWriter> writers = new HashMap<>();

        // Open the input file for reading
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {

            // Loop through each category and create a corresponding output file
            for (String category : categories) {
                // For each category, create a BufferedWriter that will append to the file
                BufferedWriter writer = new BufferedWriter(new FileWriter(category + "_output.csv", true));
                writers.put(category, writer);
            }

            String line;
            String lastProcessedType = "";  // Tracks last processed type (for switching to "others")

            // Read the input file line by line
            while ((line = reader.readLine()) != null) {
                // Split the line by comma (CSV format)
                String[] columns = line.split(",");

                if (columns.length > 3) {  // Ensure we have enough columns
                    String actionType = columns[3].trim().toLowerCase();  // The 4th column (case-insensitive)

                    // Check if the actionType matches any category
                    boolean matched = false;
                    for (String category : categories) {
                        // Check both 'Transfer-In' and 'transferIn' for case insensitivity
                        if (actionType.contains(category.toLowerCase())) {
                            // Write to the corresponding category file
                            BufferedWriter writer = writers.get(category);
                            if (writer != null) {
                                writer.write(line);
                                writer.newLine();  // Write the line to the output file
                            }
                            matched = true;
                            break;  // Break out of the loop since a match is found
                        }
                    }

                    // If no match was found, write to "others"
                    if (!matched) {
                        BufferedWriter othersWriter = writers.get("others");
                        if (othersWriter != null) {
                            othersWriter.write(line);
                            othersWriter.newLine();  // Write the line to the "others" file
                        }
                    }
                }
            }

            System.out.println("Processing complete!");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close all BufferedWriter objects
            writers.values().forEach(writer -> {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            scanner.close();  // Close the scanner resource
        }
    }

    /**
     * Reads categories from a text file and returns them as a set.
     *
     * @param categoriesFilePath Path to the categories text file
     * @return Set of categories
     */
    private static Set<String> readCategories(String categoriesFilePath) {
        Set<String> categories = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(categoriesFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String category = line.trim();
                if (!category.isEmpty()) {
                    categories.add(category);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return categories;
    }
}
