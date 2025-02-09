import java.io.*;
import java.util.*;

public class sort {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Sort <input-csv-file> <output-csv-file>");
            return;
        }

        String inputCsv = args[0];
        String outputCsv = args[1];

        try (BufferedReader br = new BufferedReader(new FileReader(inputCsv));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputCsv))) {

            String line;
            List<String[]> rows = new ArrayList<>();
            
            // Read all rows into a list
            // For Abnormal Use
            while ((line = br.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = line.split(",");  // Now split by comma
                // Skip rows that don't have at least 3 columns (timestamp column is missing)
                if (columns.length < 3) {
                    System.out.println("Skipping malformed row: " + line);
                    continue;
                }

                rows.add(columns);
            }

            // If there's no data or only one row, exit early
            if (rows.size() <= 1) {
                System.out.println("Not enough data to process.");
                return;
            }

            // Write the first row to the output as the pillar
            bw.write(String.join(",", rows.get(0)));  // Now using commas for output
            bw.newLine();

            // Process all rows for comparison
            String[] previousRow = rows.get(0);  // The first row is the initial pillar
            for (int i = 1; i < rows.size(); i++) {  // Iterate through all rows
                String[] currentRow = rows.get(i);

                // Extract HH:MM from the third column (assuming date format: yyyy-MM-dd HH:mm:ss)
                String currentTime = currentRow[2].split(" ")[1];  // HH:MM
                String previousTime = previousRow[2].split(" ")[1];  // HH:MM

                // Parse the times for comparison
                String[] currentTimeParts = currentTime.split(":");
                String[] previousTimeParts = previousTime.split(":");

                int currentHH = Integer.parseInt(currentTimeParts[0]);
                int currentMM = Integer.parseInt(currentTimeParts[1]);

                int previousHH = Integer.parseInt(previousTimeParts[0]);
                int previousMM = Integer.parseInt(previousTimeParts[1]);

                // Calculate the difference in hours and minutes
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

            System.out.println("File processing complete. Sorted data saved to: " + outputCsv);

        } catch (IOException e) {
            System.out.println("Error reading or writing file: " + e.getMessage());
        }
    }
}
