package Project;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class StockCountSystem {
    private InventorySystem inventorySystem;
    private final String COUNT_FILE = "stock_counts.csv";
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StockCountSystem(InventorySystem inventorySystem) {
        this.inventorySystem = inventorySystem;
    }

    // Method untuk melakukan Stock Count
    public String performStockCount(Employee employee, String sessionType, ArrayList<Product> outletProducts) {
        StringBuilder report = new StringBuilder();
        report.append("=== ").append(sessionType.toUpperCase()).append(" COUNT REPORT ===\n");
        report.append("Outlet: ").append(employee.getOutletId()).append("\n");
        report.append("Staff: ").append(employee.getName()).append("\n\n");
        
        boolean hasDiscrepancy = false;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COUNT_FILE, true))) {
            for (Product p : outletProducts) {
                int systemQty = p.getQuantity(employee.getOutletId());
                
                // Ini nanti akan dipanggil satu-satu di Tester via Input Dialog
                // Tapi untuk method ini, kita asumsikan kita terima data fisik
                // (Lihat implementasi di Tester nanti untuk loop inputnya)
            }
        } catch (IOException e) {
            return "Error saving count data.";
        }
        return "Count Completed.";
    }

    // Kita butuh helper untuk save satu baris record count
    public void logCount(String session, String outlet, String staff, String productId, String productName, int sysQty, int physicalQty) {
        int variance = physicalQty - sysQty;
        String status = (variance == 0) ? "MATCH" : "DISCREPANCY (" + variance + ")";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(COUNT_FILE, true))) {
            String record = LocalDateTime.now().format(dtf) + "," +
                            session + "," +
                            outlet + "," +
                            staff + "," +
                            productId + "," +
                            productName + "," +
                            sysQty + "," +
                            physicalQty + "," +
                            variance;
            writer.write(record);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Failed to log count.");
        }
    }

    public String readCountHistory() {
        File file = new File(COUNT_FILE);
        if (!file.exists()) return "No stock count records found.";

        StringBuilder sb = new StringBuilder("=== STOCK COUNT HISTORY ===\n");
        // Header Table
        sb.append(String.format("%-20s | %-10s | %-10s | %-15s | %-5s | %-5s | %-10s\n", 
                "Date", "Session", "Outlet", "Product", "Sys", "Phys", "Variance"));
        sb.append("---------------------------------------------------------------------------------------------\n");

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] d = line.split(",");
                if (d.length >= 9) {
                    sb.append(String.format("%-20s | %-10s | %-10s | %-15s | %-5s | %-5s | %-10s\n", 
                            d[0], d[1], d[2], d[5], d[6], d[7], d[8]));
                }
            }
        } catch (Exception e) {}
        return sb.toString();
    }
}