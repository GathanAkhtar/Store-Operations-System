package Project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PerformanceAnalytics {
    
    private InventorySystem inventorySystem;
    private final String SALES_FILE = "sales_history.csv"; 

    public PerformanceAnalytics(InventorySystem inventorySystem) {
        this.inventorySystem = inventorySystem;
    }

    public List<EmployeeStats> getEmployeePerformanceReport(Manager requester) {
        if (requester == null) throw new SecurityException("Unauthorized Access");

        Map<String, EmployeeStats> statsMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Hapus spasi kosong di awal/akhir baris
                line = line.trim();
                if (line.isEmpty()) continue;

                // Split berdasarkan koma
                String[] parts = line.split(",");

                // Kita butuh minimal sampai index 7 (Harga) agar aman
                if (parts.length < 8) continue;

                // --- LOGIKA PASTI (HARDCODED INDEX) ---
                // Berdasarkan screenshot kamu:
                // Kolom ke-5 adalah Nama Employee -> Index 4
                // Kolom ke-8 adalah Total Harga   -> Index 7
                
                String empName = parts[4].trim(); // Ambil nama (Fulanah, BOB, dll)
                String priceStr = parts[7].trim(); // Ambil harga (60, 30.00, dll)

                double total = 0.0;
                try {
                    // Bersihkan harga dari "RM" atau spasi jika ada
                    priceStr = priceStr.replace("RM", "").replace(" ", "");
                    total = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    // Jika harga error (misal kosong), skip baris ini
                    continue;
                }

                // Masukkan data ke statistik
                statsMap.putIfAbsent(empName, new EmployeeStats(empName));
                statsMap.get(empName).addTransaction(total);
            }
        } catch (IOException e) {
            System.out.println("Error reading sales file: " + e.getMessage());
        }

        // Urutkan dari Sales Tertinggi ke Terendah
        return statsMap.values().stream()
                .sorted((a, b) -> Double.compare(b.getTotalSales(), a.getTotalSales()))
                .collect(Collectors.toList());
    }
}