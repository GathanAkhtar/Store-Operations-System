package Project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;

public class SearchSystem {
    private InventorySystem inventorySystem;
    
    // [PERBAIKAN] Ganti ke "sales_history.csv" agar sesuai dengan SalesSystem
    private final String SALES_FILE = "sales_history.csv"; 

    public SearchSystem(InventorySystem inventorySystem) {
        this.inventorySystem = inventorySystem;
    }

    // === 1. STOCK SEARCH (Model Name -> All Outlets) ===
    public String searchStockByModel(String query) {
        StringBuilder result = new StringBuilder();
        ArrayList<Product> products = inventorySystem.getAllProducts();
        boolean found = false;

        result.append("=== SEARCH RESULT: '").append(query).append("' ===\n\n");

        for (Product p : products) {
            // Case insensitive search
            if (p.getName().toLowerCase().contains(query.toLowerCase()) || 
                p.getProductID().toLowerCase().contains(query.toLowerCase())) {
                
                found = true;
                result.append("Product: ").append(p.getName()).append(" (ID: ").append(p.getProductID()).append(")\n");
                result.append("Price: RM ").append(p.getPrice()).append("\n");
                result.append("Stock Availability:\n");
                
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

    // === 2. SALES SEARCH (Date / Customer / Model) ===
    public String searchSalesRecord(String query, String searchType) {
        StringBuilder result = new StringBuilder();
        result.append("=== SALES RECORDS (Filter: ").append(searchType).append(" - '").append(query).append("') ===\n\n");
        boolean found = false;
        
        try (BufferedReader br = new BufferedReader(new FileReader(SALES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Lewati header atau baris kosong
                if(!line.contains(",") || line.startsWith("Date")) continue;

                String[] data = line.split(","); 
                // Format CSV: Date(0), Time(1), TransID(2), Cust(3), Staff(4), Outlet(5), Items(6), Total(7), Method(8)
                
                if (data.length < 8) continue;

                boolean match = false;
                String date = data[0];      
                String customer = data[3];  
                String items = data[6];     
                
                // Logic Pencarian
                if (searchType.equals("DATE")) {
                    if (date.contains(query)) match = true;
                } 
                else if (searchType.equals("CUSTOMER")) {
                    if (customer.toLowerCase().contains(query.toLowerCase())) match = true;
                } 
                else if (searchType.equals("MODEL")) {
                    // Cari di dalam kolom items
                    if (items.toLowerCase().contains(query.toLowerCase())) match = true;
                }

                if (match) {
                    found = true;
                    result.append("Date: ").append(data[0]).append(" | Time: ").append(data[1]).append("\n");
                    result.append("Trans ID: ").append(data[2]).append("\n");
                    result.append("Customer: ").append(data[3]).append("\n");
                    result.append("Outlet: ").append(data[5]).append("\n");
                    result.append("Items: ").append(data[6]).append("\n");
                    result.append("Total: RM ").append(data[7]).append("\n");
                    result.append("------------------------------------------------\n");
                }
            }
        } catch (Exception e) {
            return "Error: Could not read " + SALES_FILE + " (File not found or empty).";
        }

        if (!found) return "No transactions found for criteria: " + query;
        return result.toString();
    }
}