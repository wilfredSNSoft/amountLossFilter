import java.io.*;
import java.util.*;

public class EzSort {

    // Method to read categories from a text file
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

    // Method to filter data based on categories and write to respective CSV files
    private static void filterData(String inputFilePath, Set<String> categories) {
        Map<String, BufferedWriter> writers = new HashMap<>();
        
        // Open the input file for reading
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {
            // Create a writer for each category file
            for (String category : categories) {
                BufferedWriter writer = new BufferedWriter(new FileWriter("providerFilter/" + category + "_output.csv", true));
                writers.put(category, writer);
            }
            // Create a writer for 'others' category
            BufferedWriter othersWriter = new BufferedWriter(new FileWriter("providerFilter/others_output.csv", true));
            writers.put("others", othersWriter);

            String line;
            // Process the input CSV line by line
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 3) {
                    String actionType = columns[3].trim().toLowerCase();

                    boolean matched = false;
                    for (String category : categories) {
                        if (actionType.contains(category.toLowerCase())) {
                            BufferedWriter writer = writers.get(category);
                            writer.write(line);
                            writer.newLine();
                            matched = true;
                            break;
                        }
                    }
                    // If no category matched, write to "others"
                    if (!matched) {
                        BufferedWriter othersWriterFile = writers.get("others");
                        othersWriterFile.write(line);
                        othersWriterFile.newLine();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close all writers
            writers.values().forEach(writer -> {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    // Method to sort the filtered CSV file
    private static void sortFile(String inputCsv, String outputCsv) {
        try (BufferedReader br = new BufferedReader(new FileReader(inputCsv));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputCsv))) {

            String line;
            List<String[]> rows = new ArrayList<>();

            // Read all rows into a list
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] columns = line.split(",");
                if (columns.length < 3) continue; // Skip malformed rows
                rows.add(columns);
            }

            if (rows.size() <= 0) return; // No data to process

            // Write the first row (header) to output
            bw.write(String.join(",", rows.get(0)));
            bw.newLine();

            String[] previousRow = rows.get(0);
            for (int i = 1; i < rows.size(); i++) {
                String[] currentRow = rows.get(i);
                String currentTime = currentRow[2].split(" ")[1];  // HH:MM
                String previousTime = previousRow[2].split(" ")[1];  // HH:MM

                // Parse the times for comparison
                String[] currentTimeParts = currentTime.split(":");
                String[] previousTimeParts = previousTime.split(":");

                int currentHH = Integer.parseInt(currentTimeParts[0]);
                int currentMM = Integer.parseInt(currentTimeParts[1]);

                int previousHH = Integer.parseInt(previousTimeParts[0]);
                int previousMM = Integer.parseInt(previousTimeParts[1]);

                int diffHH = currentHH - previousHH;
                int diffMM = currentMM - previousMM;

                // Adjust for cases where current minute is less than the previous minute (same hour)
                if (diffMM < 0) {
                    // When current minute is less than previous minute, we are within the same hour
                    diffMM = previousMM - currentMM;  // Add 60 to the current minute
                    diffHH = 0; // Keep the hour difference as zero since we are still in the same hour
                }

                // Debug: Print the time differences for each row comparison
                System.out.println("Comparing times: " + previousTime + " and " + currentTime);
                System.out.println("Time difference: " + diffHH + " hours, " + diffMM + " minutes");

                // Handle the negative hour difference (diffHH < 0) case:
                if (diffHH < 0) {
                    // Adjust for the negative hour difference by adding 60 minutes to the minute difference
                    diffMM = 60 - Math.abs(diffMM);  // Calculate the minutes across the hour boundary

                    // After adjustment, the time difference should be positive, or zero if it's within the same hour
                    if (diffMM < 30) {
                        System.out.println("Skipped row: " + String.join(",", currentRow));
                        continue; // Skip the row if the minute difference is less than 30
                    } else {
                        // If the minute difference is 30 or more, accept the row
                        System.out.println("Accepted row: " + String.join(",", currentRow));
                        bw.write(String.join(",", currentRow));
                        bw.newLine();
                        previousRow = currentRow;  // Update the pillar
                        continue;
                    }
                }

                // Now check the condition for whether to accept the current row
                if (diffHH == 0 && diffMM >= 30) {
                    // Case 1: Same hour, but minute difference >= 30
                    System.out.println("Accepted row: " + String.join(",", currentRow));
                    bw.write(String.join(",", currentRow));
                    bw.newLine();
                    previousRow = currentRow;  // Update the pillar
                } else if (diffHH > 1) {
                    // Case 2: More than 1 hour difference, always accept the row
                    System.out.println("Accepted row: " + String.join(",", currentRow));
                    bw.write(String.join(",", currentRow));
                    bw.newLine();
                    previousRow = currentRow;  // Update the pillar
                } else {
                    System.out.println("Skipped row: " + String.join(",", currentRow));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to clear all files in a directory
    private static void clearDirectory(String directoryPath) {
        File dir = new File(directoryPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        // Clear the directories before starting the program
        clearDirectory("providerFilter");
        clearDirectory("amountLossFilter");
        
        // Recreate the directories
        new File("providerFilter").mkdirs();
        new File("amountLossFilter").mkdirs();

        Scanner scanner = new Scanner(System.in);
        
        // Step 1: Get file paths from user input
        System.out.print("Enter the path of the CSV input file: ");
        String inputFilePath = scanner.nextLine().trim();
        
        // Categories file path
        String categoriesFilePath = "categories.txt";
        
        // Step 2: Read categories from categories.txt
        Set<String> categories = readCategories(categoriesFilePath);

        // Step 3: Filter input data into category-specific CSV files
        filterData(inputFilePath, categories);

        // Step 4: Sort each filtered CSV file
        for (String category : categories) {
            String outputFilteredFile = "providerFilter/" + category + "_output.csv";
            String outputSortedFile = "amountLossFilter/" + category + "_output_sorted.csv";
            sortFile(outputFilteredFile, outputSortedFile);
        }

        // Sort 'others' category
        sortFile("providerFilter/others_output.csv", "amountLossFilter/others_output_sorted.csv");

        System.out.println("Processing complete!");
        scanner.close();
    }
}
