package Project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;

public class SearchSystem {
    private InventorySystem inventorySystem;
    private final String SALES_FILE = "sales_history.csv"; 

    public SearchSystem(InventorySystem inventorySystem) {
        this.inventorySystem = inventorySystem;
    }

    // === 1. STOCK SEARCH (Tidak diubah) ===
    public String searchStockByModel(String query) {
        StringBuilder result = new StringBuilder();
        ArrayList<Product> products = inventorySystem.getAllProducts();
        boolean found = false;

        result.append("=== SEARCH RESULT: '").append(query).append("' ===\n\n");

        for (Product p : products) {
            if (p.getName().toLowerCase().contains(query.toLowerCase()) || 
                p.getProductID().toLowerCase().contains(query.toLowerCase())) {
                
                found = true;
                result.append("Product: ").append(p.getName()).append(" (ID: ").append(p.getProductID()).append(")\n");
                result.append("Price: RM ").append(p.getPrice()).append("\n");
                
                Map<String, Integer> stockMap = p.getStockDistribution();
                if (stockMap.isEmpty()) {
                    result.append("   - No Stock in any outlet.\n");
                } else {
                    for (Map.Entry<String, Integer> entry : stockMap.entrySet()) {
                        result.append("   - ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" units\n");
                    }
                }
                result.append("------------------------------------------------\n");
            }
        }
        if (!found) return "No products found matching: " + query;
        return result.toString();
    }

    // === 2. SALES SEARCH (VERSI UNIVERSAL & DEBUG) ===
    public String searchSalesRecord(String query, String searchType) {
        StringBuilder result = new StringBuilder();
        result.append("=== SALES RECORDS (Searching for: '").append(query).append("') ===\n\n");
        
        boolean found = false;
        // Bersihkan query dari user (hapus spasi depan/belakang)
        String cleanQuery = query.trim().toLowerCase(); 

        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip baris kosong
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",");
                
                // Pastikan data lengkap (Minimal sampai harga/index 7)
                if (data.length < 8) continue;

                // --- AMBIL DATA DARI CSV (SESUAI GAMBAR KAMU) ---
                String date     = data[0].trim(); // Index 0
                String time     = data[1].trim(); // Index 1
                String customer = data[3].trim(); // Index 3 (Customer Name)
                String employee = data[4].trim(); // Index 4 (Employee)
                String items    = data[6].trim(); // Index 6 (Item List)
                String total    = data[7].trim(); // Index 7 (Price)

                boolean match = false;

                // --- LOGIKA "UNIVERSAL MATCH" ---
                // Tidak peduli user pilih menu Date atau Customer, 
                // kita cek SEMUANYA agar pasti ketemu.
                
                // 1. Cek Tanggal (Harus persis sama)
                if (date.equals(cleanQuery) || date.equalsIgnoreCase(query)) {
                    match = true;
                }
                // 2. Cek Nama Customer (Mengandung kata kunci)
                else if (customer.toLowerCase().contains(cleanQuery)) {
                    match = true;
                }
                // 3. Cek Nama Item/Model (Mengandung kata kunci)
                else if (items.toLowerCase().contains(cleanQuery)) {
                    match = true;
                }

                // Jika ketemu, masukkan ke hasil
                if (match) {
                    found = true;
                    result.append("Date: ").append(date).append(" | Time: ").append(time).append("\n");
                    result.append("Customer: ").append(customer).append("\n");
                    result.append("Employee: ").append(employee).append("\n");
                    result.append("Items   : ").append(items).append("\n");
                    result.append("Total   : RM ").append(total).append("\n");
                    result.append("------------------------------------------------\n");
                }
            }
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }

        if (!found) {
            // Pesan Error yang informatif
            return "Transaction NOT FOUND.\n" +
                   "Your Input: '" + query + "'\n" +
                   "Tip: Try checking the exact spelling or date format (YYYY-MM-DD).";
        }
        return result.toString();
    }
}