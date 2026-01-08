package Project;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Product implements Serializable {
    private String productID;
    private String name;
    private double price;
    
    // Map: Key = Nama Outlet (KLCC), Value = Jumlah Stok
    private Map<String, Integer> stockDistribution;

    public Product(String productID, String name, double price) {
        this.productID = productID;
        this.name = name;
        this.price = price;
        this.stockDistribution = new HashMap<>();
    }

    // --- GETTERS STANDAR ---
    public String getProductID() { return productID; }
    public String getName() { return name; }
    public double getPrice() { return price; }

    // --- METHODS STOK (PENTING) ---

    // 1. [YANG TADI ERROR] Method ini yang dicari oleh SearchSystem
    public Map<String, Integer> getStockDistribution() {
        return stockDistribution;
    }

    // 2. Ambil stok spesifik cabang (Dipakai saat Staff Login)
    public int getQuantity(String outletId) {
        return stockDistribution.getOrDefault(outletId, 0);
    }
    
    // 3. Set stok cabang tertentu (Dipakai saat Stock In/Out)
    public void setQuantity(String outletId, int qty) {
        stockDistribution.put(outletId, qty);
    }

    // 4. Ambil total stok semua cabang (Optional, helper)
    public int getTotalQuantity() {
        int total = 0;
        for (int qty : stockDistribution.values()) total += qty;
        return total;
    }

    // --- FORMATTING (UNTUK CSV & TAMPILAN) ---

    // String untuk CSV: "ID,Name,Price,KLCC:10;UMCentral:5"
    // (Kode ini SAMA dengan punya kamu sebelumnya, agar save file tidak rusak)
    @Override
    public String toString() {
        String stockStr = stockDistribution.entrySet().stream()
            .map(e -> e.getKey() + ":" + e.getValue())
            .collect(Collectors.joining(";"));
            
        if (stockStr.isEmpty()) stockStr = "None:0";
        
        return productID + "," + name + "," + price + "," + stockStr;
    }
    
    // String untuk tampilan tabel di Manager View
    public String getStockDisplay() {
        if (stockDistribution.isEmpty()) return "Out of Stock";
        return stockDistribution.entrySet().stream()
            .map(e -> e.getKey() + ": " + e.getValue())
            .collect(Collectors.joining(" | "));
    }
}