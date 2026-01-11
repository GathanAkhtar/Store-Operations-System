package Project;

import java.io.*;
import java.util.*;

public class PerformanceAnalytics {
    private InventorySystem inventorySystem;
    private final String TRANSACTION_FILE = "transactions.csv";

    public PerformanceAnalytics(InventorySystem inv) {
        this.inventorySystem = inv;
    }

    public List<EmployeeStats> getEmployeePerformanceReport(User currentUser) throws SecurityException {
        // SECURITY CHECK: Cuma Manager yang boleh lihat
        if (!(currentUser instanceof Manager)) {
            throw new SecurityException("ACCESS DENIED: Only Managers can view Performance Metrics.");
        }

        Map<String, EmployeeStats> statsMap = new HashMap<>();
        File file = new File(TRANSACTION_FILE);

        if (!file.exists()) return new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Format CSV: Date, Type, PID, Qty, Name
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String type = parts[1];
                    String pid = parts[2];
                    int qty = Integer.parseInt(parts[3]);
                    String empName = parts[4];

                    // Hitung jika tipe transaksi adalah STOCK_OUT (Penjualan)
                    if (type.startsWith("STOCK_OUT")) {
                        Product p = inventorySystem.findProduct(pid);
                        if (p != null) {
                            double value = p.getPrice() * qty;
                            statsMap.putIfAbsent(empName, new EmployeeStats(empName));
                            statsMap.get(empName).addSale(value);
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        List<EmployeeStats> list = new ArrayList<>(statsMap.values());
        Collections.sort(list); // Sort highest to lowest
        return list;
    }
}